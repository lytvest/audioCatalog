package ru.lytvest.audiocatalog.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.lytvest.audiocatalog.model.Book
import ru.lytvest.audiocatalog.model.Link

interface LinkRepository : JpaRepository<Link, Long> {

    fun findLinksByBook(book: Book): List<Link>
}