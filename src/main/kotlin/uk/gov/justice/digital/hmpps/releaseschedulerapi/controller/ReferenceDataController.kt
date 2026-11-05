package uk.gov.justice.digital.hmpps.releaseschedulerapi.controller

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.releaseschedulerapi.access.Roles
import uk.gov.justice.digital.hmpps.releaseschedulerapi.config.OpenApiTags
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.referencedata.ReferenceDataResponse
import uk.gov.justice.digital.hmpps.releaseschedulerapi.service.ReferenceDataService

@Tag(name = OpenApiTags.UI)
@RestController
@RequestMapping("reference-data")
@PreAuthorize("hasRole('${Roles.RELEASE_SCHEDULER_UI}')")
class ReferenceDataController(private val rdService: ReferenceDataService) {
  @GetMapping("/{domain}")
  fun getDomain(
    @Parameter(
      description = "The reference data domain required. This is case insensitive.",
      schema = Schema(type = "string", allowableValues = ["release-reason", "release-status"]),
    )
    @PathVariable domain: String,
  ): ReferenceDataResponse = rdService.findByDomain(ReferenceDataDomain.Code.of(domain))
}
