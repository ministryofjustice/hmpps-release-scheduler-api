package uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata

import jakarta.persistence.Cacheable
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Immutable

@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Immutable
@Entity
@Table(name = "release_reason")
class ReleaseReason(
  code: String,
  description: String,
  sequenceNumber: Int,
  active: Boolean,
) : ReferenceData(code, description, sequenceNumber, active)
