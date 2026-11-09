package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.search

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.domain.Sort.by
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.PersonSummary
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.Release
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import java.time.LocalDate
import kotlin.reflect.KClass

@ValidReleaseSearch
@ValidStartAndEnd
@ValidDateRange(31)
data class ReleaseSearchRequest(
  override val start: LocalDate? = null,
  override val end: LocalDate? = null,
  @field:Schema(requiredMode = NOT_REQUIRED)
  val statusCodes: Set<ReleaseStatus.Code> = setOf(),
  val query: String? = null,
  override val page: Int = 1,
  override val size: Int = 10,
) : PagedRequest,
  StartAndEnd {

  override fun sort(): Sort = by(Direction.ASC, "${PERSON}_${LAST_NAME}", "${PERSON}_${FIRST_NAME}", "${PERSON}_$IDENTIFIER")

  companion object {
    private val PERSON = Release::person.name
    private val IDENTIFIER = PersonSummary::identifier.name
    private val FIRST_NAME = PersonSummary::firstName.name
    private val LAST_NAME = PersonSummary::lastName.name
  }
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ReleaseSearchValidator::class])
annotation class ValidReleaseSearch(
  val message: String = "Either start and end must be completed or provide a query string",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Any>> = [],
)

class ReleaseSearchValidator : ConstraintValidator<ValidReleaseSearch, ReleaseSearchRequest> {
  override fun isValid(request: ReleaseSearchRequest, context: ConstraintValidatorContext): Boolean = with(request) {
    return (query.isNullOrBlank() && start != null && end != null) || (!query.isNullOrBlank() && start == null && end == null)
  }
}
