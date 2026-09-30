package com.virtue.habittracker.domain.model

/**
 * Public profile data for the signed-in person.
 *
 * Authentication credentials are deliberately excluded: Firebase Authentication stores
 * and validates the password. Never mirror passwords into Room or Firestore.
 */
data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
