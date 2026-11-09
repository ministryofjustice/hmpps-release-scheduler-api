package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.releaseschedulerapi.access.Roles
import uk.gov.justice.digital.hmpps.releaseschedulerapi.config.CaseloadIdHeader
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.IdGenerator.newUuid
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.prisonCode
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperations
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperationsImpl.Companion.release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.ReleaseDetail
import java.util.UUID

class RetrieveReleaseDetailIntTest(
  @Autowired ro: ReleaseOperations,
) : IntegrationTestBase(),
  ReleaseOperations by ro {
  @Test
  fun `401 unauthorised without a valid token`() {
    webTestClient
      .get()
      .uri(RETRIEVAL_URL, newUuid())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `403 forbidden without correct role`() {
    retrieveRelease(newUuid(), role = "ROLE_ANY__OTHER").expectStatus().isForbidden
  }

  @Test
  fun `403 forbidden if caseload does not match current location`() {
    val caseloadId = prisonCode()
    val release = givenRelease(release())
    retrieveRelease(release.id, caseloadId).expectStatus().isForbidden
  }

  @Test
  fun `404 not found with non existent id`() {
    retrieveRelease(newUuid(), caseloadId = prisonCode()).expectStatus().isNotFound
  }

  @Test
  fun `400 bad request if invalid uuid provided`() {
    retrieveRelease("invalid-uuid").expectStatus().isBadRequest
  }

  @Test
  fun `200 ok returns release details`() {
    val release = givenRelease(release())
    val res = retrieveRelease(release.id, release.person.prisonCode).successResponse<ReleaseDetail>()
    res verifyAgainst release
  }

  private fun retrieveRelease(
    id: UUID,
    caseloadId: String? = null,
    role: String? = Roles.RELEASE_SCHEDULER_UI,
  ) = retrieveRelease(id.toString(), caseloadId, role)

  private fun retrieveRelease(
    id: String,
    caseloadId: String? = null,
    role: String? = Roles.RELEASE_SCHEDULER_UI,
  ) = webTestClient
    .get()
    .uri(RETRIEVAL_URL, id)
    .headers(setAuthorisation(username = DEFAULT_USERNAME, roles = listOfNotNull(role)))
    .apply { caseloadId?.let { header(CaseloadIdHeader.NAME, caseloadId) } }
    .exchange()

  companion object {
    const val RETRIEVAL_URL = "/releases/{id}"
  }
}
