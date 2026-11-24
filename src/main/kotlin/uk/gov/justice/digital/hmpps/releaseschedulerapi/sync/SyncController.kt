package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.releaseschedulerapi.access.Roles
import uk.gov.justice.digital.hmpps.releaseschedulerapi.config.OpenApiTags
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.internal.ReleaseSync
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.internal.RetrieveForSync
import java.util.UUID

@Tag(name = OpenApiTags.SYNC)
@RestController
@RequestMapping("sync")
@PreAuthorize("hasRole('${Roles.LEGACY_SYNC}')")
class SyncController(private val sync: ReleaseSync, private val retrieve: RetrieveForSync) {
  @PutMapping("/releases/{personIdentifier}")
  fun syncRelease(
    @PathVariable personIdentifier: String,
    @RequestBody request: SyncReleaseRequest,
  ): ReferenceId = sync.release(personIdentifier, request)

  @GetMapping("/releases/{id}")
  fun getRelease(@PathVariable id: UUID): ScheduledRelease = retrieve.release(id)
}
