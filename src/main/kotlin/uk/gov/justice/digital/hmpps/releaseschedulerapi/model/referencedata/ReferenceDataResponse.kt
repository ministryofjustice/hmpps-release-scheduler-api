package uk.gov.justice.digital.hmpps.releaseschedulerapi.model.referencedata

data class ReferenceDataResponse(val domain: CodedDescription, val items: List<CodedDescription>)
