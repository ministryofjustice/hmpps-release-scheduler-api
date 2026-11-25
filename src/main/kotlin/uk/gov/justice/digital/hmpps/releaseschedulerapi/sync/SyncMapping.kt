package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.PersonSummary
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.RdProvider
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.exception.ConflictException
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action.ScheduleRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action.UnscheduleRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action.UnverifyRelease
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action.VerifyRelease
import java.time.LocalDate
import java.time.LocalDateTime

fun Release.forSync() = SyncRelease(
  legacyBookingId = legacyBookingId,
  legacySequenceNumber = legacySequenceNumber,
  dpsId = id,
  releaseDate = confirmedReleaseDate,
  reasonCode = reason?.code,
  statusCode = if (confirmedReleaseDate?.isBefore(LocalDate.now()) == true) SyncRelease.EXPIRED else SyncRelease.SCHEDULED,
  verified = status.code == ReleaseStatus.Code.VERIFIED.name,
  comments = comments,
  approvedReleaseDate = approvedReleaseDate,
  autoReleaseDate = autoReleaseDate,
  dtoApprovedDate = null,
  dtoMidTermDate = null,
)

fun Release.updateFrom(request: SyncRelease, person: PersonSummary, rdProvider: RdProvider) = apply {
  if (status.code == ReleaseStatus.Code.COMPLETED.name) {
    throw ConflictException("Unable to edit a completed release.")
  }
  if (request.releaseDate != null && request.reasonCode != null) {
    if (status.code == ReleaseStatus.Code.VERIFIED.name && !request.verified) {
      val unverify = UnverifyRelease(request.releaseDate, request.reasonCode, comments)
      apply(unverify, rdProvider, true)
    } else {
      val schedule = ScheduleRelease(request.releaseDate, request.reasonCode, comments)
      apply(schedule, rdProvider)
    }
    if (request.verified) {
      apply(VerifyRelease, rdProvider)
    }
  }
  if (status.code != ReleaseStatus.Code.PENDING.name && (request.releaseDate == null || request.reasonCode == null)) {
    apply(UnscheduleRelease, rdProvider)
  }
}

fun SyncRelease.asEntity(person: PersonSummary, occurredAt: LocalDateTime, rdProvider: RdProvider): Release = Release(
  personSummary = person,
  status = rdProvider.get(dpsStatusCode().name),
  reason = reasonCode?.let { rdProvider.get(it) },
  confirmedReleaseDate = releaseDate,
  comments = comments,
  anchorDate = occurredAt,
  legacyBookingId = legacyBookingId,
  legacySequenceNumber = legacySequenceNumber,
  groupedDates = null,
).overrideApprovedReleaseDate(approvedReleaseDate).overrideAutoReleaseDate(autoReleaseDate)
