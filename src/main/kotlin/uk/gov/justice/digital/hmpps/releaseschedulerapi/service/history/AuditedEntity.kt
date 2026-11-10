package uk.gov.justice.digital.hmpps.releaseschedulerapi.service.history

import org.hibernate.envers.RevisionType
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.AuditRevision

class AuditedEntity<T>(
  val type: RevisionType,
  val revision: AuditRevision,
  val state: T,
)
