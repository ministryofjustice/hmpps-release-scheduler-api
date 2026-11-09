package uk.gov.justice.digital.hmpps.releaseschedulerapi.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.Version
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.IdGenerator.newUuid
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.RdProvider
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseReason
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate.GroupedReleaseDates
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "release")
final class Release(
  personSummary: PersonSummary,
  status: ReleaseStatus,
  reason: ReleaseReason?,
  confirmedReleaseDate: LocalDate?,
  comments: String?,
  groupedDates: GroupedReleaseDates?,
  anchorDate: LocalDateTime,
  legacyId: Long?,
  id: UUID = newUuid(),
) {
  @Id
  @Column(name = "id", nullable = false)
  var id: UUID = id
    private set

  @Version
  @Column(name = "version", nullable = false)
  var version: Int? = null
    private set

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "person_identifier", nullable = false)
  var person: PersonSummary = personSummary
    private set

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "status_code", nullable = false)
  var status: ReleaseStatus = status
    private set

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reason_code", nullable = false)
  var reason: ReleaseReason? = reason
    private set

  @Column(name = "confirmed_release_date")
  var confirmedReleaseDate: LocalDate? = confirmedReleaseDate
    private set

  @Column(name = "auto_release_date")
  var autoReleaseDate: LocalDate? = groupedDates?.autoDates?.maxOfOrNull { it.date }
    private set

  @Column(name = "approved_release_date")
  var approvedReleaseDate: LocalDate? = groupedDates?.approvedDates?.maxOfOrNull { it.date }
    private set

  @Column(name = "comments", length = Integer.MAX_VALUE)
  var comments: String? = comments
    private set

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "grouped_dates")
  var groupedDates: GroupedReleaseDates? = groupedDates
    private set

  @Column(name = "anchor_date_time")
  var anchorDate: LocalDateTime = anchorDate
    private set

  @Column(name = "legacy_id")
  var legacyId: Long? = legacyId
    private set

  @PrePersist
  @PreUpdate
  fun validateState() {
    if (confirmedReleaseDate == null) {
      check(status.code == ReleaseStatus.Code.PENDING.name)
    }
    if (confirmedReleaseDate?.isBefore(LocalDate.now()) == true) {
      check(status.code == ReleaseStatus.Code.COMPLETED.name)
    }
  }

  // for testing in dev until crd endpoints are available
  fun updateReleaseDates(groupedDates: GroupedReleaseDates, rdProvider: RdProvider) = apply {
    val auto = groupedDates.autoDates.maxOfOrNull { it.date }
    val approved = groupedDates.approvedDates.maxOfOrNull { it.date }
    if (auto changes autoReleaseDate) {
      autoReleaseDate = auto
      status = if (currentlyScheduled()) rdProvider.get(ReleaseStatus.Code.UNVERIFIED.name) else status
    }
    if (approved changes approvedReleaseDate) {
      approvedReleaseDate = approved
      status = if (currentlyScheduled()) rdProvider.get(ReleaseStatus.Code.UNVERIFIED.name) else status
    }
  }

  private fun currentlyScheduled(): Boolean = confirmedReleaseDate?.isAfter(LocalDate.now()) == true

  private infix fun LocalDate?.changes(other: LocalDate?): Boolean {
    if (this == other) return false
    if (this == null || other == null) return true
    return isEqual(other)
  }
}
