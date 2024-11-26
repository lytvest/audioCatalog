package ru.lytvest.audiocatalog.service

import it.skrape.core.htmlDocument
import it.skrape.selects.html5.*
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class LitresImportService(val restClient: RestClient) {

    val log = LoggerFactory.getLogger(this.javaClass)

    fun parsePage(url: String, page: Int): String {
        val res = restClient.get()
            .uri(url, mapOf("page" to page))
            .accept(MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML, MediaType.APPLICATION_XML, MediaType.ALL)
            .headers{
                it["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"
            }.retrieve()
            .body(String::class.java) ?: ""

        log.info(res)
        htmlDocument(res) {
            div {
                withClass = "ArtInfo_wrapper__GoMsb"
            }
        }
        return ""
    }
}
