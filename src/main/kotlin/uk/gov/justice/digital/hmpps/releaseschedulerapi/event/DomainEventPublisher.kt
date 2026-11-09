package uk.gov.justice.digital.hmpps.releaseschedulerapi.event

import org.springframework.data.domain.Pageable
import org.springframework.retry.RetryPolicy
import org.springframework.retry.backoff.BackOffPolicy
import org.springframework.retry.support.RetryTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.sns.model.PublishBatchRequest
import software.amazon.awssdk.services.sns.model.PublishBatchRequestEntry
import software.amazon.awssdk.services.sns.model.PublishBatchResponse
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.releaseschedulerapi.config.ServiceConfig
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.HmppsDomainEventRepository
import uk.gov.justice.hmpps.sqs.DEFAULT_BACKOFF_POLICY
import uk.gov.justice.hmpps.sqs.DEFAULT_RETRY_POLICY
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import uk.gov.justice.hmpps.sqs.eventTypeSnsMap
import java.util.UUID
import kotlin.collections.forEach
import kotlin.text.chunked

@Service
class DomainEventPublisher(
  private val jsonMapper: JsonMapper,
  private val hmppsQueueService: HmppsQueueService,
  private val domainEventRepository: HmppsDomainEventRepository,
  private val serviceConfig: ServiceConfig,
) {
  private val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId("hmppseventtopic") ?: throw IllegalStateException("Domain event topic not found")
  }

  private val domainEventsQueue: HmppsQueue by lazy {
    hmppsQueueService.findByQueueId("hmppsdomaineventsqueue") ?: throw IllegalStateException("Domain event queue not available")
  }

  @Transactional
  fun publishUnpublishedEvents() {
    domainEventRepository.findByPublishedIsFalseOrderById(Pageable.ofSize(serviceConfig.domainEvents.batchSize))
      .takeIf { it.isNotEmpty() }
      ?.also { events -> domainEventsTopic.publishBatch(events) }
      ?.forEach { it.published = true }
  }

  private fun HmppsTopic.publishBatch(
    events: List<HmppsDomainEvent>,
    retryPolicy: RetryPolicy = DEFAULT_RETRY_POLICY,
    backOffPolicy: BackOffPolicy = DEFAULT_BACKOFF_POLICY,
  ) {
    val retryTemplate = RetryTemplate().apply {
      setRetryPolicy(retryPolicy)
      setBackOffPolicy(backOffPolicy)
    }
    val publishRequest = PublishBatchRequest.builder().topicArn(arn).publishBatchRequestEntries(
      events.map {
        PublishBatchRequestEntry.builder()
          .id(it.id.toString())
          .message(jsonMapper.writeValueAsString(it.event))
          .messageAttributes(eventTypeSnsMap(it.eventType, noTracing = true))
          .build()
      },
    ).build()
    retryTemplate.execute<PublishBatchResponse, RuntimeException> {
      snsClient.publishBatch(publishRequest).get()
    }
  }

  // Temporary solution to populate data until migration and crd is ready
  @Async
  fun handleInternalEvents(ie: InternalEvents) {
    publishEventsInternally(ie.toPublish)
  }

  private fun publishEventsInternally(events: Collection<DomainEvent<*>>) {
    events.asSequence().chunked(10).forEach { domainEventsQueue.publishBatch(it) }
  }

  private fun HmppsQueue.publishBatch(
    events: Collection<DomainEvent<*>>,
    retryPolicy: RetryPolicy = DEFAULT_RETRY_POLICY,
    backOffPolicy: BackOffPolicy = DEFAULT_BACKOFF_POLICY,
  ) {
    val retryTemplate =
      RetryTemplate().apply {
        setRetryPolicy(retryPolicy)
        setBackOffPolicy(backOffPolicy)
      }
    val publishRequest =
      SendMessageBatchRequest
        .builder()
        .queueUrl(queueUrl)
        .entries(
          events.map {
            val notification =
              Notification(jsonMapper.writeValueAsString(it), attributes = MessageAttributes(it.eventType))
            SendMessageBatchRequestEntry
              .builder()
              .id(UUID.randomUUID().toString())
              .messageBody(jsonMapper.writeValueAsString(notification))
              .messageAttributes(notification.attributes.map { a -> a.key to MessageAttributeValue.builder().dataType(a.value.type).stringValue(a.value.value).build() }.toMap())
              .build()
          },
        ).build()
    retryTemplate.execute<SendMessageBatchResponse, RuntimeException> {
      sqsClient.sendMessageBatch(publishRequest).get()
    }
  }
}
