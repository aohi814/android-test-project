package database
import dev.drivemode.techtest.DetailModel
import dev.drivemode.techtest.BookModel


class BookDatabase(private val bookQueries: BookQueries) {
//    private val db = AppDatabase(driverFactory.createDriver())
//    val queries = db.bookQueries

    fun insertBook(key: String, title: String, author: String, imageUrl: String, publishDate: String) {

                bookQueries.insertBook(
                    key = key,
                    title = title,
                    author = author,
                    imageUrl = imageUrl,
                    publishedDate = publishDate, // Fill in if available
                )
            }

    fun addDetails(key: String, description: String) {
        bookQueries.updateBookDetails(
            key = key,
            description = description
        )
    }

    fun getAllBooks(): List<BookModel> {
        return bookQueries.selectAll().executeAsList().map {
            BookModel(
                key = it.key,
                title = it.title,
                author = it.author,
                imageUrl = it.imageUrl ?: ""
            )
        }
    }

    fun getDetailBook(bookKey: String): DetailModel {
       val book = bookQueries.selectByKey(bookKey).executeAsOneOrNull()
        return book?.let {
            DetailModel(
                book = BookModel(
                    key = it.key,
                    title = it.title,
                    author = it.author,
                    imageUrl = it.imageUrl ?: ""
                ),
                description = it.description ?: "",
                publishDate = it.publishedDate ?: ""
            )
        }?: DetailModel.EMPTY
    }

    fun clearBooks() {
        bookQueries.deleteAll()
    }
}