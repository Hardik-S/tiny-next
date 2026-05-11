package com.batb4016.tinynext.data.purchase

data class PurchaseCache(
    val isPremium: Boolean = false,
    val productId: String? = null,
    val purchaseTokenHash: String? = null,
    val acknowledged: Boolean = false,
    val lastVerifiedAt: Long? = null,
    val source: String = "NONE",
)
