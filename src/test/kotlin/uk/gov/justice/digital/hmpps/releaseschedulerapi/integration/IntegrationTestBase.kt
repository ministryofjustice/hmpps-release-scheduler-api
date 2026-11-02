package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.envers.AuditReaderFactory
import org.hibernate.envers.RevisionType
import org.hibernate.envers.query.AuditEntity
import org.hibernate.envers.query.AuditEntity.revisionNumber
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.transaction.support.TransactionTemplate
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.releaseschedulerapi.context.SchedulerContext
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.AuditRevision
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.DomainEventPublication
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Identifiable
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.publication
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.DomainEvent
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.TestConfig
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.container.LocalStackContainer
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.container.LocalStackContainer.setMiniStackProperties
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.container.PostgresContainer
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.wiremock.ManageUsersExtension
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.wiremock.PrisonerRegisterExtension
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.wiremock.PrisonerSearchExtension
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingTopicException
import uk.gov.justice.hmpps.sqs.publish
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@Import(TestConfig::class)
@ExtendWith(
  HmppsAuthApiExtension::class,
  ManageUsersExtension::class,
  PrisonerSearchExtension::class,
  PrisonerRegisterExtension::class,
)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  protected lateinit var transactionTemplate: TransactionTemplate

  @Autowired
  protected lateinit var entityManager: EntityManager

  @Autowired
  protected lateinit var jsonMapper: JsonMapper

  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  protected val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId("hmppseventtesttopic")
      ?: throw MissingTopicException("hmppseventtesttopic not found")
  }

  protected fun sendDomainEvent(event: DomainEvent<*>) {
    domainEventsTopic.publish(event.eventType, jsonMapper.writeValueAsString(event))
  }

  internal fun setAuthorisation(
    username: String? = DEFAULT_USERNAME,
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun verifyAudit(
    entity: Identifiable,
    revisionType: RevisionType,
    affectedEntities: Set<String>,
    context: SchedulerContext = SchedulerContext.get(),
  ) {
    transactionTemplate.execute {
      val auditReader = AuditReaderFactory.get(entityManager)
      assertTrue(auditReader.isEntityClassAudited(entity::class.java))

      val revisionNumber =
        auditReader
          .getRevisions(entity::class.java, entity.id)
          .filterIsInstance<Long>()
          .max()

      val entityRevision: Array<*> =
        auditReader
          .createQuery()
          .forRevisionsOfEntity(entity::class.java, false, true)
          .add(revisionNumber().eq(revisionNumber))
          .add(AuditEntity.id().eq(entity.id))
          .resultList
          .first() as Array<*>
      assertThat(entityRevision[2]).isEqualTo(revisionType)

      val auditRevision = entityRevision[1] as AuditRevision
      with(auditRevision) {
        assertThat(this.affectedEntities).containsExactlyInAnyOrderElementsOf(affectedEntities)
        assertThat(username).isEqualTo(context.username)
        assertThat(source).isEqualTo(context.source)
        assertThat(reason).isEqualTo(context.reason)
        assertThat(caseloadId).isEqualTo(context.caseloadId)
      }
    }
  }

  protected fun verifyEventPublications(
    entity: Identifiable,
    events: Set<DomainEventPublication>,
  ) {
    transactionTemplate.execute {
      val auditReader = AuditReaderFactory.get(entityManager)
      assertTrue(auditReader.isEntityClassAudited(entity::class.java))

      val revisionNumber =
        auditReader
          .getRevisions(entity::class.java, entity.id)
          .filterIsInstance<Long>()
          .max()

      val domainEventsPersisted: List<HmppsDomainEvent> =
        auditReader
          .createQuery()
          .forRevisionsOfEntity(HmppsDomainEvent::class.java, true, true)
          .add(revisionNumber().eq(revisionNumber))
          .resultList
          .filterIsInstance<HmppsDomainEvent>()
      domainEventsPersisted.forEach {
        assertThat(it.eventType).isEqualTo(it.event.eventType)
      }
      assertThat(domainEventsPersisted.map { de -> de.event.publication(de.entityId) { !de.published } })
        .containsExactlyInAnyOrderElementsOf(events)
    }
  }

  protected final inline fun <reified T : Any> WebTestClient.ResponseSpec.successResponse(status: HttpStatus = HttpStatus.OK): T = expectStatus().isEqualTo(status)
    .expectBody<T>()
    .returnResult().responseBody!!

  protected final fun WebTestClient.ResponseSpec.errorResponse(status: HttpStatus): ErrorResponse = expectStatus().isEqualTo(status)
    .expectBody<ErrorResponse>()
    .returnResult().responseBody!!

  @BeforeEach
  fun clearContext() {
    SchedulerContext.clear()
  }

  companion object {
    private val pgContainer = PostgresContainer.instance
    private val localStackContainer = LocalStackContainer.instance
    const val DEFAULT_USERNAME = "R3L5CH3DU13R"

    @JvmStatic
    @DynamicPropertySource
    @Suppress("unused")
    fun properties(registry: DynamicPropertyRegistry) {
      pgContainer?.also {
        registry.add("spring.datasource.url", pgContainer::getJdbcUrl)
        registry.add("spring.datasource.username", pgContainer::getUsername)
        registry.add("spring.datasource.password", pgContainer::getPassword)
      }

      System.setProperty("aws.region", "eu-west-2")
      localStackContainer?.also { setMiniStackProperties(it, registry) }
    }
  }
}
