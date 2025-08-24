package dev.drivemode.techtest

import kotlinx.serialization.Serializable

@Serializable
data class Works(
    val books: List<BookModel>,
    val timeStamp: String,
)

@Serializable
data class BookModel(
    val title: String,
    val author: String,
    val key: String,
    val imageUrl: String = "",
)

@Serializable
data class DetailModel(
    val book: BookModel,
    val description: String = "",
    val publishDate: String = "",
) {
    companion object {
        val EMPTY = DetailModel(BookModel("", "", "", ""), "", "")
    }
}
