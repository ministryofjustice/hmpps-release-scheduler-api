package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration

import java.util.UUID

object IntegrationUrlBuilder {
  lateinit var baseUrl: String

  fun releaseUrl(id: UUID): String = "$baseUrl/integrations/releases/$id"
}
