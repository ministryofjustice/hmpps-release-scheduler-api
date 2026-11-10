package uk.gov.justice.digital.hmpps.releaseschedulerapi.service.history

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.manageusers.ManageUsersClient
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.AuditedAction

@Service
class ReleaseHistoryService(
  entityManager: EntityManager,
  managerUsers: ManageUsersClient,
) : HistoryService<Release>(entityManager, managerUsers) {
  override val entityClass = Release::class.java
  override fun Release.changesFrom(previous: Release): List<AuditedAction.Change> = Release.changeableProperties().mapNotNull {
    val change = it(this).asChangeValue()
    val previous = it(previous).asChangeValue()
    if (change != previous) {
      AuditedAction.Change(it.name, previous, change)
    } else {
      null
    }
  }
}
