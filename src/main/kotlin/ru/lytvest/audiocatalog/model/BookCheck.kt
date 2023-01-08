package ru.lytvest.audiocatalog.model

import jakarta.persistence.*


@Entity
class BookCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @OneToOne
    lateinit var book: Book

    var linksDownload: Boolean = false

    var audioRating: Double = 0.0

}