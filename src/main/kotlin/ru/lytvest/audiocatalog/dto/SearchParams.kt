package ru.lytvest.audiocatalog.dto

data class SearchParams(
    var hasAudio: Boolean? = null,
    var search: String? = null,
    var andTags: List<String> = listOf(),
    var orTags: List<String> = listOf(),
    var excludeTags: List<String> = listOf(),
    var page: Int = 0,
    var max: Int = 20,
    var sort: List<Pair<String, String>> = listOf()
)
