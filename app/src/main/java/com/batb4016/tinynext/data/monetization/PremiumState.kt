package com.batb4016.tinynext.data.monetization

const val REMOVE_ADS_PREMIUM_PRODUCT_ID = "remove_ads_premium"

enum class PremiumSource {
    None,
    PlayBilling,
    LocalCache,
    DebugOverride
}

/**
 * Local entitlement contract shared by BillingRepository and any DataStore cache.
 *
 * The model is intentionally plain Kotlin so Worker A can persist the same fields
 * without depending on Google Play Billing types. Only a confirmed Play purchase
 * or an explicitly gated debug override should set isPremium=true.
 */
data class PremiumState(
    val isPremium: Boolean,
    val source: PremiumSource,
    val productId: String,
    val purchaseToken: String?,
    val acknowledged: Boolean,
    val billingReady: Boolean,
    val productAvailable: Boolean,
    val priceLabel: String?,
    val purchaseInProgress: Boolean,
    val lastMessage: String?,
    val recordedAtMillis: Long
) {
    val canLaunchPurchase: Boolean
        get() = billingReady && productAvailable && !isPremium && !purchaseInProgress

    fun withDebugOverride(
        allowDebugPremiumOverride: Boolean,
        debugPremiumEnabled: Boolean
    ): PremiumState {
        if (!allowDebugPremiumOverride || !debugPremiumEnabled) return this

        return copy(
            isPremium = true,
            source = PremiumSource.DebugOverride,
            purchaseToken = null,
            acknowledged = true,
            purchaseInProgress = false
        )
    }

    companion object {
        val Free = PremiumState(
            isPremium = false,
            source = PremiumSource.None,
            productId = REMOVE_ADS_PREMIUM_PRODUCT_ID,
            purchaseToken = null,
            acknowledged = false,
            billingReady = false,
            productAvailable = false,
            priceLabel = null,
            purchaseInProgress = false,
            lastMessage = null,
            recordedAtMillis = 0L
        )

        fun fromCache(
            productId: String,
            purchaseToken: String?,
            purchased: Boolean,
            acknowledged: Boolean,
            recordedAtMillis: Long
        ): PremiumState {
            if (!purchased || productId != REMOVE_ADS_PREMIUM_PRODUCT_ID) {
                return Free.copy(recordedAtMillis = recordedAtMillis)
            }

            return Free.copy(
                isPremium = true,
                source = PremiumSource.LocalCache,
                productId = productId,
                purchaseToken = purchaseToken,
                acknowledged = acknowledged,
                recordedAtMillis = recordedAtMillis
            )
        }

        fun fromPlayPurchase(
            purchaseToken: String,
            acknowledged: Boolean,
            recordedAtMillis: Long
        ): PremiumState = Free.copy(
            isPremium = true,
            source = PremiumSource.PlayBilling,
            purchaseToken = purchaseToken,
            acknowledged = acknowledged,
            recordedAtMillis = recordedAtMillis
        )
    }
}
