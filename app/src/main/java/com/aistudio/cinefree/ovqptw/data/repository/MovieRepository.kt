package com.aistudio.cinefree.ovqptw.data.repository

import android.util.Log
import com.aistudio.cinefree.ovqptw.data.Config
import com.aistudio.cinefree.ovqptw.data.local.FallbackCatalog
import com.aistudio.cinefree.ovqptw.data.model.Movie
import com.aistudio.cinefree.ovqptw.data.remote.TmdbService

class MovieRepository(
    private val tmdbService: TmdbService
) {
    private val tag = "MovieRepository"

    suspend fun getTrending(): List<Movie> {
        return try {
            val response = tmdbService.getTrendingMovies(Config.currentTmdbKey)
            val list = response.results?.map { it.toMovie() } ?: emptyList()
            if (list.isNotEmpty()) list else FallbackCatalog.trendingMovies
        } catch (e: Exception) {
            Log.w(tag, "Trending fetch failed, using fallback: ${e.message}")
            FallbackCatalog.trendingMovies
        }
    }

    suspend fun getPopular(): List<Movie> {
        return try {
            val response = tmdbService.getPopularMovies(Config.currentTmdbKey)
            val list = response.results?.map { it.toMovie() } ?: emptyList()
            if (list.isNotEmpty()) list else FallbackCatalog.popularMovies
        } catch (e: Exception) {
            Log.w(tag, "Popular fetch failed, using fallback: ${e.message}")
            FallbackCatalog.popularMovies
        }
    }

    suspend fun getTopRated(): List<Movie> {
        return try {
            val response = tmdbService.getTopRatedMovies(Config.currentTmdbKey)
            val list = response.results?.map { it.toMovie() } ?: emptyList()
            if (list.isNotEmpty()) list else FallbackCatalog.popularMovies.reversed()
        } catch (e: Exception) {
            Log.w(tag, "TopRated fetch failed, using fallback: ${e.message}")
            FallbackCatalog.popularMovies.reversed()
        }
    }

    suspend fun getTvSeries(): List<Movie> {
        return try {
            val response = tmdbService.getTrendingTv(Config.currentTmdbKey)
            val list = response.results?.map { it.toMovie() } ?: emptyList()
            if (list.isNotEmpty()) list else FallbackCatalog.tvSeries
        } catch (e: Exception) {
            Log.w(tag, "TV fetch failed, using fallback: ${e.message}")
            FallbackCatalog.tvSeries
        }
    }

    suspend fun getActionMovies(): List<Movie> {
        return try {
            val response = tmdbService.discoverByGenre(Config.currentTmdbKey, 28) // Action
            val list = response.results?.map { it.toMovie() } ?: emptyList()
            if (list.isNotEmpty()) list else FallbackCatalog.trendingMovies.filter { it.title.contains("Deadpool") || it.title.contains("Dark Knight") || it.title.contains("Spider") }
        } catch (e: Exception) {
            FallbackCatalog.trendingMovies
        }
    }

    suspend fun getSciFiMovies(): List<Movie> {
        return try {
            val response = tmdbService.discoverByGenre(Config.currentTmdbKey, 878) // Sci-Fi
            val list = response.results?.map { it.toMovie() } ?: emptyList()
            if (list.isNotEmpty()) list else FallbackCatalog.trendingMovies.filter { it.title.contains("Dune") || it.title.contains("Interstellar") }
        } catch (e: Exception) {
            FallbackCatalog.trendingMovies
        }
    }

    suspend fun searchMovies(query: String): List<Movie> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return FallbackCatalog.allCatalog
        return try {
            val response = tmdbService.searchMulti(Config.currentTmdbKey, trimmed)
            val list = response.results?.map { it.toMovie() } ?: emptyList()
            if (list.isNotEmpty()) list else FallbackCatalog.search(trimmed)
        } catch (e: Exception) {
            Log.w(tag, "Search fetch failed, using fallback: ${e.message}")
            FallbackCatalog.search(trimmed)
        }
    }

    suspend fun getMovieDetails(movieId: String, mediaType: String = "movie"): Movie {
        return try {
            if (mediaType == "tv") {
                tmdbService.getTvDetails(movieId, Config.currentTmdbKey).toMovie()
            } else {
                tmdbService.getMovieDetails(movieId, Config.currentTmdbKey).toMovie()
            }
        } catch (e: Exception) {
            Log.w(tag, "Details fetch failed for $movieId, checking local: ${e.message}")
            FallbackCatalog.findMovie(movieId) ?: Movie(
                id = movieId,
                title = "Movie Details",
                overview = "Streaming video and metadata available via CINEFREE MultiEmbed servers.",
                posterPath = null,
                backdropPath = null,
                voteAverage = 8.0,
                releaseDate = "2024",
                mediaType = mediaType
            )
        }
    }
}
