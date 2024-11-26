package ru.lytvest.audiocatalog.model

import jakarta.persistence.*
import org.springframework.beans.BeanUtils
import ru.lytvest.audiocatalog.dto.BookInfo
import ru.lytvest.audiocatalog.dto.SiteType

import java.time.LocalDateTime

@Entity
class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var author: String = ""

    @Column(length = 1000)
    var tags: String = ""

    var date: LocalDateTime = LocalDateTime.now()

    var series: String = ""

    @Column(length = 500)
    var name: String = ""

    var watchers: Long = 0

    var likes: Long = 0

    var comments: Long = 0

    var rating: Double = 0.0

    var hasAudio: Boolean = false

    @Enumerated(EnumType.STRING)
    var siteType: SiteType = SiteType.unknown

    var imageLink: String = ""

    var link: String = ""

    @Column(length = 5000)
    var annotation: String = ""

    fun copyFrom(book: Book) {
        for(field in book.javaClass.fields) {
            if (field.name != "id") {
                val value = field.get(book)
                field.set(this, value)
            }
        }
    }

    fun fillFrom(bookInfo: BookInfo) {
        BeanUtils.copyProperties(bookInfo, this, "tags")
        tags = bookInfo.tags.joinToString(",")
    }

}
