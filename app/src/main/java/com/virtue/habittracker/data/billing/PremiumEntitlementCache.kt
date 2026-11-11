package com.virtue.habittracker.data.billing

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abstraction for local persistent caching of premium entitlement status.
 *
 * Separating storage from the billing transport satisfies SRP.
 */
interface PremiumEntitlementCache {
    fun isPremiumCached(): Boolean
    fun setPremiumCached(isPremium: Boolean)
}

@Singleton
class SharedPreferencesPremiumEntitlementCache @Inject constructor(
    @ApplicationContext context: Context
) : PremiumEntitlementCache {
    private val preferences = context.getSharedPreferences("virtue_premium_entitlement", Context.MODE_PRIVATE)

    override fun isPremiumCached(): Boolean = preferences.getBoolean("premiumCached", false)

    override fun setPremiumCached(isPremium: Boolean) {
        preferences.edit().putBoolean("premiumCached", isPremium).apply()
    }
}
