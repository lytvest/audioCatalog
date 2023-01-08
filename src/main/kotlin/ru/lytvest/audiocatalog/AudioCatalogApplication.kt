package ru.lytvest.audiocatalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.StringHttpMessageConverter
import java.nio.charset.Charset


@SpringBootApplication
class AudioCatalogApplication {

}

fun main(args: Array<String>) {
	runApplication<AudioCatalogApplication>(*args)
}
