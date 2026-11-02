package com.aistudio.cinefree.ovqptw.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.*
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aistudio.cinefree.ovqptw.data.Config
import com.aistudio.cinefree.ovqptw.data.local.FallbackCatalog
import com.aistudio.cinefree.ovqptw.data.model.*
import com.aistudio.cinefree.ovqptw.data.remote.RetrofitClient
import com.aistudio.cinefree.ovqptw.data.repository.FirestoreRepository
import com.aistudio.cinefree.ovqptw.data.repository.MovieRepository
import com.aistudio.cinefree.ovqptw.ui.theme.CinemaRed
import com.aistudio.cinefree.ovqptw.ui.theme.DeepBlack
import com.aistudio.cinefree.ovqptw.ui.theme.SurfaceGray
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: String,
    mediaType: String = "movie",
    navController: NavController,
    onPlayClick: (Movie, String, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestoreRepo = remember { FirestoreRepository(context) }
    val movieRepo = remember { MovieRepository(RetrofitClient.tmdbService) }

    var movie by remember { mutableStateOf<Movie?>(FallbackCatalog.findMovie(movieId)) }
    var isLoading by remember { mutableStateOf(movie == null) }
    var isInWatchlist by remember { mutableStateOf(false) }
    var selectedServerId by remember { mutableStateOf("vidlink") }
    var season by remember { mutableIntStateOf(1) }
    var episode by remember { mutableIntStateOf(1) }

    val reviews by firestoreRepo.observeReviews(movieId).collectAsState(initial = emptyList())
    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewRating by remember { mutableFloatStateOf(5.0f) }
    var reviewComment by remember { mutableStateOf("") }

    val watchlistItems by firestoreRepo.observeWatchlist().collectAsState(initial = emptyList())
    LaunchedEffect(watchlistItems, movieId) {
        isInWatchlist = watchlistItems.any { it.movieId == movieId }
    }

    LaunchedEffect(movieId) {
        isLoading = true
        try {
            val details = movieRepo.getMovieDetails(movieId, mediaType)
            movie = details
        } catch (e: Exception) {
            // Fallback loaded
        } finally {
            isLoading = false
        }
    }

    val currentMovie = movie ?: FallbackCatalog.findMovie(movieId) ?: Movie(
        id = movieId,
        title = "CineFree Movie",
        overview = "Streaming video and metadata available via CINEFREE by OXA STUDIOS.",
        posterPath = null,
        backdropPath = null,
        voteAverage = 8.5,
        releaseDate = "2026",
        mediaType = mediaType
    )

    val servers = StreamProviders.buildServers(currentMovie.id, currentMovie.mediaType, season, episode)

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(currentMovie.title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("CINEFREE BY OXA STUDIOS", color = CinemaRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            if (isInWatchlist) {
                                firestoreRepo.removeFromWatchlist(currentMovie.id)
                                Toast.makeText(context, "Removed from My List", Toast.LENGTH_SHORT).show()
                            } else {
                                firestoreRepo.addToWatchlist(
                                    WatchlistItem(
                                        movieId = currentMovie.id,
                                        title = currentMovie.title,
                                        posterPath = currentMovie.posterPath,
                                        mediaType = currentMovie.mediaType
                                    )
                                )
                                Toast.makeText(context, "Added to My List", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Watchlist",
                            tint = if (isInWatchlist) CinemaRed else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack.copy(alpha = 0.95f))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    val backdropUrl = if (!currentMovie.backdropPath.isNullOrEmpty()) {
                        "${Config.TMDB_IMAGE_BASE_ORIGINAL}${currentMovie.backdropPath}"
                    } else if (!currentMovie.posterPath.isNullOrEmpty()) {
                        "${Config.TMDB_IMAGE_BASE_ORIGINAL}${currentMovie.posterPath}"
                    } else {
                        "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=1200&auto=format&fit=crop"
                    }

                    AsyncImage(
                        model = backdropUrl,
                        contentDescription = currentMovie.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, DeepBlack.copy(alpha = 0.7f), DeepBlack),
                                    startY = 60f
                                )
                            )
                    )

                    FloatingActionButton(
                        onClick = { onPlayClick(currentMovie, selectedServerId, season, episode) },
                        containerColor = CinemaRed,
                        contentColor = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(36.dp))
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentMovie.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Surface(
                            color = CinemaRed,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "FULL MOVIE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f TMDB", currentMovie.voteAverage),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (!currentMovie.releaseDate.isNullOrEmpty()) {
                            Text(
                                text = " • ${currentMovie.releaseDate.take(4)}",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onPlayClick(currentMovie, selectedServerId, season, episode) },
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Watch Movie", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val downloadUrl = StreamProviders.getDownloadUrl(currentMovie.id, currentMovie.mediaType, season, episode)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Select Streaming Node",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Powered by OXA Studios multi-CDN infrastructure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        servers.forEach { s ->
                            val isSelected = s.id == selectedServerId
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CinemaRed.copy(alpha = 0.2f) else SurfaceGray,
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(CinemaRed, CinemaRed))) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedServerId = s.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedServerId = s.id },
                                        colors = RadioButtonDefaults.colors(selectedColor = CinemaRed)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(s.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text(s.badge, color = if (isSelected) CinemaRed else Color.LightGray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (currentMovie.mediaType == "tv") {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Season & Episode", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Season: $season", color = Color.White)
                                IconButton(onClick = { if (season > 1) season-- }) { Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White) }
                                IconButton(onClick = { season++ }) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Episode: $episode", color = Color.White)
                                IconButton(onClick = { if (episode > 1) episode-- }) { Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White) }
                                IconButton(onClick = { episode++ }) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Synopsis", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentMovie.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCCCCCC),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    AdMobBannerAd()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerScreen(
    movieId: String,
    serverId: String = "vidlink",
    mediaType: String = "movie",
    season: Int = 1,
    episode: Int = 1,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var currentServerId by remember { mutableStateOf(serverId) }
    var currentSeason by remember { mutableIntStateOf(season) }
    var currentEpisode by remember { mutableIntStateOf(episode) }
    var pageProgress by remember { mutableIntStateOf(0) }
    var isPlayerLoading by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isHudVisible by remember { mutableStateOf(true) }
    var isLandscapeMode by remember { mutableStateOf(true) }

    val servers = StreamProviders.buildServers(movieId, mediaType, currentSeason, currentEpisode)
    val activeServer = servers.find { it.id == currentServerId } ?: servers.first()
    val embedUrl = activeServer.embedUrl

    // Auto landscape orientation & immersive fullscreen setup
    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.window?.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            activity?.window?.decorView?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }

        onDispose {
            activity?.requestedOrientation = originalOrientation
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity?.window?.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                @Suppress("DEPRECATION")
                activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }

    // Auto-hide HUD after 4 seconds of inactivity
    LaunchedEffect(isHudVisible) {
        if (isHudVisible) {
            delay(4000)
            isHudVisible = false
        }
    }

    BackHandler {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { isHudVisible = !isHudVisible }
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewInstance = this
                    setBackgroundColor(android.graphics.Color.BLACK)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Enable audio and cookies
                    val webView = this
                    CookieManager.getInstance().apply {
                        setAcceptCookie(true)
                        setAcceptThirdPartyCookies(webView, true)
                    }

                    // Request audio focus
                    val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
                    audioManager?.requestAudioFocus(
                        { },
                        android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.AUDIOFOCUS_GAIN
                    )

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mediaPlaybackRequiresUserGesture = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                        setSupportMultipleWindows(false)
                        javaScriptCanOpenWindowsAutomatically = false
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            pageProgress = newProgress
                            if (newProgress >= 85) {
                                isPlayerLoading = false
                            }
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isPlayerLoading = false
                            // Comprehensive audio unmuting script for main page and all iframe documents
                            val forceAudioScript = """
                                (function() {
                                    function unmuteAll() {
                                        const mediaElements = document.querySelectorAll('video, audio');
                                        mediaElements.forEach(el => {
                                            el.muted = false;
                                            el.volume = 1.0;
                                            el.play().catch(e => {});
                                        });
                                        const iframes = document.querySelectorAll('iframe');
                                        iframes.forEach(iframe => {
                                            try {
                                                const innerDoc = iframe.contentDocument || iframe.contentWindow.document;
                                                if (innerDoc) {
                                                    innerDoc.querySelectorAll('video, audio').forEach(el => {
                                                        el.muted = false;
                                                        el.volume = 1.0;
                                                        el.play().catch(e => {});
                                                    });
                                                }
                                            } catch(err) {}
                                        });
                                    }
                                    unmuteAll();
                                    setInterval(unmuteAll, 2000);
                                })();
                            """.trimIndent()
                            view?.evaluateJavascript(forceAudioScript, null)
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: return false
                            val scheme = request.url?.scheme?.lowercase() ?: ""
                            if (scheme != "http" && scheme != "https") return true
                            val host = request.url?.host?.lowercase() ?: ""
                            val knownAdHosts = listOf("adsterra", "popads", "bet365", "1xbet", "propeller")
                            if (knownAdHosts.any { host.contains(it) }) return true
                            return false
                        }
                    }

                    loadUrl(embedUrl)
                }
            },
            update = { webView ->
                if (webView.url != embedUrl) {
                    webView.loadUrl(embedUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading Overlay
        if (isPlayerLoading && pageProgress < 85) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CinemaRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Connecting to ${activeServer.name.substringBefore(":")}...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Loading media stream ($pageProgress%)", color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { pageProgress / 100f },
                        color = CinemaRed,
                        trackColor = SurfaceGray,
                        modifier = Modifier.width(180.dp)
                    )
                }
            }
        }

        // Top HUD & Server Switcher with Auto-Hide Animation
        AnimatedVisibility(
            visible = isHudVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.95f), Color.Transparent)))
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Player", tint = Color.White)
                        }
                        Column {
                            Text("CINEFREE PLAYER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("OXA STUDIOS", color = CinemaRed, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Force Unmute Audio Button
                        Button(
                            onClick = {
                                isHudVisible = true
                                val forceUnmuteJs = """
                                    (function() {
                                        document.querySelectorAll('video, audio').forEach(el => {
                                            el.muted = false;
                                            el.volume = 1.0;
                                            el.play();
                                        });
                                        document.querySelectorAll('iframe').forEach(iframe => {
                                            try {
                                                const doc = iframe.contentDocument || iframe.contentWindow.document;
                                                if (doc) {
                                                    doc.querySelectorAll('video, audio').forEach(el => {
                                                        el.muted = false;
                                                        el.volume = 1.0;
                                                        el.play();
                                                    });
                                                }
                                            } catch(e) {}
                                        });
                                    })();
                                """.trimIndent()
                                webViewInstance?.evaluateJavascript(forceUnmuteJs, null)
                                Toast.makeText(context, "🔊 Audio Forced On!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unmute", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Fullscreen / Rotate Toggle Button
                        IconButton(onClick = {
                            isHudVisible = true
                            isLandscapeMode = !isLandscapeMode
                            activity?.requestedOrientation = if (isLandscapeMode) {
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                        }) {
                            Icon(Icons.Default.ScreenRotation, contentDescription = "Rotate Screen", tint = Color.White)
                        }

                        IconButton(onClick = {
                            isHudVisible = true
                            webViewInstance?.reload()
                            Toast.makeText(context, "Reloading player...", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color.White)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    servers.forEach { s ->
                        val isSelected = s.id == currentServerId
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) CinemaRed else SurfaceGray.copy(alpha = 0.9f),
                            modifier = Modifier.clickable {
                                isHudVisible = true
                                if (currentServerId != s.id) {
                                    currentServerId = s.id
                                    isPlayerLoading = true
                                    Toast.makeText(context, "Switched to ${s.name.substringBefore(":")}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text(
                                text = s.name.substringBefore(":"),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
