package uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate

data class GroupedReleaseDates(val autoDates: List<AutoReleaseDate>, val approvedDates: List<ApprovedReleaseDate>)
