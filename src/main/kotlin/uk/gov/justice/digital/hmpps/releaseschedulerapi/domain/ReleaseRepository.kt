package uk.gov.justice.digital.hmpps.releaseschedulerapi.domain

import jakarta.persistence.criteria.JoinType
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReleaseStatus
import uk.gov.justice.digital.hmpps.releaseschedulerapi.exception.NotFoundException
import java.time.LocalDate
import java.util.UUID

interface ReleaseRepository :
  JpaRepository<Release, UUID>,
  JpaSpecificationExecutor<Release> {
  fun findByPersonIdentifierAndStatusCodeIn(
    personIdentifier: String,
    statusCode: List<String> = ReleaseStatus.Code.entries.filter { it != ReleaseStatus.Code.COMPLETED }.map { it.name },
  ): Release?
}

fun ReleaseRepository.getRelease(id: UUID) = findByIdOrNull(id) ?: throw NotFoundException("Release not found")

fun statusCodeIn(codes: Set<ReleaseStatus.Code>) = Specification<Release> { rel, _, _ ->
  rel.get<Any>(Release::status.name).get<String>(ReleaseStatus::code.name).`in`(codes.map { it.name })
}

fun matchesPersonIdentifier(personIdentifier: String, prisonCode: String?) = Specification<Release> { rel, _, cb ->
  val person = rel.join<Release, PersonSummary>(Release::person.name, JoinType.INNER)
  cb.and(
    person.matchesIdentifier(cb, personIdentifier),
    prisonCode?.let { person.matchesPrisonCode(cb, it) } ?: cb.conjunction(),
  )
}

fun matchesPersonName(name: String, prisonCode: String) = Specification<Release> { rel, _, cb ->
  val person = rel.join<Release, PersonSummary>(Release::person.name, JoinType.INNER)
  cb.and(
    person.matchesName(cb, name),
    person.matchesPrisonCode(cb, prisonCode),
  )
}

fun matchesPersonPrisonCode(prisonCode: String) = Specification<Release> { rel, _, cb ->
  rel.join<Release, PersonSummary>(Release::person.name, JoinType.INNER).matchesPrisonCode(cb, prisonCode)
}

fun releaseDateBetween(from: LocalDate, to: LocalDate) = Specification<Release> { rel, _, cb ->
  cb.or(
    cb.between(rel.get(Release::confirmedReleaseDate.name), from, to),
    cb.between(rel.get(Release::approvedReleaseDate.name), from, to),
    cb.between(rel.get(Release::autoReleaseDate.name), from, to),
  )
}
