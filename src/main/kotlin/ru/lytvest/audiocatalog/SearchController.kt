package ru.lytvest.audiocatalog


import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import it.skrape.selects.ElementNotFoundException
import it.skrape.selects.and
import it.skrape.selects.html5.a
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
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
    val log = LoggerFactory.getLogger(this.javaClass)


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
        val res = searchJson(text)
        return ResponseEntity.ok(res.joinToString(separator = " <br><br> "))
    }

    @GetMapping("start")
    fun searchStart(@RequestParam num: Long): ResponseEntity<String> {
        if (count.get() == 0) {
            Thread {
                for (id in num..num + 600L) {
                    count.set(id.toInt())
                    bookService.getById(id)?.let { book ->
                        try {
                            log.info("$id ############################################################################")
                            log.info("find by " + book.name + " " + book.author)
                            val text = book.name + " " + book.author + " аудиокнига торрент"
                            val res = searchJson(text)

                            for (p in res) {
                                val link = Link()
                                link.book = book
                                link.href = p["url"] ?: ""
                                link.text = p["text"] ?: ""
                                link.tags = p["tags"] ?: ""
                                if (link.href.length > 255) {
                                    link.href = link.href.slice(0..252)
                                }
                                log.info("save link $link")
                                linkRepository.save(link)
                            }
                        } catch (e: Exception) {
                            System.err.println(e.message)
                        }
                        log.info("wait 1 sec")
                        Thread.sleep(1000)
                    }
                }
            }.start()
        }
        Thread.sleep(6000)
        return ResponseEntity.ok("count -> " + count.get())
    }

    fun searchJson(text1: String): List<Map<String, String>> {

        val text = text1.replace("#", "")


//        return runBlocking {
//            val client = HttpClient(CIO) {
//                install(ContentNegotiation) {
//                    json(Json {
//                        prettyPrint = true
//                        isLenient = true
//                    })
//                }
//                install(HttpTimeout.Plugin)
//            }
//
//            val response = client.post("https://api.apify.com/v2/acts/apify~google-search-scraper/run-sync-get-dataset-items?token=apify_api_IObP7EwT3kPk4a9N27Qb162TCjEkiL0PYxv9") {
//                contentType(ContentType.Application.Json)
//                setBody(mapOf(
//                    "queries" to text
//                ))
//                timeout { requestTimeoutMillis = 600000 }
//            }
//
//            log.info(response.bodyAsText())
//
//            val map: JsonArray = response.body()
//            val list = mutableListOf<Map<String, String>>()
//            map.first().jsonObject["organicResults"]?.jsonArray?.forEach{
//
//                list += mapOf(
//                    "text" to (it.jsonObject["title"]?.toString() ?: "") + (it.jsonObject["description"]?.toString() ?: ""),
//                    "url" to (it.jsonObject["url"]?.toString() ?: ""),
//                    "tags" to (it.jsonObject["emphasizedKeywords"]?.jsonArray?.joinToString() ?: ""),
//                )
//            }
//
//            list.forEach{
//
//            }
//
//            list
//        }
        return listOf()
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
                    log.info(responseBody)
                    htmlDocument(responseBody) {
                        "doc" {

                            findAll {
                                map {
                                    log.info("---------------------------------------------------------------->")
                                    val url = it.children.first { it.tagName == "url" }.text
                                    val title = it.children.first { it.tagName == "title" }.text
                                    log.info(url)
                                    log.info(title)



                                    url to title

                                }
                            }
                        }
                    }
                } catch (e: ElementNotFoundException) {
                    log.info(responseBody)
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
//                log.info(responseBody)
                    htmlDocument(responseBody) {
                        a {
                            withClass = "w-gl__result-title" and "result-link"
                            findAll {
                                map {
                                    log.info("---------------------------------------------------------------->")
                                    log.info("href " + it.attribute("href"))
                                    log.info("text " + it.text)
                                    it.attribute("href") to it.text
                                }
                            }
                        }
                    }
                } catch (e: ElementNotFoundException) {
                    log.info(responseBody)
                    throw e
                }
            }
        }
        return res
    }
}
