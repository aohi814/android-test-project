package MainStore

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SearchKeywordStore(private val context: Context) {
    companion object {
        val KEY_SEARCH = stringPreferencesKey("last_search")
    }

    val lastSearch: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[KEY_SEARCH] }

    suspend fun saveSearchKeyword(keyword: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SEARCH] = keyword
        }
    }
}

class PageStore(private val context: Context) {
    companion object {
        val KEY_PAGES = intPreferencesKey("last_pages")
    }

    val lastPages: Flow<Int?> = context.dataStore.data
        .map { preferences -> preferences[KEY_PAGES] }

    suspend fun savePages(pages: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PAGES] = pages
        }
    }
}
