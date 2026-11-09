package uk.gov.justice.digital.hmpps.releaseschedulerapi.integration.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.HmppsDomainEventRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.PersonSummaryRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.ReleaseRepository
import uk.gov.justice.digital.hmpps.releaseschedulerapi.domain.referencedata.ReferenceDataRepository

@TestConfiguration
class TestConfig(
  private val transactionTemplate: TransactionTemplate,
  private val hmppsDomainEventRepository: HmppsDomainEventRepository,
  private val rdRepository: ReferenceDataRepository,
  private val releaseRepository: ReleaseRepository,
  private val personSummaryRepository: PersonSummaryRepository,
) {
  @Bean
  fun hmppsDomainEventOperations(): HmppsDomainEventOperations = HmppsDomainEventOperationsImpl(transactionTemplate, hmppsDomainEventRepository)

  @Bean
  fun personSummaryOperations(): PersonSummaryOperations = PersonSummaryOperationsImpl(personSummaryRepository)

  @Bean
  fun releaseOperations(pso: PersonSummaryOperations): ReleaseOperations = ReleaseOperationsImpl(transactionTemplate, rdRepository, releaseRepository, pso)
}
