package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.search

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.access.Roles
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.personIdentifier
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.prisonCode
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.PersonSummaryOperations
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperations
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperationsImpl.Companion.approvedDate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperationsImpl.Companion.autoDate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperationsImpl.Companion.groupedDates
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.ReleaseOperationsImpl.Companion.release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.search.ReleaseSearchRequest
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.search.ReleaseSearchResponse
import java.time.LocalDate

class ReleaseSearchIntTest(
  @Autowired ro: ReleaseOperations,
  @Autowired pso: PersonSummaryOperations,
) : IntegrationTestBase(),
  ReleaseOperations by ro,
  PersonSummaryOperations by pso {
  @Test
  fun `401 unauthorised without a valid token`() {
    webTestClient
      .get()
      .uri(SEARCH_URL, prisonCode())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `403 forbidden without correct role`() {
    searchReleases(prisonCode(), searchRequest(), "ROLE_ANY__OTHER").expectStatus().isForbidden
  }

  @Test
  fun `400 bad request if date range more than 31 days`() {
    val res = searchReleases(prisonCode(), searchRequest(start = LocalDate.now(), end = LocalDate.now().plusDays(32)))
      .errorResponse(HttpStatus.BAD_REQUEST)

    assertThat(res.status).isEqualTo(HttpStatus.BAD_REQUEST.value())
    assertThat(res.userMessage).isEqualTo("Validation failure: Invalid date range")
  }

  @Test
  fun `400 bad request if date range and query are used together`() {
    val res = searchReleases(
      prisonCode(),
      searchRequest(query = personIdentifier(), start = LocalDate.now(), end = LocalDate.now().plusDays(1)),
    ).errorResponse(HttpStatus.BAD_REQUEST)

    assertThat(res.status).isEqualTo(HttpStatus.BAD_REQUEST.value())
    assertThat(res.userMessage).isEqualTo("Validation failure: Either start and end must be completed or provide a query string")
  }

  @Test
  fun `can find releases by confirmed date`() {
    val prisonCode = prisonCode()
    val start = LocalDate.now().plusDays(2)
    val end = start.plusDays(3)
    val releases = (0..5).map {
      val confirmedDate = start.minusDays(1).plusDays(it.toLong())
      givenRelease(
        release(
          prisonCode = prisonCode,
          confirmedReleaseDate = if (it == 4) null else confirmedDate,
          statusCode = when {
            it == 4 -> ReleaseStatus.Code.PENDING
            it % 2 == 0 -> ReleaseStatus.Code.VERIFIED
            else -> ReleaseStatus.Code.UNVERIFIED
          },
        ),
      )
    }

    val res =
      searchReleases(prisonCode, searchRequest(start = start, end = end)).successResponse<ReleaseSearchResponse>()
    assertThat(res.content).hasSize(3)
    assertThat(res.metadata.totalElements).isEqualTo(3)
    assertThat(res.content.map { it.id }).containsExactlyInAnyOrderElementsOf(releases.subList(1, 4).map { it.id })
  }

  @Test
  fun `can find releases by auto release date`() {
    val prisonCode = prisonCode()
    val start = LocalDate.now().plusDays(2)
    val end = start.plusDays(3)
    val releases = (0..5).map {
      val releaseDate = start.minusDays(1).plusDays(it.toLong())
      givenRelease(
        release(
          prisonCode = prisonCode,
          statusCode = ReleaseStatus.Code.PENDING,
          groupedDates = if (it == 4) null else groupedDates(autoDates = listOf(autoDate(releaseDate))),
        ),
      )
    }

    val res =
      searchReleases(prisonCode, searchRequest(start = start, end = end)).successResponse<ReleaseSearchResponse>()
    assertThat(res.content).hasSize(3)
    assertThat(res.metadata.totalElements).isEqualTo(3)
    assertThat(res.content.map { it.id }).containsExactlyInAnyOrderElementsOf(releases.subList(1, 4).map { it.id })
  }

  @Test
  fun `can find releases by approved release date`() {
    val prisonCode = prisonCode()
    val start = LocalDate.now().plusDays(2)
    val end = start.plusDays(3)
    val releases = (0..5).map {
      val releaseDate = start.minusDays(1).plusDays(it.toLong())
      givenRelease(
        release(
          prisonCode = prisonCode,
          statusCode = ReleaseStatus.Code.PENDING,
          groupedDates = if (it == 4) null else groupedDates(approvedDates = listOf(approvedDate(releaseDate))),
        ),
      )
    }

    val res =
      searchReleases(prisonCode, searchRequest(start = start, end = end)).successResponse<ReleaseSearchResponse>()
    assertThat(res.content).hasSize(3)
    assertThat(res.metadata.totalElements).isEqualTo(3)
    assertThat(res.content.map { it.id }).containsExactlyInAnyOrderElementsOf(releases.subList(1, 4).map { it.id })
  }

  @Test
  fun `can filter releases by person identifier`() {
    val prisonCode = prisonCode()
    val toFind = givenRelease(release(prisonCode = prisonCode))
    givenRelease(release(prisonCode = prisonCode))

    val res = searchReleases(prisonCode, searchRequest(query = toFind.person.identifier, start = null, end = null))
      .successResponse<ReleaseSearchResponse>()

    assertThat(res.content).hasSize(1)
    assertThat(res.metadata.totalElements).isEqualTo(1)
    with(res.content.single()) {
      assertThat(id).isEqualTo(toFind.id)
    }
  }

  @Test
  fun `can filter releases by person name`() {
    val prisonCode = prisonCode()
    val toFind = givenRelease(release(prisonCode = prisonCode))
    givenRelease(release(prisonCode = prisonCode))

    toFind.person.nameFormats().forEach {
      val res = searchReleases(prisonCode, searchRequest(query = it, start = null, end = null))
        .successResponse<ReleaseSearchResponse>()

      assertThat(res.content).hasSize(1)
      assertThat(res.metadata.totalElements).isEqualTo(1)
      with(res.content.single()) {
        assertThat(id).isEqualTo(toFind.id)
      }
    }
  }

  private fun searchRequest(
    start: LocalDate? = LocalDate.now(),
    end: LocalDate? = start?.plusDays(30),
    statusCodes: Set<ReleaseStatus.Code> = setOf(),
    query: String? = null,
    page: Int = 1,
    size: Int = 10,
  ): ReleaseSearchRequest = ReleaseSearchRequest(start, end, statusCodes, query, page, size)

  private fun searchReleases(
    prisonCode: String,
    request: ReleaseSearchRequest,
    role: String? = Roles.RELEASE_SCHEDULER_UI,
  ) = webTestClient
    .post()
    .uri(SEARCH_URL, prisonCode)
    .bodyValue(request)
    .headers(setAuthorisation(username = DEFAULT_USERNAME, roles = listOfNotNull(role)))
    .exchange()

  companion object {
    const val SEARCH_URL = "/search/prisons/{prisonCode}/releases"
  }
}
