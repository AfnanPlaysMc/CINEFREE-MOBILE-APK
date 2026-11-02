package com.aistudio.cinefree.ovqptw.ui.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.aistudio.cinefree.ovqptw.data.Config
import com.aistudio.cinefree.ovqptw.data.local.FallbackCatalog
import com.aistudio.cinefree.ovqptw.data.model.MovieOverride
import com.aistudio.cinefree.ovqptw.data.repository.FirestoreRepository
import com.aistudio.cinefree.ovqptw.ui.theme.CinemaRed
import com.aistudio.cinefree.ovqptw.ui.theme.DeepBlack
import com.aistudio.cinefree.ovqptw.ui.theme.SurfaceGray
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    object Search : Screen("search", "Movies", Icons.Outlined.Movie, Icons.Filled.Movie)
    object Watchlist : Screen("watchlist", "My List", Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark)
    object Profile : Screen("profile", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(userId: String) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Search, Screen.Watchlist, Screen.Profile)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val isPlayerRoute = currentRoute.startsWith("player")

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            if (!isPlayerRoute) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("CF", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "CINEFREE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "BY OXA STUDIOS",
                                    color = CinemaRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            // Surprise pick: pick random movie from fallback catalog
                            val randomMovie = FallbackCatalog.allCatalog.random()
                            navController.navigate("details/${randomMovie.id}?mediaType=${randomMovie.mediaType}")
                        }) {
                            Row(
                                modifier = Modifier
                                    .background(SurfaceGray, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Surprise", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
                )
            }
        },
        bottomBar = {
            if (!isPlayerRoute) {
                NavigationBar(
                    containerColor = DeepBlack,
                    contentColor = Color.White
                ) {
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.icon,
                                    contentDescription = screen.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text(screen.label, fontSize = 11.sp) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CinemaRed,
                                selectedTextColor = CinemaRed,
                                unselectedIconColor = Color.LightGray,
                                unselectedTextColor = Color.LightGray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(if (isPlayerRoute) PaddingValues(0.dp) else innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(Screen.Search.route) {
                SearchScreen(navController)
            }
            composable(Screen.Watchlist.route) {
                WatchlistScreen(navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            composable(
                route = "details/{movieId}?mediaType={mediaType}",
                arguments = listOf(
                    navArgument("movieId") { type = NavType.StringType },
                    navArgument("mediaType") {
                        type = NavType.StringType
                        defaultValue = "movie"
                    }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                val mediaType = backStackEntry.arguments?.getString("mediaType") ?: "movie"
                MovieDetailScreen(
                    movieId = movieId,
                    mediaType = mediaType,
                    navController = navController,
                    onPlayClick = { movie, serverId, season, episode ->
                        navController.navigate("player/${movie.id}?server=$serverId&mediaType=${movie.mediaType}&season=$season&episode=$episode")
                    }
                )
            }
            composable(
                route = "player/{movieId}?server={server}&mediaType={mediaType}&season={season}&episode={episode}",
                arguments = listOf(
                    navArgument("movieId") { type = NavType.StringType },
                    navArgument("server") {
                        type = NavType.StringType
                        defaultValue = "vidlink"
                    },
                    navArgument("mediaType") {
                        type = NavType.StringType
                        defaultValue = "movie"
                    },
                    navArgument("season") {
                        type = NavType.IntType
                        defaultValue = 1
                    },
                    navArgument("episode") {
                        type = NavType.IntType
                        defaultValue = 1
                    }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                val server = backStackEntry.arguments?.getString("server") ?: "vidlink"
                val mediaType = backStackEntry.arguments?.getString("mediaType") ?: "movie"
                val season = backStackEntry.arguments?.getInt("season") ?: 1
                val episode = backStackEntry.arguments?.getInt("episode") ?: 1

                VideoPlayerScreen(
                    movieId = movieId,
                    serverId = server,
                    mediaType = mediaType,
                    season = season,
                    episode = episode,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = Firebase.auth
    val user = auth.currentUser
    val firestoreRepo = remember { FirestoreRepository(context) }

    val watchlistItems by firestoreRepo.observeWatchlist().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            model = user?.photoUrl ?: "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png",
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = user?.displayName ?: "CINEFREE VIP Member",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = user?.email ?: "OXA Studios Authenticated",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceGray),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${watchlistItems.size}", color = CinemaRed, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = "My List", color = Color.LightGray, fontSize = 12.sp)
                }
                Divider(color = Color.DarkGray, modifier = Modifier.height(36.dp).width(1.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "6", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = "Servers", color = Color.LightGray, fontSize = 12.sp)
                }
                Divider(color = Color.DarkGray, modifier = Modifier.height(36.dp).width(1.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Active", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = "Cloud Sync", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = { auth.signOut() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = CinemaRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AdMobBannerAd() {
    val liveUnitId = "ca-app-pub-8435369808017114/9783119032"
    val testUnitId = "ca-app-pub-3940256099942544/6300978111"
    var activeAdUnitId by remember { mutableStateOf(liveUnitId) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        key(activeAdUnitId) {
            androidx.compose.ui.viewinterop.AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    com.google.android.gms.ads.AdView(ctx).apply {
                        setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                        adUnitId = activeAdUnitId
                        adListener = object : com.google.android.gms.ads.AdListener() {
                            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                                android.util.Log.w("AdMob", "AdUnit $activeAdUnitId failed: code ${error.code} (${error.message})")
                                if (activeAdUnitId == liveUnitId) {
                                    // If live production ad unit returns no fill or is pending activation, fallback to test banner so ad is visible
                                    activeAdUnitId = testUnitId
                                }
                            }
                        }
                        loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}

