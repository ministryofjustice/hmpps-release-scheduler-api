package uk.gov.justice.digital.hmpps.releaseschedulerapi.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.Predicate
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "person_summary")
final class PersonSummary(
  firstName: String,
  lastName: String,
  prisonCode: String,
  @Id
  @Size(max = 10)
  @Column(name = "person_identifier", nullable = false, length = 10)
  val identifier: String,
) {
  @Version
  @NotNull
  @Column(name = "version", nullable = false)
  var version: Int? = null
    private set

  @Size(max = 64)
  @NotNull
  @Column(name = "first_name", nullable = false, length = 64)
  var firstName: String = firstName
    private set

  @Size(max = 64)
  @NotNull
  @Column(name = "last_name", nullable = false, length = 64)
  var lastName: String = lastName
    private set

  @Size(max = 6)
  @Column(name = "prison_code", nullable = false, length = 6)
  var prisonCode: String = prisonCode
    private set

  fun update(firstName: String, lastName: String, prisonCode: String) = apply {
    if (this.firstName != firstName || this.lastName != lastName || this.prisonCode != prisonCode) {
      this.firstName = firstName
      this.lastName = lastName
      this.prisonCode = prisonCode
    }
  }
}

interface PersonSummaryRepository : JpaRepository<PersonSummary, String>

fun <T> Join<T, PersonSummary>.matchesName(cb: CriteriaBuilder, name: String): Predicate {
  val matches = name.replace(",", " ").split("\\s".toRegex())
    .filter { it.isNotBlank() }
    .map {
      cb.or(
        cb.like(cb.lower(this[PersonSummary::lastName.name]), "%${it.lowercase()}%", '\\'),
        cb.like(cb.lower(this[PersonSummary::firstName.name]), "%${it.lowercase()}%", '\\'),
      )
    }.toTypedArray()
  return cb.and(*matches)
}

fun <T> Join<T, PersonSummary>.matchesIdentifier(
  cb: CriteriaBuilder,
  identifier: String,
): Predicate = cb.equal(get<String>(PersonSummary::identifier.name), identifier.uppercase())

fun <T> Join<T, PersonSummary>.matchesPrisonCode(
  cb: CriteriaBuilder,
  prisonCode: String,
): Predicate = cb.equal(get<String>(PersonSummary::prisonCode.name), prisonCode)
