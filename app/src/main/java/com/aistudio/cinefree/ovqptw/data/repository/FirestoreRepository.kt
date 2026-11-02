package com.aistudio.cinefree.ovqptw.data.repository

import android.content.Context
import com.aistudio.cinefree.ovqptw.R
import com.aistudio.cinefree.ovqptw.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepository(context: Context) {
    private val db = FirebaseFirestore.getInstance(
        context.getString(R.string.firestore_database_id)
    )
    private val auth = Firebase.auth

    private fun requireUserId(): String = auth.currentUser?.uid 
        ?: throw IllegalStateException("User must be signed in")

    suspend fun syncUser(user: UserProfile) {
        val uid = requireUserId()
        db.collection("users").document(uid).set(user).await()
    }

    fun observeUserProfile(): Flow<UserProfile?> {
        val uid = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(null)
        return db.collection("users").document(uid).snapshots().map { it.toObject(UserProfile::class.java) }
    }

    fun observeWatchlist(): Flow<List<WatchlistItem>> {
        val uid = requireUserId()
        return db.collection("users").document(uid).collection("watchlist")
            .snapshots()
            .map { it.toObjects(WatchlistItem::class.java) }
    }

    suspend fun addToWatchlist(item: WatchlistItem) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("watchlist")
            .document(item.movieId)
            .set(item)
            .await()
    }

    suspend fun removeFromWatchlist(movieId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("watchlist")
            .document(movieId)
            .delete()
            .await()
    }

    fun observeReviews(movieId: String): Flow<List<MovieReview>> {
        return db.collection("reviews")
            .whereEqualTo("movieId", movieId)
            .snapshots()
            .map { it.toObjects(MovieReview::class.java) }
    }

    suspend fun addReview(review: MovieReview) {
        db.collection("reviews").add(review).await()
    }

    suspend fun getMovieOverride(movieId: String): MovieOverride? {
        return db.collection("overrides").document(movieId).get().await().toObject(MovieOverride::class.java)
    }
}
