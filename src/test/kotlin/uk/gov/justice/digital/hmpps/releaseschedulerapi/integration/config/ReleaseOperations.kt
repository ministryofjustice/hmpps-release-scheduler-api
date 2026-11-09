package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config

import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.PersonSummary
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.ReleaseRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.RdProvider
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.ApprovedReleaseDate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.ApprovedReleaseType
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.AutoReleaseDate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.AutoReleaseType
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.GroupedReleaseDates
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.personIdentifier
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.DataGenerator.prisonCode
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config.PersonSummaryOperations.Companion.personSummary
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

typealias PersonProvider = (String, String) -> PersonSummary
typealias ReleaseProvider = (PersonProvider, RdProvider) -> Release

interface ReleaseOperations {
  fun findRelease(uuid: UUID): Release?
  fun givenRelease(rp: ReleaseProvider): Release
}

class ReleaseOperationsImpl(
  private val transactionTemplate: TransactionTemplate,
  private val rdRepository: ReferenceDataRepository,
  private val releaseRepository: ReleaseRepository,
  private val psOperations: PersonSummaryOperations,
) : ReleaseOperations {
  override fun findRelease(uuid: UUID): Release? = releaseRepository.findByIdOrNull(uuid)

  override fun givenRelease(rp: ReleaseProvider) = transactionTemplate.execute {
    releaseRepository.save(
      rp(
        { personIdentifier, prisonCode ->
          psOperations.findPersonSummary(personIdentifier)
            ?: psOperations.givenPersonSummary(personSummary(personIdentifier, prisonCode = prisonCode))
        },
        rdRepository.rdProvider(),
      ),
    )
  }

  companion object {
    fun autoDate(
      date: LocalDate,
      type: AutoReleaseType = AutoReleaseType.entries.random(),
    ) = AutoReleaseDate(type, date)

    fun approvedDate(
      date: LocalDate,
      type: ApprovedReleaseType = ApprovedReleaseType.entries.random(),
    ) = ApprovedReleaseDate(type, date)

    fun groupedDates(
      autoDates: List<AutoReleaseDate> = listOf(),
      approvedDates: List<ApprovedReleaseDate> = listOf(),
    ) = GroupedReleaseDates(autoDates, approvedDates)

    fun release(
      personIdentifier: String = personIdentifier(),
      prisonCode: String = prisonCode(),
      statusCode: ReleaseStatus.Code = ReleaseStatus.Code.PENDING,
      reasonCode: String = "CR",
      confirmedReleaseDate: LocalDate? = null,
      comments: String? = null,
      groupedDates: GroupedReleaseDates? = null,
      anchorDateTime: LocalDateTime = LocalDateTime.now(),
      legacyId: Long? = null,
    ): ReleaseProvider = { per, rd ->
      Release(
        per(personIdentifier, prisonCode),
        rd.get(statusCode.name),
        rd.get(reasonCode),
        confirmedReleaseDate,
        comments,
        groupedDates,
        anchorDateTime,
        legacyId,
      )
    }
  }
}
