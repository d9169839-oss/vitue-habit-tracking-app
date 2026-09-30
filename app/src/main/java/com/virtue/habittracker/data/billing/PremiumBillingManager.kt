package com.virtue.habittracker.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.virtue.habittracker.domain.repository.PremiumEntitlementProvider

/**
 * Google Play subscription client.
 * Replace product IDs with Play Console IDs and verify purchase tokens on a trusted backend
 * before production. Client-only purchase state is a UX signal, not a secure authorization boundary.
 */
@Singleton
class PremiumBillingManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext context: Context
) : com.android.billingclient.api.PurchasesUpdatedListener, PremiumEntitlementProvider {

    companion object {
        const val MONTHLY_PRODUCT_ID = "vitue_premium_monthly"
        const val YEARLY_PRODUCT_ID = "vitue_premium_yearly"
        private val PRODUCT_IDS = listOf(MONTHLY_PRODUCT_ID, YEARLY_PRODUCT_ID)
    }

    private val preferences = context.getSharedPreferences("vitue_premium_entitlement", Context.MODE_PRIVATE)
    // Cached state supports offline UX only; a trusted backend must be the authorization source.
    private val _isPremium = MutableStateFlow(preferences.getBoolean("premiumCached", false))
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()
    override val premiumEntitlement: StateFlow<Boolean> get() = isPremium
    override fun refreshEntitlement() = refreshPurchases()
    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val client = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    @Volatile private var connecting = false

    fun connect() {
        if (client.isReady || connecting) return
        connecting = true
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                connecting = false
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    refreshProducts()
                    refreshPurchases()
                } else {
                    _message.value = "Google Play billing is unavailable (${result.responseCode})."
                }
            }
            override fun onBillingServiceDisconnected() {
                connecting = false
                _message.value = "Billing disconnected. Try again when Google Play is available."
            }
        })
    }

    fun refreshProducts() {
        if (!client.isReady) { connect(); return }
        val products = PRODUCT_IDS.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        client.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = details
                _message.value = if (details.isEmpty()) {
                    "No subscriptions found. Check Play Console product IDs and testing setup."
                } else null
            } else {
                _message.value = "Could not load subscriptions (${result.responseCode})."
            }
        }
    }

    fun refreshPurchases() {
        if (!client.isReady) { connect(); return }
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) handlePurchases(purchases)
            else _message.value = "Could not restore purchases (${result.responseCode})."
        }
    }

    fun launchPurchase(activity: Activity, product: ProductDetails) {
        if (!client.isReady) {
            _message.value = "Connecting to Google Play. Please try again in a moment."
            connect()
            return
        }
        val offer = product.subscriptionOfferDetails?.firstOrNull()
        if (offer == null) {
            _message.value = "No eligible subscription offer is available for this account."
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product)
            .setOfferToken(offer.offerToken)
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        val result = client.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _message.value = "Could not open Google Play checkout (${result.responseCode})."
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> handlePurchases(purchases.orEmpty())
            BillingClient.BillingResponseCode.USER_CANCELED -> _message.value = "Purchase cancelled."
            else -> _message.value = "Purchase could not be completed (${result.responseCode})."
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        val owned = purchases.filter {
            it.purchaseState == Purchase.PurchaseState.PURCHASED && it.products.any(PRODUCT_IDS::contains)
        }
        _isPremium.value = owned.isNotEmpty()
        preferences.edit().putBoolean("premiumCached", owned.isNotEmpty()).apply()
        owned.filter { !it.isAcknowledged }.forEach { purchase ->
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            client.acknowledgePurchase(params) { result ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    _message.value = "Purchase acknowledgement will need to be retried."
                }
            }
        }
        if (owned.isNotEmpty()) _message.value = null
    }
}
