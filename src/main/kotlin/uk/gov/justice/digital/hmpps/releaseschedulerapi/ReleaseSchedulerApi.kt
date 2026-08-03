package uk.gov.justice.digital.hmpps.releaseschedulerapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReleaseSchedulerApi

fun main(args: Array<String>) {
  runApplication<ReleaseSchedulerApi>(*args)
}
