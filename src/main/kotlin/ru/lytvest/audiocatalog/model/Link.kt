package ru.lytvest.audiocatalog.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.time.LocalDateTime

@Entity
class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var href: String = ""

    var text: String = ""

    var date: LocalDateTime = LocalDateTime.now()

    @ManyToOne
    lateinit var book: Book
}