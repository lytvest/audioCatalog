package ru.lytvest.audiocatalog.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ru.lytvest.audiocatalog.model.Book
import ru.lytvest.audiocatalog.repository.BookRepository
import java.time.LocalDateTime

@Service
class BookService(
   val bookRepository: BookRepository
) {

    fun saveOrUpdate(book: Book): Book {
        val old = bookRepository.findBookByNameAndAuthor(book.name, book.author)
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

    fun topBooks(): List<Book> {
        val page = PageRequest.of(0, 500, Sort.by(Sort.Order.desc("likes")))
        return bookRepository.findBooksBy(page)
    }

    fun getById(id: Long): Book? {
        return bookRepository.findById(id).orElse(null)
    }
}