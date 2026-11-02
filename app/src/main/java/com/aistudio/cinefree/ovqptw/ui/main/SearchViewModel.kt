package com.aistudio.cinefree.ovqptw.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.cinefree.ovqptw.data.local.FallbackCatalog
import com.aistudio.cinefree.ovqptw.data.model.Movie
import com.aistudio.cinefree.ovqptw.data.remote.RetrofitClient
import com.aistudio.cinefree.ovqptw.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val repository = MovieRepository(RetrofitClient.tmdbService)
    private var searchJob: Job? = null

    private val _searchResults = MutableStateFlow<List<Movie>>(FallbackCatalog.allCatalog)
    val searchResults: StateFlow<List<Movie>> = _searchResults.asStateFlow()

    private val _selectedFilter = MutableStateFlow("all") // all, movie, tv
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = FallbackCatalog.allCatalog
            _isLoading.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isLoading.value = true
            delay(300) // Debounce
            try {
                val results = repository.searchMovies(query)
                _searchResults.value = results
            } catch (e: Exception) {
                _searchResults.value = FallbackCatalog.search(query)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
