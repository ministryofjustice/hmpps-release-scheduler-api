package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.sync

import org.assertj.core.api.Assertions.assertThat
import org.hibernate.envers.RevisionType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.access.Roles
import uk.gov.justice.digital.hmpps.releaseschedulerapi.context.SchedulerContext
import uk.gov.justice.digital.hmpps.releaseschedulerapi.context.SchedulerContext.Companion.SYSTEM_USERNAME
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.DataSource
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.publication
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseAwaitingDetails
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseCompleted
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseScheduled
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseUnscheduled
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseUnverified
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseVerified
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.personIdentifier
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.prisonCode
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperations
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperationsImpl.Companion.release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.wiremock.PrisonerSearchExtension.Companion.prisonerSearch
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.wiremock.PrisonerSearchServer.Companion.prisoner
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.ReferenceId
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.SyncRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.SyncUser
import java.time.LocalDate

class SyncScheduledReleaseIntTest(
  @Autowired releaseOps: ReleaseOperations,
) : IntegrationTestBase(),
  ReleaseOperations by releaseOps {
  @Test
  fun `401 unauthorised without a valid token`() {
    webTestClient
      .put()
      .uri(SYNC, personIdentifier())
      .bodyValue(syncReleaseRequest())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `403 forbidden without correct role`() {
    sendRelease(personIdentifier(), syncRelease(), syncUser(), Roles.RELEASE_SCHEDULER_UI).expectStatus().isForbidden
  }

  @Test
  fun `409 - cannot amend a completed release`() {
    val existing = givenRelease(
      release(
        confirmedReleaseDate = LocalDate.now().minusDays(7),
        reasonCode = "CR",
        statusCode = ReleaseStatus.Code.COMPLETED,
      ),
    )

    val request = existing.syncRelease().copy(releaseDate = LocalDate.now().minusDays(5))
    val user = syncUser()
    sendRelease(existing.person.identifier, request, user).errorResponse(HttpStatus.CONFLICT)

    val saved = requireNotNull(findRelease(existing.id))
    assertThat(saved.status.code).isEqualTo(ReleaseStatus.Code.COMPLETED.name)
    assertThat(saved.confirmedReleaseDate).isEqualTo(existing.confirmedReleaseDate)
    assertThat(saved.reason?.code).isEqualTo(existing.reason?.code)

    verifyAudit(
      saved,
      RevisionType.ADD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get()
        .copy(username = SYSTEM_USERNAME, caseloadId = null, source = DataSource.DPS),
    )
  }

  @Test
  fun `200 - can create a new release awaiting details`() {
    val prisonCode = prisonCode()
    val prisoner = prisonerSearch.givenPrisoner(prisoner(prisonCode))

    val request = syncRelease()
    val user = syncUser()
    val res = sendRelease(prisoner.prisonerNumber, request, user).successResponse<ReferenceId>()

    val saved = requireNotNull(findRelease(res.dpsId))
    assertThat(saved.status.code).isEqualTo(ReleaseStatus.Code.PENDING.name)
    saved verifyAgainst request

    verifyAudit(
      saved,
      RevisionType.ADD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get()
        .copy(username = user.username, caseloadId = user.activeCaseloadId, source = DataSource.NOMIS),
    )

    verifyEventPublications(
      saved,
      setOf(ReleaseAwaitingDetails(saved.person.identifier, saved.id, DataSource.NOMIS).publication(saved.id)),
    )
  }

  @Test
  fun `200 - can create a new unverified release`() {
    val prisonCode = prisonCode()
    val prisoner = prisonerSearch.givenPrisoner(prisoner(prisonCode))

    val request = syncRelease(releaseDate = LocalDate.now().plusDays(14), verified = false)
    val user = syncUser()
    val res = sendRelease(prisoner.prisonerNumber, request, user).successResponse<ReferenceId>()

    val saved = requireNotNull(findRelease(res.dpsId))
    assertThat(saved.status.code).isEqualTo(ReleaseStatus.Code.UNVERIFIED.name)
    saved verifyAgainst request

    verifyAudit(
      saved,
      RevisionType.ADD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get()
        .copy(username = user.username, caseloadId = user.activeCaseloadId, source = DataSource.NOMIS),
    )

    verifyEventPublications(
      saved,
      setOf(ReleaseUnverified(saved.person.identifier, saved.id, DataSource.NOMIS).publication(saved.id)),
    )
  }

  @Test
  fun `200 - can create a new verified release`() {
    val prisonCode = prisonCode()
    val prisoner = prisonerSearch.givenPrisoner(prisoner(prisonCode))

    val request = syncRelease(releaseDate = LocalDate.now().plusDays(14), verified = true)
    val user = syncUser()
    val res = sendRelease(prisoner.prisonerNumber, request, user).successResponse<ReferenceId>()

    val saved = requireNotNull(findRelease(res.dpsId))
    assertThat(saved.status.code).isEqualTo(ReleaseStatus.Code.VERIFIED.name)
    saved verifyAgainst request

    verifyAudit(
      saved,
      RevisionType.ADD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get()
        .copy(username = user.username, caseloadId = user.activeCaseloadId, source = DataSource.NOMIS),
    )

    verifyEventPublications(
      saved,
      setOf(ReleaseVerified(saved.person.identifier, saved.id, DataSource.NOMIS).publication(saved.id)),
    )
  }

  @Test
  fun `200 - can create a new completed release`() {
    val prisonCode = prisonCode()
    val prisoner = prisonerSearch.givenPrisoner(prisoner(prisonCode))

    val request = syncRelease(statusCode = SyncRelease.EXPIRED)
    val user = syncUser()
    val res = sendRelease(prisoner.prisonerNumber, request, user).successResponse<ReferenceId>()

    val saved = requireNotNull(findRelease(res.dpsId))
    assertThat(saved.status.code).isEqualTo(ReleaseStatus.Code.COMPLETED.name)
    saved verifyAgainst request

    verifyAudit(
      saved,
      RevisionType.ADD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get()
        .copy(username = user.username, caseloadId = user.activeCaseloadId, source = DataSource.NOMIS),
    )

    verifyEventPublications(
      saved,
      setOf(ReleaseCompleted(saved.person.identifier, saved.id, DataSource.NOMIS).publication(saved.id)),
    )
  }

  @Test
  fun `200 - can update awaiting details to scheduled - unverified`() {
    val existing = givenRelease(release())
    val request = existing.syncRelease().copy(releaseDate = LocalDate.now().plusDays(7), reasonCode = "CR", verified = false)
    val user = syncUser()
    val res = sendRelease(existing.person.identifier, request, user).successResponse<ReferenceId>()

    val saved = requireNotNull(findRelease(res.dpsId))
    assertThat(saved.status.code).isEqualTo(ReleaseStatus.Code.UNVERIFIED.name)
    saved verifyAgainst request

    verifyAudit(
      saved,
      RevisionType.MOD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get()
        .copy(username = user.username, caseloadId = user.activeCaseloadId, source = DataSource.NOMIS),
    )

    verifyEventPublications(
      saved,
      setOf(ReleaseScheduled(saved.person.identifier, saved.id, DataSource.NOMIS).publication(saved.id)),
    )
  }

  @Test
  fun `200 - can verify an unverified release`() {
    val existing = givenRelease(
      release(
        confirmedReleaseDate = LocalDate.now().plusDays(7),
        reasonCode = "CR",
        statusCode = ReleaseStatus.Code.UNVERIFIED,
      ),
    )

    val request = existing.syncRelease().copy(verified = true)
    val user = syncUser()
    val res = sendRelease(existing.person.identifier, request, user).successResponse<ReferenceId>()

    val saved = requireNotNull(findRelease(res.dpsId))
    assertThat(saved.status.code).isEqualTo(ReleaseStatus.Code.VERIFIED.name)
    saved verifyAgainst request

    verifyAudit(
      saved,
      RevisionType.MOD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get()
        .copy(username = user.username, caseloadId = user.activeCaseloadId, source = DataSource.NOMIS),
    )

    verifyEventPublications(
      saved,
      setOf(ReleaseVerified(saved.person.identifier, saved.id, DataSource.NOMIS).publication(saved.id)),
    )
  }

  @Test
  fun `200 - can unschedule an unverified release`() {
    val existing = givenRelease(
      release(
        confirmedReleaseDate = LocalDate.now().plusDays(7),
        reasonCode = "CR",
        statusCode = ReleaseStatus.Code.UNVERIFIED,
      ),
    )

    val request = existing.syncRelease().copy(releaseDate = null)
    val user = syncUser()
    val res = sendRelease(existing.person.identifier, request, user).successResponse<ReferenceId>()

    val saved = requireNotNull(findRelease(res.dpsId))
    assertThat(saved.status.code).isEqualTo(ReleaseStatus.Code.PENDING.name)
    saved verifyAgainst request

    verifyAudit(
      saved,
      RevisionType.MOD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get()
        .copy(username = user.username, caseloadId = user.activeCaseloadId, source = DataSource.NOMIS),
    )

    verifyEventPublications(
      saved,
      setOf(ReleaseUnscheduled(saved.person.identifier, saved.id, DataSource.NOMIS).publication(saved.id)),
    )
  }

  @Test
  fun `200 - can unschedule a verified release`() {
    val existing = givenRelease(
      release(
        confirmedReleaseDate = LocalDate.now().plusDays(7),
        reasonCode = "CR",
        statusCode = ReleaseStatus.Code.VERIFIED,
      ),
    )

    val request = existing.syncRelease().copy(releaseDate = null)
    val user = syncUser()
    val res = sendRelease(existing.person.identifier, request, user).successResponse<ReferenceId>()

    val saved = requireNotNull(findRelease(res.dpsId))
    assertThat(saved.status.code).isEqualTo(ReleaseStatus.Code.PENDING.name)
    saved verifyAgainst request

    verifyAudit(
      saved,
      RevisionType.MOD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get()
        .copy(username = user.username, caseloadId = user.activeCaseloadId, source = DataSource.NOMIS),
    )

    verifyEventPublications(
      saved,
      setOf(ReleaseUnscheduled(saved.person.identifier, saved.id, DataSource.NOMIS).publication(saved.id)),
    )
  }

  @Test
  fun `200 - can unverify a verified release without other changes`() {
    val existing = givenRelease(
      release(
        confirmedReleaseDate = LocalDate.now().plusDays(7),
        reasonCode = "CR",
        statusCode = ReleaseStatus.Code.VERIFIED,
      ),
    )

    val request = existing.syncRelease().copy(verified = false)
    val user = syncUser()
    val res = sendRelease(existing.person.identifier, request, user).successResponse<ReferenceId>()

    val saved = requireNotNull(findRelease(res.dpsId))
    assertThat(saved.status.code).isEqualTo(ReleaseStatus.Code.UNVERIFIED.name)
    saved verifyAgainst request

    verifyAudit(
      saved,
      RevisionType.MOD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get()
        .copy(username = user.username, caseloadId = user.activeCaseloadId, source = DataSource.NOMIS),
    )

    verifyEventPublications(
      saved,
      setOf(ReleaseUnverified(saved.person.identifier, saved.id, DataSource.NOMIS).publication(saved.id)),
    )
  }

  @Test
  fun `200 - can unverify a verified release with other changes`() {
    val existing = givenRelease(
      release(
        confirmedReleaseDate = LocalDate.now().plusDays(7),
        reasonCode = "CR",
        statusCode = ReleaseStatus.Code.VERIFIED,
      ),
    )

    val request = existing.syncRelease().copy(releaseDate = LocalDate.now().plusDays(10), verified = false)
    val user = syncUser()
    val res = sendRelease(existing.person.identifier, request, user).successResponse<ReferenceId>()

    val saved = requireNotNull(findRelease(res.dpsId))
    assertThat(saved.status.code).isEqualTo(ReleaseStatus.Code.UNVERIFIED.name)
    saved verifyAgainst request

    verifyAudit(
      saved,
      RevisionType.MOD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get()
        .copy(username = user.username, caseloadId = user.activeCaseloadId, source = DataSource.NOMIS),
    )

    verifyEventPublications(
      saved,
      setOf(ReleaseUnverified(saved.person.identifier, saved.id, DataSource.NOMIS).publication(saved.id)),
    )
  }

  private fun sendRelease(
    personIdentifier: String,
    request: SyncRelease,
    syncUser: SyncUser,
    role: String? = Roles.LEGACY_SYNC,
  ) = webTestClient
    .put()
    .uri(SYNC, personIdentifier)
    .bodyValue(syncReleaseRequest(request, syncUser))
    .headers(setAuthorisation(username = SYSTEM_USERNAME, roles = listOfNotNull(role)))
    .exchange()

  companion object {
    const val SYNC = "/sync/releases/{personIdentifier}"
  }
}
