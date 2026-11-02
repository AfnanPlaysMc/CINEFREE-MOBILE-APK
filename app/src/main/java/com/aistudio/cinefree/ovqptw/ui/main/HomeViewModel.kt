package com.aistudio.cinefree.ovqptw.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.cinefree.ovqptw.data.local.FallbackCatalog
import com.aistudio.cinefree.ovqptw.data.model.Movie
import com.aistudio.cinefree.ovqptw.data.remote.RetrofitClient
import com.aistudio.cinefree.ovqptw.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = MovieRepository(RetrofitClient.tmdbService)

    private val _heroMovie = MutableStateFlow<Movie?>(FallbackCatalog.trendingMovies.firstOrNull())
    val heroMovie: StateFlow<Movie?> = _heroMovie.asStateFlow()

    private val _trendingMovies = MutableStateFlow<List<Movie>>(FallbackCatalog.trendingMovies)
    val trendingMovies: StateFlow<List<Movie>> = _trendingMovies.asStateFlow()

    private val _popularMovies = MutableStateFlow<List<Movie>>(FallbackCatalog.popularMovies)
    val popularMovies: StateFlow<List<Movie>> = _popularMovies.asStateFlow()

    private val _topRatedMovies = MutableStateFlow<List<Movie>>(FallbackCatalog.popularMovies.reversed())
    val topRatedMovies: StateFlow<List<Movie>> = _topRatedMovies.asStateFlow()

    private val _actionMovies = MutableStateFlow<List<Movie>>(emptyList())
    val actionMovies: StateFlow<List<Movie>> = _actionMovies.asStateFlow()

    private val _scifiMovies = MutableStateFlow<List<Movie>>(emptyList())
    val scifiMovies: StateFlow<List<Movie>> = _scifiMovies.asStateFlow()

    private val _tvSeries = MutableStateFlow<List<Movie>>(FallbackCatalog.tvSeries)
    val tvSeries: StateFlow<List<Movie>> = _tvSeries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val trending = repository.getTrending()
                _trendingMovies.value = trending
                if (trending.isNotEmpty()) {
                    _heroMovie.value = trending.firstOrNull { it.backdropPath != null } ?: trending.first()
                }

                _popularMovies.value = repository.getPopular()
                _topRatedMovies.value = repository.getTopRated()
                _tvSeries.value = repository.getTvSeries()
                _actionMovies.value = repository.getActionMovies()
                _scifiMovies.value = repository.getSciFiMovies()
            } catch (e: Exception) {
                // Catalog fallbacks already initialized
            } finally {
                _isLoading.value = false
            }
        }
    }
}
