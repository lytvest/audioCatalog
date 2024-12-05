package ru.lytvest.audiocatalog.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.lytvest.audiocatalog.dto.SiteType
import ru.lytvest.audiocatalog.model.Book

interface BookRepository : JpaRepository<Book, Long> {

    fun findBookByNameAndAuthorAndSiteType(name: String?, author: String?, siteType: SiteType): Book?

    fun findBooksBy(pageable: Pageable): List<Book>

    fun findBooksByHasAudioGreaterThanEqual(hasAudio: Boolean, pageable: Pageable): List<Book>

    @Query("select b from Book b where b.author ilike :search or :search == null")
    fun findByQuery(search: String?, pageable: Pageable): List<Book>
}
