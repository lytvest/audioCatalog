package ru.lytvest.audiocatalog.dto

data class BookInfo(
    var author: String = "",
    var tags: List<String> = mutableListOf(),

    var series: String = "",
    var name: String = "",

    var watchers: Long = 0,
    var likes: Long = 0,
    var comments: Long = 0,
    var rating: Double = 0.0,


    var hasAudio: Boolean = false,
    var siteType: SiteType = SiteType.unknown,
    var imageLink: String = "",
    var link: String = "",
    var annotation: String = ""

)