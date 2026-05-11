package com.batb4016.tinynext.data.purchase

import kotlinx.coroutines.flow.Flow

interface PurchaseCacheRepository {
    val purchaseCache: Flow<PurchaseCache>

    suspend fun cachePremiumPurchase(
        productId: String,
        purchaseToken: String,
        verifiedAt: Long,
        acknowledged: Boolean,
        source: String,
    )

    suspend fun setAcknowledged(acknowledged: Boolean)
    suspend fun clearPremiumPurchase()
}
