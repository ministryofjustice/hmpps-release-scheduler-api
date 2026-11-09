package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.search

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.PersonSummary

fun PersonSummary.nameFormats(): List<String> = listOf(
  "$firstName $lastName",
  "$lastName $firstName",
  "$lastName,$firstName",
  "$lastName, $firstName",
)
