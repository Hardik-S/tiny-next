package com.batb4016.tinynext.data.monetization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumStateTest {
    @Test
    fun cachedPurchaseOnlyGrantsPremiumWhenRemoveAdsWasPurchased() {
        val purchased = PremiumState.fromCache(
            productId = REMOVE_ADS_PREMIUM_PRODUCT_ID,
            purchaseToken = "token-123",
            purchased = true,
            acknowledged = true,
            recordedAtMillis = 42L
        )

        val wrongProduct = PremiumState.fromCache(
            productId = "other_product",
            purchaseToken = "token-456",
            purchased = true,
            acknowledged = true,
            recordedAtMillis = 43L
        )

        assertTrue(purchased.isPremium)
        assertEquals(PremiumSource.LocalCache, purchased.source)
        assertEquals("token-123", purchased.purchaseToken)
        assertEquals(42L, purchased.recordedAtMillis)
        assertFalse(wrongProduct.isPremium)
        assertEquals(PremiumSource.None, wrongProduct.source)
        assertNull(wrongProduct.purchaseToken)
    }

    @Test
    fun debugOverrideRequiresBuildConfigGateAndExplicitToggle() {
        val baseline = PremiumState.Free.copy(lastMessage = "billing unavailable")

        val blocked = baseline.withDebugOverride(
            allowDebugPremiumOverride = false,
            debugPremiumEnabled = true
        )
        val enabled = baseline.withDebugOverride(
            allowDebugPremiumOverride = true,
            debugPremiumEnabled = true
        )
        val disabled = baseline.withDebugOverride(
            allowDebugPremiumOverride = true,
            debugPremiumEnabled = false
        )

        assertFalse(blocked.isPremium)
        assertEquals(PremiumSource.None, blocked.source)
        assertTrue(enabled.isPremium)
        assertEquals(PremiumSource.DebugOverride, enabled.source)
        assertEquals("billing unavailable", enabled.lastMessage)
        assertFalse(disabled.isPremium)
    }
}
