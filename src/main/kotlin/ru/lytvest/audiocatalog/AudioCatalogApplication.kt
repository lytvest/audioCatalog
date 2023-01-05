package ru.lytvest.audiocatalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AudioCatalogApplication

fun main(args: Array<String>) {
	runApplication<AudioCatalogApplication>(*args)
}
