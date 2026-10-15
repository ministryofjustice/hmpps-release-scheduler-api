package uk.gov.justice.digital.hmpps.releaseschedulerapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import uk.gov.justice.digital.hmpps.releaseschedulerapi.config.ServiceConfig

@EnableConfigurationProperties(ServiceConfig::class)
@SpringBootApplication
class ReleaseSchedulerApi

fun main(args: Array<String>) {
  runApplication<ReleaseSchedulerApi>(*args)
}
