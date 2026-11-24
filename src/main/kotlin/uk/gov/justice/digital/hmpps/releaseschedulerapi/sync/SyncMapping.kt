package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import java.time.LocalDate

fun Release.forSync() = ScheduledRelease(
  legacyBookingId = legacyBookingId,
  legacySequenceNumber = legacySequenceNumber,
  dpsId = id,
  releaseDate = confirmedReleaseDate,
  reasonCode = reason?.code,
  statusCode = if (confirmedReleaseDate?.isBefore(LocalDate.now()) == true) ScheduledRelease.EXPIRED else ScheduledRelease.SCHEDULED,
  verified = status.code == ReleaseStatus.Code.VERIFIED.name,
  comments = comments,
  approvedReleaseDate = approvedReleaseDate,
  autoReleaseDate = autoReleaseDate,
  dtoApprovedDate = null,
  dtoMidTermDate = null,
)
