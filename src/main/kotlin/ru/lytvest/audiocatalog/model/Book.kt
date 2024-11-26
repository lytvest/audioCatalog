package ru.lytvest.audiocatalog.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

import java.time.LocalDateTime

@Entity
class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var externalId: String = ""

    var author: String = ""

    @Column(length = 1000)
    var genres: String = ""

    var date: LocalDateTime = LocalDateTime.now()

    var circle: String = ""

    @Column(length = 500)
    var name: String = ""

    var watchers: Long = 0

    var likes: Long = 0

    var comments: Long = 0

    @Column(length = 5000)
    var annotation: String = ""

    var baseSite: String = "author.today"

    var image: String = ""

    var lastTimeFindLinks: LocalDateTime? = null

    var countFindLinks: Int = 0

    var audioRating: Double = 0.0

    fun copyFrom(book: Book) {
        for(field in book.javaClass.fields) {
            if (field.name != "id") {
                val value = field.get(book)
                field.set(this, value)
            }
        }
    }

    override fun toString(): String {
        return "Book(id=$id, externalId='$externalId', author='$author', genres='$genres', date=$date, circle='$circle', name='$name', watchers=$watchers, likes=$likes, comments=$comments, annotation='$annotation', baseSite='$baseSite', image='$image')"
    }


}
