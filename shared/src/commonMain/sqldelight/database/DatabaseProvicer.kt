package database
import app.cash.sqldelight.db.SqlDriver
expect fun provideSqlDriver(): SqlDriver

object DatabaseProvider {
    val database: AppDatabase by lazy {
        AppDatabase(provideSqlDriver())
    }
}