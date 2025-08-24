package dev.drivemode.techtest

import database.AppDatabase
import database.BookDatabase
import database.DatabaseDriverFactory
import network.HttpClientFactory
import network.OpenLibraryService
import network.Connectivity

class DetailViewModel(private var driverFactory:DatabaseDriverFactory, private var connection: Connectivity) {

    private val httpClient = HttpClientFactory().create()
    private val database = AppDatabase(driverFactory.createDriver())
    private val bookDatabase = BookDatabase(database.bookQueries)
    private val openLibraryService = OpenLibraryService(httpClient, bookDatabase)
//    private val connection = Connectivity()

    suspend fun getDetails(key: String, online: Boolean = true): DetailModel {

        // Check if data exists in local DB


        val localDetail = bookDatabase.getDetailBook(key)
        if (localDetail.description.isNotBlank() || !online) {
            return localDetail
        }

        // If not, fetch from network
        println(connection.isOnline())
        if(connection.isOffline()) throw Exception("Connection Error; No Internet!")

        val descriptionFromNetwork = openLibraryService.getDetailByBook(key)

        // Add/Update local DB
        bookDatabase.addDetails(key, descriptionFromNetwork)

        return DetailModel(
            book = localDetail.book, // Assuming book and publishDate are already in local DB or fetched elsewhere
            publishDate = localDetail.publishDate,
            description = descriptionFromNetwork
        )

    }
}
