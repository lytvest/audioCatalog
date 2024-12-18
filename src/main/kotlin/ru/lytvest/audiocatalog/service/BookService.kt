package ru.lytvest.audiocatalog.service

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ru.lytvest.audiocatalog.dto.BookInfo
import ru.lytvest.audiocatalog.dto.SearchParams
import ru.lytvest.audiocatalog.model.Book
import ru.lytvest.audiocatalog.model.Link
import ru.lytvest.audiocatalog.repository.BookRepository
import ru.lytvest.audiocatalog.repository.LinkRepository
import java.time.LocalDateTime

@Service
class BookService(
   val bookRepository: BookRepository,
    val linkRepository: LinkRepository
) {

    val log = LoggerFactory.getLogger(this.javaClass)

    fun saveOrUpdate(bookInfo: BookInfo) {
        saveOrUpdate(Book().apply { fillFrom(bookInfo) })
    }

    fun saveOrUpdate(book: Book): Book {
        val old = bookRepository.findBookByNameAndAuthorAndSiteType(book.name, book.author, book.siteType)
        val book = if (old == null) {
            println("" + LocalDateTime.now() + " save "  + book)
            bookRepository.save(book)
        } else {
            old.copyFrom(book)
            println("" + LocalDateTime.now() + " update "  + old)
            bookRepository.save(old)
        }


        return book
    }

    fun list(params: SearchParams): List<BookInfo> {
        PageRequest.of(params.page, params.max, sortBy(params.sort))
        // TODO:
    }

    fun asc() = Sort.Direction.ASC
    fun desc() = Sort.Direction.DESC

    fun sortBy(list: List<Pair<String, String>>): Sort =
        Sort.by(list.map { Sort.Order(if (it.first == "asc") asc() else desc() , it.second) })

    fun calc(link: Link): Double {
        var score = 0.0


        if (link.tags?.lowercase()?.contains("аудиокниг") == true){
            score += 0.5
        }

        if (link.tags?.lowercase()?.contains("торрент") == true){
            score += 0.1
        }

        if (link.tags?.lowercase()?.contains("torrent") == true){
            score += 0.1
        }

        if (link.text.lowercase().contains("аудиокниг")){
            score += 0.5
        }


        if (link.text.lowercase().contains("торрент")){
            score += 0.1
        }


        if (link.text.lowercase().contains("слушат")){
            score += 0.1
        }
        if (link.text.lowercase().contains("fb2")){
            score -= 0.1
        }
        if (link.text.lowercase().contains("читает")){
            score += 0.2
        }
        if (link.text.lowercase().contains("cлуша")){
            score += 0.2
        }
        if (link.href.lowercase().contains("audiobook")){
            score += 0.2
        }
        if (link.href.lowercase().contains("audio")){
            score += 0.1
        }
        if (link.href.lowercase().contains("au-ok")){
            score -= 0.5
        }
        if (link.href.lowercase().contains("aume")){
            score -= 0.5
        }


        return score
    }

//    @PostConstruct
    fun calcScore() {
        for (id in 1L..2730L) {
            val book = bookRepository.findById(id).get()
            val links = linkRepository.findLinksByBook(book)

            if (links.size <= 0) {
                log.info("book id=${book.id} -> links not found!")
                continue
            }


            var score = links.sumOf { calc(it) } / links.size

            log.info("book id=${book.id} -> score = $score")
            if (score >= 1.0) {
                score = 1.0
            }
            if (links.size <= 1) {
                score = 0.0
            }

            bookRepository.save(book)
        }
    }

    fun topBooks(page: Int): List<Book> {
        val pageR = PageRequest.of(page, 12, Sort.by(Sort.Order.desc("likes")))
        return bookRepository.findBooksBy(pageR)
    }

    fun topAudioBooks(page: Int): List<Book> {

        val pageR = PageRequest.of(page, 12, Sort.by(Sort.Order.desc("likes")))
        return bookRepository.findBooksByHasAudioGreaterThanEqual(true, pageR)
    }

    fun getById(id: Long): Book? {
        return bookRepository.findById(id).orElse(null)
    }
}
