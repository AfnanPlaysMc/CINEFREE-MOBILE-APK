package com.aistudio.cinefree.ovqptw.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = "user",
    val photoUrl: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class WatchlistItem(
    @DocumentId val id: String = "",
    val movieId: String = "",
    val title: String = "",
    val posterPath: String? = null,
    val mediaType: String = "movie",
    @ServerTimestamp val addedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MovieReview(
    @DocumentId val reviewId: String = "",
    val movieId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MovieOverride(
    @DocumentId val movieId: String = "",
    val customStreamUrl: String? = null,
    val featured: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class Movie(
    val id: String,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val releaseDate: String?,
    val mediaType: String = "movie"
)
