package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.releasedates

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.retryOnTransientException

@Service
class ReleaseDateClient(@Qualifier("crdWebClient") private val webClient: WebClient) {
  fun getReleaseDates(personIdentifier: String): ReleaseDates = webClient
    .get()
    .uri(GET_PERSON_RELEASE_DATES, personIdentifier)
    .retrieve()
    .bodyToMono<ReleaseDates>()
    .retryOnTransientException()
    .block()!!

  companion object {
    const val GET_PERSON_RELEASE_DATES = "person/{personIdentifier}/release-dates"
  }
}
