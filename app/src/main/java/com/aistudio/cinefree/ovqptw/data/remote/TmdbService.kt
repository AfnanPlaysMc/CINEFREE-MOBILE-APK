package com.aistudio.cinefree.ovqptw.data.remote

import com.aistudio.cinefree.ovqptw.data.model.Movie
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class TmdbResponse<T>(
    @Json(name = "results") val results: List<T>? = null
)

@JsonClass(generateAdapter = true)
data class TmdbMovieDto(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Double? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "media_type") val mediaType: String? = null
) {
    fun toMovie(): Movie = Movie(
        id = id.toString(),
        title = title ?: name ?: "Unknown Title",
        overview = overview ?: "",
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage ?: 0.0,
        releaseDate = releaseDate ?: firstAirDate,
        mediaType = mediaType ?: if (title != null) "movie" else "tv"
    )
}

interface TmdbService {
    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMovieDto>

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMovieDto>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMovieDto>

    @GET("trending/tv/week")
    suspend fun getTrendingTv(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMovieDto>

    @GET("discover/movie")
    suspend fun discoverByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMovieDto>

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbResponse<TmdbMovieDto>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: String,
        @Query("api_key") apiKey: String
    ): TmdbMovieDto
    
    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") tvId: String,
        @Query("api_key") apiKey: String
    ): TmdbMovieDto
}
