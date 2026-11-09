package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration

import org.assertj.core.api.Assertions.assertThat
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.ReleaseDate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.ReleaseDetail
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.ReleaseInformation

infix fun ReleaseInformation.verifyAgainst(release: Release) {
  assertThat(id).isEqualTo(release.id)
  assertThat(person.identifier).isEqualTo(release.person.identifier)
  assertThat(status.code).isEqualTo(release.status.code)
  assertThat(reason?.code).isEqualTo(release.reason?.code)
  assertThat(confirmedReleaseDate).isEqualTo(release.confirmedReleaseDate)
  assertThat(autoReleaseDate).isEqualTo(release.autoReleaseDate)
  assertThat(approvedReleaseDate).isEqualTo(release.approvedReleaseDate)
  if (this is ReleaseDetail) {
    assertThat(comments).isEqualTo(release.comments)
    assertThat(calculatedDates)
      .isEqualTo(release.groupedDates?.let { it.autoDates + it.approvedDates } ?: emptyList<ReleaseDate<*>>())
  }
}
