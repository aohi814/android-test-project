package dev.drivemode.techtest

import database.AppDatabase
import database.BookDatabase
import database.DatabaseDriverFactory
import network.Connectivity
import network.HttpClientFactory
import network.OpenLibraryService




class SearchViewModel(private var driverFactory:DatabaseDriverFactory, private var connection: Connectivity) {
    private val httpClient = HttpClientFactory().create()
    private val database = AppDatabase(driverFactory.createDriver())
    private val bookDatabase = BookDatabase(database.bookQueries)
    private val openLibraryService = OpenLibraryService(httpClient, bookDatabase)

    fun cachedResult():Works {
        val books = bookDatabase.getAllBooks()
        var works = Works(
            books= books,
            timeStamp="" )
        return works
    }
    suspend fun searchBySubject(token: String, page: Int): Works {

            println(connection.isOnline())
            if(connection.isOffline()) throw Exception("Connection Error; No Internet!")

            val response = openLibraryService.getBookBySubject(token, page)
            return response
        }



}
