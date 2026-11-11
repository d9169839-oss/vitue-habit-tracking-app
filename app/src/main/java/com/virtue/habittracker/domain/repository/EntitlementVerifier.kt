package com.virtue.habittracker.domain.repository

/**
 * Determines whether purchase records satisfy entitlement criteria.
 *
 * Separating the entitlement determination policy from the Google Play BillingClient
 * transport adheres to the Single Responsibility Principle (SRP) and enables isolated,
 * deterministic unit testing of purchase state verification.
 */
fun interface EntitlementVerifier {
    fun isPurchaseEntitled(
        purchaseState: Int,
        purchasedProducts: List<String>,
        eligibleProducts: List<String>
    ): Boolean
}
