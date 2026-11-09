package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.search

import uk.gov.justice.digital.hmpps.releaseschedulerapi.model.ReleaseSummary

data class ReleaseSearchResponse(
  override val content: List<ReleaseSummary>,
  override val metadata: PageMetadata,
) : PagedResponse<ReleaseSummary>
