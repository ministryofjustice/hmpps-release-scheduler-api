package uk.gov.justice.digital.hmpps.releaseschedulerapi.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.releaseschedulerapi.access.Roles
import uk.gov.justice.digital.hmpps.releaseschedulerapi.config.CaseloadIdHeader
import uk.gov.justice.digital.hmpps.releaseschedulerapi.config.OpenApiTags.UI
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.ReleaseDetail
import uk.gov.justice.digital.hmpps.releaseschedulerapi.service.RetrieveReleases
import java.util.UUID

@Tag(name = UI)
@RestController
@RequestMapping("/releases")
@PreAuthorize("hasRole('${Roles.RELEASE_SCHEDULER_UI}')")
class ReleaseDetailController(private val retrieve: RetrieveReleases) {
  @CaseloadIdHeader
  @GetMapping("/{id}")
  fun getReleaseDetails(@PathVariable id: UUID): ReleaseDetail = retrieve.byId(id, checkCaseload = true)
}
