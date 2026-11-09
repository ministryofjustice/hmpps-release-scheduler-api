package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.search

interface PagedResponse<T> {
  val content: List<T>
  val metadata: PageMetadata
}

data class PageMetadata(val totalElements: Long)
