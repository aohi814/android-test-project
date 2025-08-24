package network

expect class Connectivity (context: Any?) {
    fun isOnline(): Boolean
    fun isOffline(): Boolean
}
