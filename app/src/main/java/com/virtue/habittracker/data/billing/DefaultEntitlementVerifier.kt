package com.virtue.habittracker.data.billing

import com.android.billingclient.api.Purchase
import com.virtue.habittracker.domain.repository.EntitlementVerifier
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates whether purchase records confer active premium entitlements.
 *
 * NOTE: Client-side verification is an offline/UX fallback. In production, a trusted backend
 * must verify purchase tokens directly with the Google Play Developer API before unlocking
 * sensitive cloud features.
 */
@Singleton
class DefaultEntitlementVerifier @Inject constructor() : EntitlementVerifier {
    override fun isPurchaseEntitled(
        purchaseState: Int,
        purchasedProducts: List<String>,
        eligibleProducts: List<String>
    ): Boolean {
        // Only confirmed PURCHASED states are entitled; PENDING, UNSPECIFIED, or cancelled do not qualify.
        if (purchaseState != Purchase.PurchaseState.PURCHASED) return false
        return purchasedProducts.any(eligibleProducts::contains)
    }
}
