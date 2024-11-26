package ru.lytvest.audiocatalog

import it.skrape.core.htmlDocument
import it.skrape.selects.html5.*
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import ru.lytvest.audiocatalog.model.Book
import ru.lytvest.audiocatalog.service.BookService
import ru.lytvest.audiocatalog.service.LitresImportService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger


@RestController
class StartController(
    val bookService: BookService,
    val restClient: RestClient,
    val litresImportService: LitresImportService,
) {

    val log = LoggerFactory.getLogger(this.javaClass)

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSX")

    @PostConstruct
    fun init() {


    }

    val count: AtomicInteger = AtomicInteger(0)

    @GetMapping("litres")
    fun litres(@RequestParam page: String?): ResponseEntity<String> {
        val currentPage = page?.toInt() ?: 1
        litresImportService.parsePage("https://www.litres.ru/genre/knigi-fentezi-5018/", currentPage)
        return ResponseEntity.ok().body("ok")
    }

    @GetMapping("testSave")
    fun test(): ResponseEntity<String> {
        var test = Book()
        test.name = "test book"
        test = bookService.saveOrUpdate(test)
        return ResponseEntity.ok().body("" + test)
    }

    @GetMapping("load")
    fun test(@RequestParam page: String?): ResponseEntity<String> {
        var res = "not result"
        page?.let {
            res = getAllFromUrl("https://author.today/work/genre/all?sorting=likes&eg=-&fnd=false&page=${page}")
        }
        return ResponseEntity.ok().body("" + res)
    }

    @GetMapping("run")
    fun run(): ResponseEntity<String> {
        if (count.get() == 0) {
            val thread = Thread {
                println("thread start")
                for (i in 1..400) {
                    count.set(i)
                    try {
                        getAllFromUrl("https://author.today/work/genre/all?sorting=likes&eg=-&fnd=false&page=${i}")
                        Thread.sleep(1000)
                    } catch (e: Exception) {
                        System.err.println(e.message)
                        try {
                            println("attempts 2")
                            getAllFromUrl("https://author.today/work/genre/all?sorting=likes&eg=-&fnd=false&page=${i}")
                            Thread.sleep(1000)
                        } catch (e: Exception) {
                            System.err.println(e.message)
                        }
                    }
                }
                count.set(401)
            }
            thread.start()
            thread.isDaemon = true
        }

        return ResponseEntity.ok().body("" + count.get())
    }

    fun getAllFromUrl(urlSite: String): String {
        println("start download $urlSite")
        val sb = StringBuilder()
        val responseBody = restClient.get()
            .uri(urlSite)
            .retrieve()
            .body(String::class.java) ?: ""

        htmlDocument(responseBody) {
            val images = div {
                withClass = "cover-image"
                img {
                    findAll {
                        map {
                            val s = it.attribute("src")
                            var index = s.indexOfFirst { it == '?' }
                            if (index < 0) index = s.length
                            s.slice(0 until index)
                        }
                    }
                }
            }
            println("картинки [${images.size}] -> $images")
            var i = 0
            div {
                withClass = "book-row-content"
                findAll {
                    forEach {
                        val book = Book()
                        htmlDocument(it.html) {
                            println("-----------------------------------------------------> " + ++i)
                            div {
                                withClass = "book-title"
                                findFirst {
                                    println("Название: $text")
                                    book.name = text
                                }
                                a {
                                    findFirst {
                                        println("id: " + attribute("href"))
                                        book.externalId = attribute("href")
                                    }
                                }
                            }
                            div {
                                withClass = "book-author"
                                findFirst {
                                    println("автор: $text")
                                    book.author = text
                                }
                            }
                            div {
                                withClass = "book-genres"
                                a {
                                    findAll {
                                        println("Жанры " + map { it.text })
                                        book.genres = "" + map { it.text }
                                    }
                                }
                            }
                            span {
                                withClass = "hint-top"
                                withAttributeKey = "data-time"
                                findAll {
                                    println("dates: " + map { it.attribute("data-time") })
                                    try {
                                        book.date = LocalDateTime.parse(map { it.attribute("data-time") }[0], formatter)
                                    } catch (e: Exception) {
                                        System.err.println(e.message)
                                    }
                                }
                            }
                            try {
                                div {
                                    withClass = "book-details"
                                    a {
                                        findAll {
                                            forEach {
                                                println("имя цикла: " + it.text)
                                                book.circle = it.text
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                System.err.println(e.message)
                            }
                            div {
                                withClass = "book-stats"
                                span {
                                    findAll {
                                        val stats = map { it.attribute("data-hint") }
                                        val watch = stats[0].split("·")[1].replace(" ", "").replace(" ", "")
                                        val like = stats[1].split("·")[1].replace(" ", "").replace(" ", "")
                                        val comments = stats[3].split("·")[1].replace(" ", "").replace(" ", "")
                                        println("Просмотры: " + watch.toLong())
                                        println("лайки: " + like.toLong())
                                        println("комменты: " + comments.toLong())
                                        book.watchers = watch.toLong()
                                        book.likes = like.toLong()
                                        book.comments = comments.toLong()
                                    }
                                }
                            }
                            div {
                                withClass = "annotation"
                                findFirst {
                                    println("Аннотация '$text'")
                                    book.annotation = text
                                }
                            }
                        }
                        book.image = images[i - 1]
                        val nBook = bookService.saveOrUpdate(book)
                        sb.append("" + nBook).append("\n")
                    }
                }
            }

        }
        return sb.toString()
    }
}
