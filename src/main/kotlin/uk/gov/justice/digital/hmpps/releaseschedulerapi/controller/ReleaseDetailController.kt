package uk.gov.justice.digital.hmpps.releaseschedulerapi.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.releaseschedulerapi.access.Roles
import uk.gov.justice.digital.hmpps.releaseschedulerapi.config.CaseloadIdHeader
import uk.gov.justice.digital.hmpps.releaseschedulerapi.config.OpenApiTags.UI
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.AuditHistory
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.ReleaseDetail
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action.ReleaseActions
import uk.gov.justice.digital.hmpps.releaseschedulerapi.service.ReleaseModifications
import uk.gov.justice.digital.hmpps.releaseschedulerapi.service.RetrieveReleases
import uk.gov.justice.digital.hmpps.releaseschedulerapi.service.history.ReleaseHistoryService
import java.util.UUID

@Tag(name = UI)
@RestController
@RequestMapping("/releases")
@PreAuthorize("hasRole('${Roles.RELEASE_SCHEDULER_UI}')")
class ReleaseDetailController(
  private val retrieve: RetrieveReleases,
  private val history: ReleaseHistoryService,
  private val modifications: ReleaseModifications,
) {
  @CaseloadIdHeader
  @GetMapping("/{id}")
  fun getReleaseDetail(@PathVariable id: UUID): ReleaseDetail = retrieve.byId(id, checkCaseload = true)

  @CaseloadIdHeader
  @GetMapping("/{id}/history")
  fun getReleaseHistory(@PathVariable id: UUID): AuditHistory = history.changes(id)

  @CaseloadIdHeader
  @PutMapping("/{id}")
  fun manageReleaseDetail(@PathVariable id: UUID, @Valid @RequestBody request: ReleaseActions): AuditHistory = modifications.apply(id, request)
}
