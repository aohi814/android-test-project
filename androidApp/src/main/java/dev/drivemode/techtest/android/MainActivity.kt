@file:OptIn(ExperimentalMaterial3Api::class)

package dev.drivemode.techtest.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import dev.drivemode.techtest.*
import kotlinx.coroutines.launch
import MainStore.SearchKeywordStore
import MainStore.PageStore
import database.DatabaseDriverFactory
import kotlinx.coroutines.CancellationException
import network.Connectivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

      setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {

                /**
                 * Initializes the application context and necessary store variables.
                 * Sets up navigation, connectivity checks, database driver, and state management for search keywords and pagination.
                 */
                val navController = rememberNavController()
                val context = LocalContext.current
                val connection = Connectivity(context)
                val driverFactory = DatabaseDriverFactory(context)
                
                val tokenStore = SearchKeywordStore(context)
                val pageStore = PageStore(context)
                var page by remember { mutableIntStateOf(0) } //
                val storedPage by pageStore.lastPages.collectAsState(initial = 0)
                
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(
                            SearchViewModel(driverFactory, connection),
                            navController,
                            page,
                            { page = (storedPage?:0)+1 },
                            {page = 0},
                            {  page == ((storedPage ?: 0)+1) },
                            tokenStore,
                            pageStore
                        )
                    }
                    composable("detail/{key}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("key").orEmpty()
                        DetailScreen(id, DetailViewModel(driverFactory, connection))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(
    searchViewModel: SearchViewModel,
    navController: NavController,
    page: Int,                      // Current page index for pagination.
    onPageChange: () -> Unit,       // Callback to increment the page index when the user reaches the bottom of the list.
    pageInit: ()->Unit,             // Callback to reset the page index, typically used when a new search is initiated.
    nextPage: () -> Boolean, // Callback to fetch the results for the next page.
    tokenStore: SearchKeywordStore,      // Store for managing search keyword persistence.
    pageStore: PageStore            // Store for managing pagination state (current page index).
) {
    var isLoading by remember { mutableStateOf(false) }
    val bookList = remember { mutableStateListOf<BookModel>() }
    val listState = rememberLazyListState()

    val initialWorks = searchViewModel.cachedResult()
    bookList.clear()
    bookList.addAll(initialWorks.books)

    val storedToken by tokenStore.lastSearch.collectAsState(initial = "")
    var token by rememberSaveable { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var showErrorDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val searchAndUpdateBooks = remember(searchViewModel, pageStore) {
        {
            scope.launch {
                if (token.isNotBlank()) {
                    isLoading = true
                    try {
                        val works: Works
                        if(!nextPage()){
                            works = searchViewModel.searchBySubject(token, 0)
                            bookList.clear()
                            bookList.addAll(works.books)
                            pageInit()
                            tokenStore.saveSearchKeyword(token)
                            snackbarHostState.showSnackbar("Loaded at ${works.timeStamp}")
                            listState.scrollToItem(0)
                            pageStore.savePages(0)
                        }
                        else {works = searchViewModel.searchBySubject(token, page)
                            bookList.addAll(works.books)
                            pageStore.savePages(page)
                        }

                    } catch (e: Exception) {
                        showErrorDialog = true
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }
    val hasInitialized = remember { mutableStateOf(false) }
    LaunchedEffect(storedToken) {
        if (!hasInitialized.value && storedToken?.isNotBlank() == true) {
            token = storedToken ?: ""
        }
    }
    // Effect to fetch next page when `page` changes
    LaunchedEffect(page) {
        if (page > 0) {
            if(nextPage()) searchAndUpdateBooks()
        }
    }
// Alert Message; No connection
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text("Please check the internet connection and try again.") },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            SearchBar(
                modifier = Modifier.fillMaxWidth(),
                query = token,
                onQueryChange = { token = it },
                onSearch = { searchAndUpdateBooks() },
                placeholder = { Text("Enter your search query") },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.clickable {
                            searchAndUpdateBooks()
                        },
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                },
                active = false,
                onActiveChange = {},
            ) {}

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(all = 8.dp),
                    ) {
                        bookList.forEach { book ->
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            navController.navigate("detail/${book.key}")
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Image(
                                        painter = rememberImagePainter(
                                            data = book.imageUrl,
                                            builder = {
                                                placeholder(R.drawable.ic_book_24)
                                                error(R.drawable.ic_book_24)
                                            }
                                        ),
                                        contentDescription = "",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(150.dp)
                                            .padding(end = 8.dp)
                                    )
                                    Column {
                                        Text(
                                            text = book.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = book.author,
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(top = 12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // When reaching the end of the list, load more items
                        if (bookList.isNotEmpty() && !isLoading) {
                            item {
                                LaunchedEffect(Unit) {   onPageChange() }
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
internal fun DetailScreen(
    key: String,
    detailViewModel: DetailViewModel,
) {
    var isLoading by remember { mutableStateOf(true) }
    var detailModel by remember { mutableStateOf(DetailModel.EMPTY) }
    var showErrorDialog1 by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = detailViewModel) {

        try {
           detailModel = detailViewModel.getDetails(key)
            isLoading = false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println(e)
            showErrorDialog1 = true
            detailModel = detailViewModel.getDetails(key, false)
            isLoading = false

        } finally {


        }
    }

    Box(
            modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())

        , contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            if (showErrorDialog1) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog1 = false },
                    title = { Text("Error") },
                    text = { Text("Failed to load details. Please check your internet connection and try again.") },
                    confirmButton = {
                        Button(onClick = { showErrorDialog1 = false }) {
                            Text("OK")
                        }
                    }
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Image(
                    painter = rememberImagePainter(
                        data = detailModel.book.imageUrl,
                        builder = {
                            placeholder(R.drawable.ic_book_24)
                            error(R.drawable.ic_book_24)
                        },
                    ),
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp),
                )
                Text(
                    text = detailModel.book.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(16.dp),
                )
                Text(
                    text = "Publish Year:  ${detailModel.publishDate ?: "--"}",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                )
                Text(
                    text = detailModel?.description?.takeIf { it.isNotBlank() } ?: "No content",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
