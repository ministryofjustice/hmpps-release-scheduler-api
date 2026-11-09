package uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.releasedate

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate

sealed interface ReleaseDateType

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "category")
@JsonSubTypes(
  JsonSubTypes.Type(value = AutoReleaseDate::class, name = "AUTO"),
  JsonSubTypes.Type(value = ApprovedReleaseDate::class, name = "APPROVED"),
)
sealed interface ReleaseDate<T : ReleaseDateType> {
  val type: T
  val date: LocalDate
}

enum class AutoReleaseType : ReleaseDateType {
  CRD,
  ARD,
  NPD,
  PRRD,
  MTD,
  DPRRD,
  ;

  companion object {
    fun from(value: String?): AutoReleaseType? = entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
  }
}

enum class ApprovedReleaseType : ReleaseDateType {
  HDCAD,
  APD,
  PRRD,
  LTD,
  ETD,
  ;

  companion object {
    fun from(value: String?): ApprovedReleaseType? = entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
  }
}

data class AutoReleaseDate(
  override val type: AutoReleaseType,
  override val date: LocalDate,
) : ReleaseDate<AutoReleaseType>

data class ApprovedReleaseDate(
  override val type: ApprovedReleaseType,
  override val date: LocalDate,
) : ReleaseDate<ApprovedReleaseType>
