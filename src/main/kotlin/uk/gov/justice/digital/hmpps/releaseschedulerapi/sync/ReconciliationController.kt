package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.releaseschedulerapi.access.Roles
import uk.gov.justice.digital.hmpps.releaseschedulerapi.config.OpenApiTags
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.internal.RetrieveForSync

@Tag(name = OpenApiTags.SYNC)
@RestController
@RequestMapping("reconciliation/releases")
@PreAuthorize("hasRole('${Roles.LEGACY_SYNC}')")
class ReconciliationController(private val retrieve: RetrieveForSync) {
  @GetMapping("/{personIdentifier}")
  fun reconcile(@PathVariable personIdentifier: String): ReconciliationResponse = retrieve.all(personIdentifier)
}

data class ReconciliationResponse(val releases: List<ReconciliationRelease>)
data class ReconciliationRelease(val release: SyncRelease)
