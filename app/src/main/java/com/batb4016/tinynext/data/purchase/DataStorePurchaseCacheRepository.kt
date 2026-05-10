package com.batb4016.tinynext.data.purchase

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

val Context.tinyNextPurchaseDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tiny_next_purchase_cache",
)

class DataStorePurchaseCacheRepository(
    private val dataStore: DataStore<Preferences>,
) : PurchaseCacheRepository {
    constructor(context: Context) : this(context.tinyNextPurchaseDataStore)

    override val purchaseCache: Flow<PurchaseCache> = dataStore.data.map { preferences ->
        PurchaseCache(
            isPremium = preferences[Keys.isPremium] ?: false,
            productId = preferences[Keys.productId],
            purchaseTokenHash = preferences[Keys.purchaseTokenHash],
            acknowledged = preferences[Keys.acknowledged] ?: false,
            lastVerifiedAt = preferences[Keys.lastVerifiedAt],
            source = preferences[Keys.source] ?: "NONE",
        )
    }

    override suspend fun cachePremiumPurchase(
        productId: String,
        purchaseToken: String,
        verifiedAt: Long,
        acknowledged: Boolean,
        source: String,
    ) {
        require(productId.isNotBlank()) { "Product id is required." }
        require(purchaseToken.isNotBlank()) { "Purchase token is required." }
        dataStore.edit { preferences ->
            preferences[Keys.isPremium] = true
            preferences[Keys.productId] = productId
            preferences[Keys.purchaseTokenHash] = purchaseToken.sha256()
            preferences[Keys.lastVerifiedAt] = verifiedAt
            preferences[Keys.acknowledged] = acknowledged
            preferences[Keys.source] = source
        }
    }

    override suspend fun setAcknowledged(acknowledged: Boolean) {
        dataStore.edit { it[Keys.acknowledged] = acknowledged }
    }

    override suspend fun clearPremiumPurchase() {
        dataStore.edit { preferences ->
            preferences[Keys.isPremium] = false
            preferences.remove(Keys.productId)
            preferences.remove(Keys.purchaseTokenHash)
            preferences.remove(Keys.lastVerifiedAt)
            preferences[Keys.acknowledged] = false
            preferences[Keys.source] = "NONE"
        }
    }

    private object Keys {
        val isPremium = booleanPreferencesKey("is_premium")
        val productId = stringPreferencesKey("product_id")
        val purchaseTokenHash = stringPreferencesKey("purchase_token_hash")
        val acknowledged = booleanPreferencesKey("acknowledged")
        val lastVerifiedAt = longPreferencesKey("last_verified_at")
        val source = stringPreferencesKey("source")
    }
}

private fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(toByteArray())
    return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
}
