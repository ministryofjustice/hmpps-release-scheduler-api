package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.RdProvider
import uk.gov.justice.digital.hmpps.releaseschedulerapi.event.DomainEvent

interface Action<T> {
  fun applyTo(entity: T, rdProvider: RdProvider)
  fun domainEvent(entity: T): DomainEvent<*>? = null
}
