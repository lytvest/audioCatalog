package ru.lytvest.audiocatalog.service

import it.skrape.core.htmlDocument
import it.skrape.selects.html5.*
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import ru.lytvest.audiocatalog.dto.BookInfo
import ru.lytvest.audiocatalog.dto.SiteType

@Service
class LitresImportService(val bookService: BookService) {

    val log = LoggerFactory.getLogger(this.javaClass)
    final val baseUrl = "https://www.litres.ru"
    val restClient: RestClient = RestClient.create(baseUrl)

    fun parsePage(page: Int): String {
        val url = "/genre/knigi-fentezi-5018/?page=$page&languages=ru"
        val res = restClient.get()
            .uri(url)
            .accept(MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML, MediaType.APPLICATION_XML, MediaType.ALL)
            .headers{
                it["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"
            }.retrieve()
            .body(String::class.java) ?: ""

//        log.info(res)
        val links = htmlDocument(res) {
            div {
                withClass = "ArtInfo_wrapper__GoMsb"
                a {
                    withAttribute = "data-testid" to "art__title"
                    findAll {
                        map {
                           it.attribute("href")
                        }
                    }
                }
            }
        }
        log.info(links.joinToString("\n"))
        log.info("count {}", links.size)

        for (link in links) {
            val book = parseBook(link)
            log.info("current book {}", book)
            Thread.sleep(10000)
        }

        return ""
    }

    fun parseBook(url: String): BookInfo {
        val book = BookInfo()

        val res = restClient.get()
            .uri(url)
            .accept(MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML, MediaType.APPLICATION_XML, MediaType.ALL)
            .headers{
                it["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"
            }.retrieve()
            .body(String::class.java) ?: ""

        htmlDocument(res) {
            withIgnoreEx("book.name ") {
                div {
                    withClass = "BookCard_book__about__27HAs"

                    h1 {
                        withClass = "BookCard_book__title__EVxWV"
                        findFirst {
                            book.name = text
                        }
                    }
                }
            }
            withIgnoreEx("Несколько авторов") {
                div {
                    withClass = "BookDetailsHeader_persons__YUW15"
                    findFirst {
                        book.author = text.replace("авторы", "").replace("автор", "").trim()
                    }
                }
            }

            if (book.author.isEmpty()) {
                withIgnoreEx("book.author ") {
                    div {
                        withClass = "BookAuthor_author__info__5eDIs"
                        findFirst {
                            book.author = text
                        }
                    }
                }
            }
            withIgnoreEx("book.rating :") {
                div {
                    withClass = "BookFactoids_rating__T12RO"
                    meta {
                        withAttribute = "itemprop" to "ratingValue"
                        findFirst{
                            book.rating = attribute("content").toDouble()
                        }
                    }
                }
            }
            withIgnoreEx("book.likes :") {
                div {
                    withClass = "BookFactoids_rating__T12RO"
                    meta {
                        withAttribute = "itemprop" to "ratingCount"
                        findFirst{
                            book.likes = attribute("content").toLong()
                        }
                    }
                }
            }
            withIgnoreEx("book.comments :") {
                div {
                    withClass = "BookFactoids_reviews__qzxey"
                    div {
                        withClass = "BookFactoids_primary__TVFhL"
                        findFirst {
                            book.comments = text.toLong()
                        }
                    }
                }
            }
            withIgnoreEx("book.imageLink ") {
                img {
                    withClass = "BookCover_img__ncTI5"
                    findFirst {
                        book.imageLink = attribute("src")
                    }
                }
            }
            withIgnoreEx("book.series ") {
                div {
                    withClass = "BookDetailsHeader_series___W4YJ"
                    a {
                        findFirst {
                            book.series = text.replace("«", "").replace("»", "")
                        }
                    }
                }
            }
            withIgnoreEx("book.tags ") {
                div {
                    withClass = "BookGenresAndTags_genresList__rd8vU"
                    a {
                        findAll {
                            book.tags = map {
                                it.text.replace("\"", "")
                            }
                        }
                    }
                }
            }
            withIgnoreEx("book.annotation") {
                div {
                    withClass = "Truncate_truncated__jKdVt"
                    p {
                        findFirst {
                            book.annotation = text
                        }
                    }
                }
            }
        }
        book.siteType = SiteType.litres
        book.link = "$baseUrl$url"
        book.hasAudio = url.startsWith("/audiobook")

        bookService.saveOrUpdate(book)

        return book
    }

    fun <T, R> T.withIgnoreEx(errorInfo: String = "", ff: T.() -> R): R? {
        try {
          return ff(this)
        } catch (e: Exception) {
            log.warn("$errorInfo ${e.message}")
            return null
        }
    }
}
