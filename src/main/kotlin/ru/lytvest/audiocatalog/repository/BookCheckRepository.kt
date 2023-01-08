package ru.lytvest.audiocatalog.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import ru.lytvest.audiocatalog.model.Book
import ru.lytvest.audiocatalog.model.BookCheck

interface BookCheckRepository : JpaRepository<BookCheck, Long> {

    fun findAllBy(page: Pageable): List<BookCheck>

    fun findByBook(book: Book): BookCheck
}