package com.aistudio.cinefree.ovqptw.data

object Config {
    // Verified working TMDB API Key from CINEFREE repository
    const val DEFAULT_TMDB_API_KEY = "407886e825dc0d2581cc397ce2289f4a"
    
    var currentTmdbKey: String = DEFAULT_TMDB_API_KEY

    const val TMDB_IMAGE_BASE_W500 = "https://image.tmdb.org/t/p/w500"
    const val TMDB_IMAGE_BASE_ORIGINAL = "https://image.tmdb.org/t/p/original"
}
