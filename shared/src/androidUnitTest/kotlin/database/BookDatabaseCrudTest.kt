package database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.Assert.*

class BookDatabaseCrudTest {

    private lateinit var driver: JdbcSqliteDriver
    private lateinit var db: AppDatabase
    private lateinit var queries: BookQueries

    @Before
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AppDatabase.Schema.create(driver)
        db = AppDatabase(driver)
        queries = db.bookQueries
    }

    @After
    fun tearDown() {
        driver.close()
    }

    @Test
    fun insertAndSelectByKey() {
        queries.insertBook(
            key = "OL1M",
            title = "KMM Guide",
            author = "Jane Doe",
            imageUrl = "https://x/y.jpg",
            publishedDate = "2023"
        )

        val one = queries.selectByKey("OL1M").executeAsOne()
        assertEquals("KMM Guide", one.title)
        assertEquals("Jane Doe", one.author)
        assertEquals("2023", one.publishedDate)
    }

    @Test
    fun updateDescription() {
        queries.insertBook("OL2M", "Another", "John", null, null)
        queries.updateBookDetails(key = "OL2M", description = "Great book")
        val one = queries.selectByKey("OL2M").executeAsOne()
        assertEquals("Great book", one.description)
    }

    @Test
    fun deleteAllClearsTable() {
        queries.insertBook("OL3M", "A", "B", null, null)
        queries.insertBook("OL4M", "C", "D", null, null)
        assertTrue(queries.selectAll().executeAsList().isNotEmpty())

        queries.deleteAll()
        assertTrue(queries.selectAll().executeAsList().isEmpty())
    }

    @Test(expected = Exception::class)
    fun primaryKeyConflictThrows() {
        queries.insertBook("OL5M", "A", "B", null, null)
        queries.insertBook("OL5M", "A2", "B2", null, null) // should fail
    }
}
