package uk.gov.justice.digital.hmpps.releaseschedulerapi.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.releaseschedulerapi.access.Roles
import uk.gov.justice.digital.hmpps.releaseschedulerapi.config.OpenApiTags.UI
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.search.ReleaseSearchRequest
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.search.ReleaseSearchResponse
import uk.gov.justice.digital.hmpps.releaseschedulerapi.service.SearchReleases

@Tag(name = UI)
@RestController
@RequestMapping("/search")
@PreAuthorize("hasRole('${Roles.RELEASE_SCHEDULER_UI}')")
class SearchController(private val releases: SearchReleases) {
  @PostMapping("/prisons/{prisonCode}/releases")
  fun findPersonReleases(
    @PathVariable prisonCode: String,
    @Valid @RequestBody request: ReleaseSearchRequest,
  ): ReleaseSearchResponse = releases.findForPrison(prisonCode, request)
}
