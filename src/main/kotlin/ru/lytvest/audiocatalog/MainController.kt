package ru.lytvest.audiocatalog

import com.samskivert.mustache.Mustache

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import ru.lytvest.audiocatalog.model.Book
import ru.lytvest.audiocatalog.model.Link
import ru.lytvest.audiocatalog.repository.LinkRepository
import ru.lytvest.audiocatalog.service.BookService

import kotlin.math.round
import kotlin.math.roundToInt

@Controller

class MainController(
    val bookService: BookService,
    val mustache: Mustache.Compiler,
    val linkRepository: LinkRepository
                     ) {


    @GetMapping("/{page}")
    fun index(model: Model, @PathVariable page: Int?) = run {
        val books = bookService.topBooks(page ?: 0)


        println("Как начать писать.")

        books.forEach {
            println(it)
        }

        model.addAttribute("list", getViewList(books))
        model.addAttribute("nextLink", "/${(page ?: 0) + 1}")

        mustache(model, "index")
    }

    @GetMapping("/audio/{page}")
    fun audio(model: Model, @PathVariable page: Int?) = run {
        val books = bookService.topAudioBooks(page ?: 0)
        println("count audio books:${books.size}")

        model.addAttribute("list", getViewList(books))
        model.addAttribute("nextLink", "/audio/${(page ?: 0) + 1}")

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

            links = links.map { old -> Link().apply {
                href = old.href.replace("\"", "")
            }}

            if (book.annotation.length > 310) {
                book.annotation = book.annotation.slice(0..307) + "..."
            }

            list += mapOf("book" to book, "links" to links)
        }

        list
    }


}
