package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.search

import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

interface PagedRequest {
  @get:Parameter(description = "The page to request, starting at 1", example = "1")
  @get:Min(value = 1, message = "Page number must be at least 1")
  val page: Int

  @get:Parameter(description = "The page size to request", example = "10")
  @get:Min(value = 1, message = "Page size must be at least 1")
  val size: Int

  fun buildSort(field: String, direction: Sort.Direction): Sort = Sort.by(direction, field)

  fun sort(): Sort

  fun pageable(): Pageable = PageRequest.of(page - 1, size, sort())
}
