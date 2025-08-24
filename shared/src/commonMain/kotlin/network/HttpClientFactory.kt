package network


import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.errors.*
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.*

class HttpClientFactory {


    fun create(): HttpClient {
        return HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 10_000
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                },
                    // Workaround for https://youtrack.jetbrains.com/issue/KTOR-4420
                    // and https://youtrack.jetbrains.com/issue/KTOR-4530
                    //
                    // It seems that Ktor can throw JsonConvertException or IOException
                    // with "Illegal input" message when the server returns an invalid JSON.
                    // This is a temporary workaround until the issue is fixed in Ktor.
                    //
                    // It is important to note that this workaround might hide other issues.
                    contentType = io.ktor.http.ContentType.Application.Json)
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            // You can add other configurations like defaultRequest, etc.
        }
    }
}