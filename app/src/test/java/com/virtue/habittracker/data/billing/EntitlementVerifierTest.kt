package com.virtue.habittracker.data.billing

import com.android.billingclient.api.Purchase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntitlementVerifierTest {

    private val verifier = DefaultEntitlementVerifier()
    private val eligibleProducts = listOf("virtue_premium_monthly", "virtue_premium_yearly")

    @Test
    fun purchasedStateWithEligibleProduct_isEntitled() {
        val result = verifier.isPurchaseEntitled(
            purchaseState = Purchase.PurchaseState.PURCHASED,
            purchasedProducts = listOf("virtue_premium_monthly"),
            eligibleProducts = eligibleProducts
        )
        assertTrue(result)
    }

    @Test
    fun pendingStateWithEligibleProduct_isNotEntitled() {
        val result = verifier.isPurchaseEntitled(
            purchaseState = Purchase.PurchaseState.PENDING,
            purchasedProducts = listOf("virtue_premium_monthly"),
            eligibleProducts = eligibleProducts
        )
        assertFalse(result)
    }

    @Test
    fun unspecifiedStateWithEligibleProduct_isNotEntitled() {
        val result = verifier.isPurchaseEntitled(
            purchaseState = Purchase.PurchaseState.UNSPECIFIED_STATE,
            purchasedProducts = listOf("virtue_premium_yearly"),
            eligibleProducts = eligibleProducts
        )
        assertFalse(result)
    }

    @Test
    fun purchasedStateWithUnknownProduct_isNotEntitled() {
        val result = verifier.isPurchaseEntitled(
            purchaseState = Purchase.PurchaseState.PURCHASED,
            purchasedProducts = listOf("unrelated_item"),
            eligibleProducts = eligibleProducts
        )
        assertFalse(result)
    }

    @Test
    fun emptyPurchasedProducts_isNotEntitled() {
        val result = verifier.isPurchaseEntitled(
            purchaseState = Purchase.PurchaseState.PURCHASED,
            purchasedProducts = emptyList(),
            eligibleProducts = eligibleProducts
        )
        assertFalse(result)
    }
}
