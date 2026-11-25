package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.sync

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.access.Roles
import uk.gov.justice.digital.hmpps.releaseschedulerapi.context.SchedulerContext.Companion.SYSTEM_USERNAME
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.IdGenerator.newUuid
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperations
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperationsImpl.Companion.release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.SyncRelease
import java.util.UUID

class SyncRetrieveReleaseIntTest(
  @Autowired releaseOps: ReleaseOperations,
) : IntegrationTestBase(),
  ReleaseOperations by releaseOps {
  @Test
  fun `401 unauthorised without a valid token`() {
    webTestClient
      .get()
      .uri(RETRIEVE_RELEASE_URL, newUuid())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `403 forbidden without correct role`() {
    retrieveRelease(newUuid(), role = Roles.RELEASE_SCHEDULER_UI).expectStatus().isForbidden
  }

  @Test
  fun `404 - not found if uuid does not exist`() {
    retrieveRelease(newUuid()).errorResponse(HttpStatus.NOT_FOUND)
  }

  @Test
  fun `400 - bad request if id not a valid uuid`() {
    retrieveRelease("invalid-uuid").errorResponse(HttpStatus.BAD_REQUEST)
  }

  @Test
  fun `200 - can retrieve details of release`() {
    val release = givenRelease(release())
    val res = retrieveRelease(release.id).successResponse<SyncRelease>()
    release verifyAgainst res
  }

  private fun retrieveRelease(
    id: UUID,
    role: String? = Roles.LEGACY_SYNC,
  ) = retrieveRelease(id.toString(), role)

  private fun retrieveRelease(
    id: String,
    role: String? = Roles.LEGACY_SYNC,
  ) = webTestClient
    .get()
    .uri(RETRIEVE_RELEASE_URL, id)
    .headers(setAuthorisation(username = SYSTEM_USERNAME, roles = listOfNotNull(role)))
    .exchange()

  companion object {
    const val RETRIEVE_RELEASE_URL = "/sync/releases/{id}"
  }
}
