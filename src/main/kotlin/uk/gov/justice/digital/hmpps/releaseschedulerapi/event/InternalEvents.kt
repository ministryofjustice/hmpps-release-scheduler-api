package uk.gov.justice.digital.hmpps.releaseschedulerapi.event

data class InternalEvents(val toPublish: List<DomainEvent<*>>) {
  constructor(de: DomainEvent<*>) : this(listOf(de))
}

object InternalInformation : AdditionalInformation

data class RefreshActiveReleases(
  override val additionalInformation: InternalInformation,
) : DomainEvent<InternalInformation> {
  override val personReference: PersonReference? = null
  override val eventType: String = EVENT_TYPE
  override val description: String = DESCRIPTION
  override val detailUrl: String? = null

  companion object {
    const val EVENT_TYPE: String = "internal.release-refresh.active"
    const val DESCRIPTION: String = "Pull dates for active prisoners in a all prisons"
  }
}

data class InternalPrisonInformation(
  val prisonCode: String,
) : AdditionalInformation

data class RefreshPrisonReleases(
  override val additionalInformation: InternalPrisonInformation,
) : DomainEvent<InternalPrisonInformation> {
  override val personReference: PersonReference? = null
  override val eventType: String = EVENT_TYPE
  override val description: String = DESCRIPTION
  override val detailUrl: String? = null

  companion object {
    const val EVENT_TYPE: String = "internal.release-refresh.prison"
    const val DESCRIPTION: String = "Pull dates for active prisoners in a given prison"
  }
}
