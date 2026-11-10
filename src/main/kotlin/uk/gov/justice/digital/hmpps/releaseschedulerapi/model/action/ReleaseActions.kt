package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.action

import com.fasterxml.jackson.annotation.JsonTypeInfo
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
sealed interface ReleaseAction : Action<Release> {
  infix fun changes(release: Release): Boolean
}

data class ReleaseActions(val actions: List<ReleaseAction>, val reason: String?)
