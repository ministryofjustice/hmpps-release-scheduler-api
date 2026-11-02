package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.manageusers

data class UserDetails(
  val username: String,
  val name: String,
)

fun String.asSystemUser() = UserDetails(this, "User $this")
