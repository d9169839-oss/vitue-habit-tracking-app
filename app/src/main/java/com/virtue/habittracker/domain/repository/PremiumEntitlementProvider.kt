package com.virtue.habittracker.domain.repository

import kotlinx.coroutines.flow.StateFlow

/** Abstraction over Google Play entitlement state; domain code does not depend on BillingClient. */
interface PremiumEntitlementProvider {
    val premiumEntitlement: StateFlow<Boolean>
    fun refreshEntitlement()
}
