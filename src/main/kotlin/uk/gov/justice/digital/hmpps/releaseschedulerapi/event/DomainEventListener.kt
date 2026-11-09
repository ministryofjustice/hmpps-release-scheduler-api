package uk.gov.justice.digital.hmpps.releaseschedulerapi.event

import io.awspring.cloud.sqs.annotation.SqsListener
import io.sentry.Sentry
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.releaseschedulerapi.context.SchedulerContext
import uk.gov.justice.digital.hmpps.releaseschedulerapi.service.PersonUpdatedHandler
import uk.gov.justice.digital.hmpps.releaseschedulerapi.service.PrisonerReceivedHandler
import uk.gov.justice.digital.hmpps.releaseschedulerapi.service.RefreshReleases

@Component
class DomainEventListener(
  private val jsonMapper: JsonMapper,
  private val personReceived: PrisonerReceivedHandler,
  private val personUpdated: PersonUpdatedHandler,
  private val refresh: RefreshReleases,
) {
  @SqsListener("hmppsdomaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun handleDomainEvent(notification: Notification) {
    try {
      when (val event = jsonMapper.readValue<DomainEvent<*>>(notification.message)) {
        is PrisonerReceived -> personReceived.handle(event)
        is PrisonerUpdated -> personUpdated.handle(event)
        is PrisonerMerged -> { /* TODO: implement merge */ }

        is RefreshActiveReleases -> refresh.active()
        is RefreshPrisonReleases -> refresh.prison(event)
      }
    } catch (ex: Exception) {
      Sentry.captureException(ex)
      throw ex
    } finally {
      SchedulerContext.clear()
    }
  }
}
