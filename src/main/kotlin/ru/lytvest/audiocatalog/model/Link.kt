package ru.lytvest.audiocatalog.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(length = 2000)
    var href: String = ""

    @Column(length = 2000)
    var text: String = ""

    @Column(length = 2000)
    var tags: String? = ""

    var date: LocalDateTime = LocalDateTime.now()

    @ManyToOne
    lateinit var book: Book
    override fun toString(): String {
        return "Link(id=$id, href='$href', text='$text', tags=$tags)"
    }

}