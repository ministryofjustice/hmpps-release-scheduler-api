package uk.gov.justice.digital.hmpps.releaseschedulerapi.model

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.PersonSummary

data class Person(val identifier: String, val firstName: String, val lastName: String)

fun PersonSummary.asPerson() = Person(identifier, firstName, lastName)
