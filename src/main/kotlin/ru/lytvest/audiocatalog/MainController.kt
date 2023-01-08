package ru.lytvest.audiocatalog

import com.samskivert.mustache.Mustache
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import ru.lytvest.audiocatalog.model.Book
import ru.lytvest.audiocatalog.repository.LinkRepository
import ru.lytvest.audiocatalog.service.BookCheckService
import ru.lytvest.audiocatalog.service.BookService

import java.nio.charset.Charset
import kotlin.math.round

@Controller
class MainController(
    val bookService: BookService,
    val mustache: Mustache.Compiler,
    val bookCheckService: BookCheckService,
    val linkRepository: LinkRepository
                     ) {


    @GetMapping("/")
    fun index(model: Model) = run {
        val books = bookService.topBooks()

        model.addAttribute("list", getViewList(books))

        mustache(model, "index")
    }

    @GetMapping("/audio")
    fun audio(model: Model) = run {
        val books = bookCheckService.topAudioBooks()
        println("count audio books:${books.size}")

        model.addAttribute("list", getViewList(books))

        mustache(model, "index")
    }

    fun mustache(model: Model, name: String) : ResponseEntity<String> {
        val res = mustache.loadTemplate(name).execute(model)
        return ResponseEntity.ok("" + res)
    }

    fun getViewList(books: List<Book>) = run {
        val list = mutableListOf<Map<String, Any>>()

        for (book in books) {
            val check = bookCheckService.getByBook(book)
            var links = linkRepository.findLinksByBook(book)

            check.audioRating = round(check.audioRating * 100)

            if (links.size > 5)
                links = links.subList(0, 5)

            if (book.annotation.length > 310) {
                book.annotation = book.annotation.slice(0..307) + "..."
            }

            list += mapOf("book" to book, "links" to links, "check" to check)
        }

        list
    }


}