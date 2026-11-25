package uk.gov.justice.digital.hmpps.releaseschedulerapi.sync

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.releaseschedulerapi.access.Roles
import uk.gov.justice.digital.hmpps.releaseschedulerapi.config.OpenApiTags
import uk.gov.justice.digital.hmpps.releaseschedulerapi.sync.internal.ResyncReleases
import java.util.UUID

@Tag(name = OpenApiTags.SYNC)
@RestController
@RequestMapping("resync/releases")
@PreAuthorize("hasRole('${Roles.LEGACY_SYNC}')")
class ResyncController(private val resync: ResyncReleases) {
  @PutMapping("/{personIdentifier}")
  fun resync(
    @PathVariable personIdentifier: String,
    @Valid @RequestBody request: ResyncReleasesRequest,
  ): ResyncResponse = resync.all(personIdentifier, request)
}

data class ResyncReleasesRequest(val releases: List<ResyncRelease>) {
  fun releaseIds(): Pair<Set<Long>, Set<UUID>> {
    val (legacyIds, ids) = releases.map { requireNotNull(it.release.legacyBookingId) to it.release.dpsId }.unzip()
    return legacyIds.toSet() to ids.filterNotNull().toSet()
  }
}

data class ResyncRelease(val release: SyncRelease, val created: AtAndBy, val modified: AtAndBy?)

data class ResyncResponse(val releases: List<ReleaseMapping>)
data class ReleaseMapping(val dpsId: UUID, val legacyBookingId: Long)
