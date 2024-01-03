package ru.lytvest.audiocatalog

import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import it.skrape.selects.ElementNotFoundException
import it.skrape.selects.and
import it.skrape.selects.html5.a
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.lytvest.audiocatalog.model.Link
import ru.lytvest.audiocatalog.repository.LinkRepository
import ru.lytvest.audiocatalog.service.BookService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

@Controller
class SearchController(
    @Autowired val bookService: BookService,
    @Autowired val linkRepository: LinkRepository,

    ) {

    val count: AtomicInteger = AtomicInteger(0)


    //    @PostConstruct
    fun init() {
        val text = "Меняя маски скачать торрент"
        val body1 =
            URLEncoder.encode("query=$text&t=&lui=english&sc=UCXnnsGetHKW20&cat=web&abp=-1", StandardCharsets.UTF_8)

        val body2 =
            "query=%D0%BC%D0%B5%D0%BD%D1%8F%D1%8F+%D0%BC%D0%B0%D1%81%D0%BA%D0%B8+%D1%81%D0%BA%D0%B0%D1%87%D0%B0%D1%82%D1%8C+%D1%82%D0%BE%D1%80%D1%80%D0%B5%D0%BD%D1%82&t=&lui=english&sc=UCXnnsGetHKW20&cat=web&abp=-1"
        val body3 = "query=" + URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        ) + "&t=&lui=english&sc=UCXnnsGetHKW20&cat=web&abp=-1"

//        println(body1)
//        println(body2)
//        println(body3)
    }


    @GetMapping("search")
    fun searchSimple(@RequestParam s: String?): ResponseEntity<String> {
        val text = "Меняя маски скачать торрент"
        val res = search(text)
        return ResponseEntity.ok(res.joinToString(separator = " <br> "))
    }

    @GetMapping("start")
    fun searchStart(@RequestParam num: Long): ResponseEntity<String> {
        if (count.get() == 0) {
            Thread {
                for (id in num..num + 60L) {
                    count.set(id.toInt())
                    bookService.getById(id)?.let { book ->
                        try {
                            println("$id ############################################################################")
                            println("find by " + book.name + " " + book.author)
                            val text = book.name + " " + book.author + " аудиокнига торрент"
                            val res = search(text)

                            for (p in res) {
                                val link = Link()
                                link.book = book
                                link.href = p.first
                                link.text = p.second
                                linkRepository.save(link)
                            }
                        } catch (e: Exception) {
                            System.err.println(e.message)
                        }
                        println("wait 60 sec")
                        Thread.sleep(60000)
                    }
                }
            }.start()
        }
        Thread.sleep(6000)
        return ResponseEntity.ok("count -> " + count.get())
    }

    fun search(text: String): List<Pair<String, String>> {
        val apiKey = "AQVNx0plLsyiN5kGe4UHzo-ogU4K2p3qQ8hR8rTm"
        val query = URLEncoder.encode(text, StandardCharsets.UTF_8)
        val urlSearch =
            "https://yandex.ru/search/xml?folderid=b1g6e5qnbphephd58qpk&filter=strict&l10n=ru&apikey=${apiKey}&query=${query}&filter=none"

        val res = skrape(BrowserFetcher) {
            request {
                url = urlSearch
                method = Method.GET
                headers = mapOf(
                    "Accept-Language" to "ru,ru-RU",
                    "Content-Type" to "application/xml",
                    "Authorization" to "Api-Key $apiKey",
                    "accept" to "text/html,application/xhtml+xml,application/xml;"
                )
//                body = URLEncoder.encode("query=$text&t=&lui=english&sc=UCXnnsGetHKW20&cat=web&abp=-1", StandardCharsets.UTF_8)

//                body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
//                        "<request>\n" +
//                        "    <query>menay masky download torrent</query>\n" +
//                        "    <sortby>rlv</sortby>\n" +
//                        "    <groupings>\n" +
//                        "        <groupby attr=\"d\" mode=\"deep\" groups-on-page=\"5\" docs-in-group=\"1\" />\n" +
//                        "    </groupings>\n" +
//                        "    <maxpassages>3</maxpassages>\n" +
//                        "</request>"
                timeout = 15000
            }
            response {
                try {
                    println(responseBody)
                    htmlDocument(responseBody) {
                        "doc" {

                            findAll {
                                map {
                                    println("---------------------------------------------------------------->")
                                    val url = it.children.first { it.tagName == "url" }.text
                                    val title = it.children.first { it.tagName == "title" }.text
                                    println(url)
                                    println(title)



                                    url to title

                                }
                            }
                        }
                    }
                } catch (e: ElementNotFoundException) {
                    println(responseBody)
                    throw e
                }
            }
        }
        return res
    }

    fun search2(text: String): List<Pair<String, String>> {

        val res = skrape(BrowserFetcher) {
            request {
                url = "https://www.startpage.com/sp/search"
                method = Method.POST
                headers = mapOf(
                    "Accept-Language" to "ru,ru-RU",
                    "Content-Type" to "application/x-www-form-urlencoded",
                    "referer" to "https://www.startpage.com/",
                    "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36",
                    "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
                )
//                body = URLEncoder.encode("query=$text&t=&lui=english&sc=UCXnnsGetHKW20&cat=web&abp=-1", StandardCharsets.UTF_8)

                body = "query=" + URLEncoder.encode(
                    text,
                    StandardCharsets.UTF_8
                ) + "&t=&lui=english&sc=UCXnnsGetHKW20&cat=web&abp=-1"
                timeout = 15000
            }
            response {
                try {
//                println(responseBody)
                    htmlDocument(responseBody) {
                        a {
                            withClass = "w-gl__result-title" and "result-link"
                            findAll {
                                map {
                                    println("---------------------------------------------------------------->")
                                    println("href " + it.attribute("href"))
                                    println("text " + it.text)
                                    it.attribute("href") to it.text
                                }
                            }
                        }
                    }
                } catch (e: ElementNotFoundException) {
                    println(responseBody)
                    throw e
                }
            }
        }
        return res
    }
}