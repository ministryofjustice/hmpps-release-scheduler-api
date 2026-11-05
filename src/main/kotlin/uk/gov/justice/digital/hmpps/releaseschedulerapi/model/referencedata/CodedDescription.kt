package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.referencedata

import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceDataDomain

data class CodedDescription(val code: String, val description: String)

fun ReferenceDataDomain.asCodedDescription() = CodedDescription(code.name, description)
fun ReferenceData.asCodedDescription(): CodedDescription = CodedDescription(code, description)
