package network

import database.AppDatabase
import database.BookDatabase
import database.BookQueries
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*

class OpenLibraryServiceTest {

    private fun inMemoryQueries(): BookQueries {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AppDatabase.Schema.create(driver)
        val db = AppDatabase(driver)
        return db.bookQueries
    }

    @Test
    fun getBookBySubject_parsesAndCaches() = runTest {
        val subjectJson = """{
          "works": [
            {
              "title": "KMM Basics",
              "key": "/works/OL1M",
              "cover_id": 12345,
              "authors": [{ "name": "Jane Doe" }],
              "first_publish_year": 2023
            }
          ]
        }"""

        val client = HttpClient(MockEngine) {
            engine {
                addHandler { request: HttpRequestData ->
                    assertEquals(
                        "https://openlibrary.org/subjects/kotlin.json?limit=10&offset=0",
                        request.url.toString()
                    )
                    respond(
                        content = subjectJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val queries = inMemoryQueries()
        val bookDb = BookDatabase(queries)
        val service = OpenLibraryService(client, bookDb)

        val works = service.getBookBySubject("kotlin", 0)
        assertEquals(1, works.books.size)
        val first = works.books.first()
        assertEquals("KMM Basics", first.title)
        assertEquals("Jane Doe", first.author)
        assertEquals("OL1M", first.key)
        assertTrue(first.imageUrl.contains("covers.openlibrary.org"))

        val cached = queries.selectByKey("OL1M").executeAsOne()
        assertEquals("KMM Basics", cached.title)
        assertEquals("Jane Doe", cached.author)
        assertEquals("2023", cached.publishedDate)
    }

    @Test
    fun getDetailByBook_parsesStringDescription() = runTest {
        val detailJson = """{
          "title": "KMM Basics",
          "description": "A gentle intro to KMM"
        }"""

        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = detailJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val bookDb = BookDatabase(inMemoryQueries())
        val service = OpenLibraryService(client, bookDb)

        val description = service.getDetailByBook("OL1M")
        assertEquals("A gentle intro to KMM", description)
    }

    @Test
    fun getDetailByBook_parsesObjectDescription() = runTest {
        val detailJson = """{
          "title": "KMM Basics",
          "description": { "value": "Deep dive into KMM" }
        }"""

        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = detailJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val bookDb = BookDatabase(inMemoryQueries())
        val service = OpenLibraryService(client, bookDb)

        val description = service.getDetailByBook("OL1M")
        assertEquals("Deep dive into KMM", description)
    }
}
