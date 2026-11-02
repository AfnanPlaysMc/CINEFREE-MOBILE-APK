package com.aistudio.cinefree.ovqptw.ui.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aistudio.cinefree.ovqptw.data.Config
import com.aistudio.cinefree.ovqptw.data.model.Movie
import com.aistudio.cinefree.ovqptw.data.model.WatchlistItem
import com.aistudio.cinefree.ovqptw.data.repository.FirestoreRepository
import com.aistudio.cinefree.ovqptw.ui.components.HeroBanner
import com.aistudio.cinefree.ovqptw.ui.components.MovieGridItem
import com.aistudio.cinefree.ovqptw.ui.components.MovieSection
import com.aistudio.cinefree.ovqptw.ui.theme.CinemaRed
import com.aistudio.cinefree.ovqptw.ui.theme.DeepBlack
import com.aistudio.cinefree.ovqptw.ui.theme.SurfaceGray
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestoreRepo = remember { FirestoreRepository(context) }

    val heroMovie by viewModel.heroMovie.collectAsState()
    val trendingMovies by viewModel.trendingMovies.collectAsState()
    val popularMovies by viewModel.popularMovies.collectAsState()
    val topRatedMovies by viewModel.topRatedMovies.collectAsState()
    val tvSeries by viewModel.tvSeries.collectAsState()
    val actionMovies by viewModel.actionMovies.collectAsState()
    val scifiMovies by viewModel.scifiMovies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val watchlistItems by firestoreRepo.observeWatchlist().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Hero Featured Banner
        heroMovie?.let { hero ->
            item {
                val isInWatchlist = watchlistItems.any { it.movieId == hero.id }
                HeroBanner(
                    movie = hero,
                    isInWatchlist = isInWatchlist,
                    onPlayClick = {
                        navController.navigate("player/${hero.id}?server=vidlink&mediaType=${hero.mediaType}")
                    },
                    onWatchlistClick = {
                        scope.launch {
                            if (isInWatchlist) {
                                firestoreRepo.removeFromWatchlist(hero.id)
                                Toast.makeText(context, "Removed from Watchlist", Toast.LENGTH_SHORT).show()
                            } else {
                                firestoreRepo.addToWatchlist(
                                    WatchlistItem(
                                        movieId = hero.id,
                                        title = hero.title,
                                        posterPath = hero.posterPath,
                                        mediaType = hero.mediaType
                                    )
                                )
                                Toast.makeText(context, "Added to Watchlist", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onDetailsClick = {
                        navController.navigate("details/${hero.id}?mediaType=${hero.mediaType}")
                    }
                )
            }
        }

        // Section: Trending
        item {
            MovieSection(
                title = "🔥 Trending This Week",
                movies = trendingMovies,
                onMovieClick = { movie ->
                    navController.navigate("details/${movie.id}?mediaType=${movie.mediaType}")
                }
            )
        }

        // Section: Popular Blockbusters
        item {
            MovieSection(
                title = "🍿 Popular Blockbusters",
                movies = popularMovies,
                onMovieClick = { movie ->
                    navController.navigate("details/${movie.id}?mediaType=${movie.mediaType}")
                }
            )
        }

        // Section: Top Rated
        item {
            MovieSection(
                title = "⭐ Top Rated Masterpieces",
                movies = topRatedMovies,
                onMovieClick = { movie ->
                    navController.navigate("details/${movie.id}?mediaType=${movie.mediaType}")
                }
            )
        }

        // Section: TV Series
        item {
            MovieSection(
                title = "📺 TV Shows & Series",
                movies = tvSeries,
                onMovieClick = { movie ->
                    navController.navigate("details/${movie.id}?mediaType=tv")
                }
            )
        }

        // Section: Action
        if (actionMovies.isNotEmpty()) {
            item {
                MovieSection(
                    title = "💥 Action & Thrillers",
                    movies = actionMovies,
                    onMovieClick = { movie ->
                        navController.navigate("details/${movie.id}?mediaType=${movie.mediaType}")
                    }
                )
            }
        }

        // Section: Sci-Fi
        if (scifiMovies.isNotEmpty()) {
            item {
                MovieSection(
                    title = "🚀 Sci-Fi & Adventure",
                    movies = scifiMovies,
                    onMovieClick = { movie ->
                        navController.navigate("details/${movie.id}?mediaType=${movie.mediaType}")
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            AdMobBannerAd()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = viewModel()
) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val filteredResults = remember(searchResults, selectedFilter) {
        when (selectedFilter) {
            "movie" -> searchResults.filter { it.mediaType == "movie" }
            "tv" -> searchResults.filter { it.mediaType == "tv" }
            else -> searchResults
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.search(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search any movie, show, or actor...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CinemaRed) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        viewModel.search("")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.LightGray)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceGray,
                unfocusedContainerColor = SurfaceGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = CinemaRed,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("all" to "All Titles", "movie" to "Movies", "tv" to "TV Series").forEach { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setFilter(key) },
                    label = { Text(label, color = if (isSelected) Color.White else Color.LightGray) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CinemaRed,
                        containerColor = SurfaceGray
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CinemaRed)
            }
        } else if (filteredResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No titles found. Try another search term.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredResults, key = { it.id }) { movie ->
                    MovieGridItem(movie = movie) {
                        navController.navigate("details/${movie.id}?mediaType=${movie.mediaType}")
                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestoreRepo = remember { FirestoreRepository(context) }
    val watchlistItems by firestoreRepo.observeWatchlist().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp)
    ) {
        Text(
            text = "My Watchlist",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (watchlistItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your watchlist is empty", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tap the Bookmark icon on any movie to save it here.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(watchlistItems, key = { it.movieId }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable {
                            navController.navigate("details/${item.movieId}?mediaType=${item.mediaType}")
                        }
                    ) {
                        Column {
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
                                val posterUrl = if (!item.posterPath.isNullOrEmpty()) {
                                    "${Config.TMDB_IMAGE_BASE_W500}${item.posterPath}"
                                } else {
                                    "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&auto=format&fit=crop"
                                }
                                AsyncImage(
                                    model = posterUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )

                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            firestoreRepo.removeFromWatchlist(item.movieId)
                                            Toast.makeText(context, "Removed from Watchlist", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = CinemaRed)
                                }
                            }
                            Text(
                                text = item.title,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
