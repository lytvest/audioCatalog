package ru.lytvest.audiocatalog.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import ru.lytvest.audiocatalog.model.Book

interface BookRepository : JpaRepository<Book, Long> {

    fun findBookByNameAndAuthor(name: String?, author: String?): Book?

    fun findBooksBy(pageable: Pageable): List<Book>

    fun findBooksByAudioRatingGreaterThanEqual(rating: Double, pageable: Pageable): List<Book>
}