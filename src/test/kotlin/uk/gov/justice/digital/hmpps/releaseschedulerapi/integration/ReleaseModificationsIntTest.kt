package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.hibernate.envers.RevisionType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.access.Roles
import uk.gov.justice.digital.hmpps.releaseschedulerapi.context.SchedulerContext
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.IdGenerator.newUuid
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.publication
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseScheduled
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseUnscheduled
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseUnverified
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.ReleaseVerified
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.prisonCode
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.word
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperations
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperationsImpl.Companion.release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.search.ReleaseSearchIntTest.Companion.SEARCH_URL
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.wiremock.ManageUsersExtension.Companion.manageUsers
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.AuditHistory
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.AuditedAction
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action.ReleaseAction
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action.ReleaseActions
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action.ScheduleRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action.UnscheduleRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action.UnverifyRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action.VerifyRelease
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE
import java.util.UUID

class ReleaseModificationsIntTest(
  @Autowired ro: ReleaseOperations,
) : IntegrationTestBase(),
  ReleaseOperations by ro {
  @Test
  fun `401 unauthorised without a valid token`() {
    webTestClient
      .put()
      .uri(SEARCH_URL, prisonCode())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `403 forbidden without correct role`() {
    applyAction(newUuid(), VerifyRelease, role = "ROLE_ANY__OTHER").expectStatus().isForbidden
  }

  @Test
  fun `409 conflict - cannot verify an unscheduled release`() {
    val release = givenRelease(release())
    val action = VerifyRelease
    val res = applyAction(release.id, action).errorResponse(HttpStatus.CONFLICT)
    assertThat(res.userMessage).isEqualTo("A conflict has been detected")
    assertThat(res.developerMessage).isEqualTo("Cannot verify from PENDING")
  }

  @Test
  fun `409 conflict - cannot schedule an already verified release`() {
    val release = givenRelease(release(confirmedReleaseDate = LocalDate.now(), reasonCode = CR, statusCode = ReleaseStatus.Code.VERIFIED))
    val action = ScheduleRelease(LocalDate.now(), CR, word(10))
    val res = applyAction(release.id, action).errorResponse(HttpStatus.CONFLICT)
    assertThat(res.userMessage).isEqualTo("A conflict has been detected")
    assertThat(res.developerMessage).isEqualTo("Cannot schedule from VERIFIED")
  }

  @Test
  fun `409 conflict - cannot unverify an unscheduled release`() {
    val release = givenRelease(release())
    val action = UnverifyRelease(LocalDate.now(), CR, word(10))
    val res = applyAction(release.id, action).errorResponse(HttpStatus.CONFLICT)
    assertThat(res.userMessage).isEqualTo("A conflict has been detected")
    assertThat(res.developerMessage).isEqualTo("Cannot unverify from PENDING")
  }

  @Test
  fun `409 conflict - cannot unverify a completed release`() {
    val release = givenRelease(release(confirmedReleaseDate = LocalDate.now(), reasonCode = CR, statusCode = ReleaseStatus.Code.COMPLETED))
    val action = UnverifyRelease(LocalDate.now(), CR, word(10))
    val res = applyAction(release.id, action).errorResponse(HttpStatus.CONFLICT)
    assertThat(res.userMessage).isEqualTo("A conflict has been detected")
    assertThat(res.developerMessage).isEqualTo("Cannot unverify from COMPLETED")
  }

  @Test
  fun `can schedule an unscheduled release`() {
    val release = givenRelease(release())
    val action = ScheduleRelease(LocalDate.now().plusDays(1), CR, word(10))
    val user = manageUsers.givenUser()
    val reason = word(20)
    val res = applyAction(release.id, action, reason, user.username).successResponse<AuditHistory>()
    with(res.content.first()) {
      assertThat(this.user.username).isEqualTo(user.username)
      assertThat(this.user.name).isEqualTo(user.name)
      assertThat(this.reason).isEqualTo(reason)
      assertThat(this.domainEvents).containsExactly(ReleaseScheduled.EVENT_TYPE)
      assertThat(this.changes).containsExactly(
        AuditedAction.Change(Release::status.name, "Awaiting details", "Unverified"),
        AuditedAction.Change(Release::reason.name, null, "Conditional release"),
        AuditedAction.Change(Release::confirmedReleaseDate.name, null, ISO_DATE.format(action.confirmedReleaseDate)),
        AuditedAction.Change(Release::comments.name, null, action.comments),
      )
    }

    verifyAudit(
      release,
      RevisionType.MOD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get().copy(username = user.username, reason = reason),
    )

    verifyEventPublications(
      release,
      setOf(ReleaseScheduled(release.person.identifier, release.id).publication(release.id)),
    )
  }

  @Test
  fun `can verify a scheduled release`() {
    val release = givenRelease(release(confirmedReleaseDate = LocalDate.now(), reasonCode = CR, statusCode = ReleaseStatus.Code.UNVERIFIED))
    val action = VerifyRelease
    val user = manageUsers.givenUser()
    val reason = word(20)
    val res = applyAction(release.id, action, reason, user.username).successResponse<AuditHistory>()
    with(res.content.first()) {
      assertThat(this.user.username).isEqualTo(user.username)
      assertThat(this.user.name).isEqualTo(user.name)
      assertThat(this.reason).isEqualTo(reason)
      assertThat(this.domainEvents).containsExactly(ReleaseVerified.EVENT_TYPE)
      assertThat(this.changes).containsExactly(
        AuditedAction.Change(Release::status.name, "Unverified", "Verified"),
      )
    }

    verifyAudit(
      release,
      RevisionType.MOD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get().copy(username = user.username, reason = reason),
    )

    verifyEventPublications(
      release,
      setOf(ReleaseVerified(release.person.identifier, release.id).publication(release.id)),
    )
  }

  @Test
  fun `can unverify a verified release`() {
    val release = givenRelease(release(confirmedReleaseDate = LocalDate.now(), reasonCode = CR, statusCode = ReleaseStatus.Code.VERIFIED, comments = word(10)))
    val action = UnverifyRelease(LocalDate.now().plusDays(1), "HP", word(10))
    val user = manageUsers.givenUser()
    val reason = word(20)
    val res = applyAction(release.id, action, reason, user.username).successResponse<AuditHistory>()
    with(res.content.first()) {
      assertThat(this.user.username).isEqualTo(user.username)
      assertThat(this.user.name).isEqualTo(user.name)
      assertThat(this.reason).isEqualTo(reason)
      assertThat(this.domainEvents).containsExactly(ReleaseUnverified.EVENT_TYPE)
      assertThat(this.changes).containsExactly(
        AuditedAction.Change(Release::status.name, "Verified", "Unverified"),
        AuditedAction.Change(Release::reason.name, "Conditional release", "Final discharge to hospital-psychiatric"),
        AuditedAction.Change(Release::confirmedReleaseDate.name, ISO_DATE.format(release.confirmedReleaseDate), ISO_DATE.format(action.confirmedReleaseDate)),
        AuditedAction.Change(Release::comments.name, release.comments, action.comments),
      )
    }

    verifyAudit(
      release,
      RevisionType.MOD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get().copy(username = user.username, reason = reason),
    )

    verifyEventPublications(
      release,
      setOf(ReleaseUnverified(release.person.identifier, release.id).publication(release.id)),
    )
  }

  @Test
  fun `can unschedule a scheduled release`() {
    val release = givenRelease(release(confirmedReleaseDate = LocalDate.now(), reasonCode = CR, statusCode = ReleaseStatus.Code.UNVERIFIED, comments = word(10)))
    val action = UnscheduleRelease
    val user = manageUsers.givenUser()
    val reason = word(20)
    val res = applyAction(release.id, action, reason, user.username).successResponse<AuditHistory>()
    with(res.content.first()) {
      assertThat(this.user.username).isEqualTo(user.username)
      assertThat(this.user.name).isEqualTo(user.name)
      assertThat(this.reason).isEqualTo(reason)
      assertThat(this.domainEvents).containsExactly(ReleaseUnscheduled.EVENT_TYPE)
      assertThat(this.changes).containsExactly(
        AuditedAction.Change(Release::status.name, "Unverified", "Awaiting details"),
        AuditedAction.Change(Release::reason.name, "Conditional release", null),
        AuditedAction.Change(Release::confirmedReleaseDate.name, ISO_DATE.format(release.confirmedReleaseDate), null),
        AuditedAction.Change(Release::comments.name, release.comments, null),
      )
    }

    verifyAudit(
      release,
      RevisionType.MOD,
      setOf(HmppsDomainEvent::class.simpleName!!, Release::class.simpleName!!),
      SchedulerContext.get().copy(username = user.username, reason = reason),
    )

    verifyEventPublications(
      release,
      setOf(ReleaseUnscheduled(release.person.identifier, release.id).publication(release.id)),
    )
  }

  private fun applyAction(
    id: UUID,
    action: ReleaseAction,
    reason: String? = word(20),
    username: String = DEFAULT_USERNAME,
    role: String? = Roles.RELEASE_SCHEDULER_UI,
  ) = webTestClient
    .put()
    .uri(MODIFICATIONS_URL, id)
    .bodyValue(ReleaseActions(listOf(action), reason))
    .headers(setAuthorisation(username = username, roles = listOfNotNull(role)))
    .exchange()

  companion object {
    const val CR = "CR"
    const val MODIFICATIONS_URL = "/releases/{id}"
  }
}
