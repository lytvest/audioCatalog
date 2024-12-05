package ru.lytvest.audiocatalog.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import ru.lytvest.audiocatalog.dto.BookInfo

@RestController
class BookController {

    @GetMapping("list")
    fun list(): List<BookInfo> {

    }
}
