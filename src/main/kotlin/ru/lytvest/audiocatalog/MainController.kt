package ru.lytvest.audiocatalog

import com.samskivert.mustache.Mustache
import lombok.extern.slf4j.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import ru.lytvest.audiocatalog.model.Book
import ru.lytvest.audiocatalog.repository.LinkRepository
import ru.lytvest.audiocatalog.service.BookService

import kotlin.math.round

@Controller
@Slf4j
class MainController(
    val bookService: BookService,
    val mustache: Mustache.Compiler,
    val linkRepository: LinkRepository
                     ) {


    @GetMapping("/")
    fun index(model: Model) = run {
        val books = bookService.topBooks()


        println("Как начать писать.")

        books.forEach {
            println(it)
        }

        model.addAttribute("list", getViewList(books))

        mustache(model, "index")
    }

    @GetMapping("/audio")
    fun audio(model: Model) = run {
        val books = bookService.topBooks()
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

            var links = linkRepository.findLinksByBook(book)



            if (links.size > 5)
                links = links.subList(0, 5)

            if (book.annotation.length > 310) {
                book.annotation = book.annotation.slice(0..307) + "..."
            }

            list += mapOf("book" to book, "links" to links)
        }

        list
    }


}