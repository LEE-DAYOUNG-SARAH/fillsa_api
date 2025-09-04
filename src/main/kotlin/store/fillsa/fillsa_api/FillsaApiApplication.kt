package store.fillsa.fillsa_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class FillsaApiApplication

fun main(args: Array<String>) {
    runApplication<FillsaApiApplication>(*args)
}
