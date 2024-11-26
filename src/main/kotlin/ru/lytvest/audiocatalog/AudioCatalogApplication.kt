package ru.lytvest.audiocatalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient


@SpringBootApplication
class AudioCatalogApplication {

	@Bean
	fun restClient(): RestClient {
		return RestClient.create()
	}

}

fun main(args: Array<String>) {
	runApplication<AudioCatalogApplication>(*args)
}
