package ru.lytvest.audiocatalog.service

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ru.lytvest.audiocatalog.model.Book
import ru.lytvest.audiocatalog.model.BookCheck
import ru.lytvest.audiocatalog.model.Link
import ru.lytvest.audiocatalog.repository.BookCheckRepository
import ru.lytvest.audiocatalog.repository.LinkRepository

@Service
class BookCheckService(
    @Autowired val bookService: BookService,
    @Autowired val bookCheckRepository: BookCheckRepository,
    @Autowired val linkRepository: LinkRepository
) {


    @PostConstruct
    fun work() {
        var page = PageRequest.of(0, 100)
        var countSuccess = 0
        while (true) {
            println("work page ${page.pageNumber} [${page.offset}] count= $countSuccess" )
            val list = bookCheckRepository.findAllBy(page)
            if (list.isEmpty())
                break;

            for(bc in list) {
                val links = linkRepository.findLinksByBook(bc.book)
                if (links.isEmpty())
                    continue
                println("44444444444447777777777 ${bc.book.name}")
                var sum = links.maxOf { calculate(it) }
                if (sum < 0) sum = 0.0
                if (sum > 1) sum = 1.0
                println("----------------------------> $sum")

                bc.linksDownload = true
                bc.audioRating = sum
                bookCheckRepository.save(bc)
                countSuccess++
            }
            bookCheckRepository.flush()
            page = page.next()
        }
    }
    val sites = listOf("audio", "zvooq-knigi.ru", "5knig.club", "slushkin.ru")

    fun calculate(link: Link): Double {
        if (link.href.contains("au-ok.com") || link.href.contains("aume.ru"))
            return 0.0


        val list = (link.book.name.lowercase() + " " + link.book.author.lowercase()).split("[ .,!()#;:?]+".toRegex())
        val query = link.text.lowercase()
        var res = 0.0

        for (site in sites) {
            if (link.href.contains(site))
                res += 0.8
        }

        if (query.contains("аудиокниг") || link.href.contains("mp3")) {
            res += 0.5
        }

        if (query.contains("торрент") || query.contains("torrent") || query.contains("mp3")) {
            res += 0.2
        }
        var words = 0.0
        println("$list ищем в  '$query'")
        for(s in list) {
            if(query.contains(s)){
                words += 1.0 / list.size
            }
        }
        println("итог: $words full:${res * words}")
        return res * words
    }

    fun createEmpty() {
        println("start book check service")
        var count = 0
        for(id in 1L..10000) {
            bookService.getById(id)?.let {
                val bc = BookCheck()
                bc.book = it
                bookCheckRepository.save(bc)
                count ++
            }
        }
        println("save $count book check")

    }

    fun getByBook(book: Book): BookCheck {
        return bookCheckRepository.findByBook(book)
    }

    fun topAudioBooks(): List<Book> {
        return bookService.topBooks().filter { getByBook(it).audioRating > 0.3 }
    }
}