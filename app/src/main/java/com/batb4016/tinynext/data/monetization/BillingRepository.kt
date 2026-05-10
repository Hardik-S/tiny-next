package com.batb4016.tinynext.data.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.batb4016.tinynext.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.Closeable

sealed interface BillingLaunchResult {
    data object Launched : BillingLaunchResult
    data object AlreadyPremium : BillingLaunchResult
    data object BillingUnavailable : BillingLaunchResult
    data object ProductUnavailable : BillingLaunchResult
    data class Failed(val responseCode: Int, val debugMessage: String) : BillingLaunchResult
}

interface BillingRepository : Closeable {
    val premiumState: StateFlow<PremiumState>

    fun start()
    fun refreshProduct()
    fun restorePurchases()
    fun launchRemoveAdsPurchase(activity: Activity): BillingLaunchResult
    fun setDebugPremiumOverride(enabled: Boolean)
}

class GooglePlayBillingRepository(
    context: Context,
    private val allowDebugPremiumOverride: Boolean = BuildConfig.ALLOW_DEBUG_PREMIUM_OVERRIDE,
    private val clockMillis: () -> Long = { System.currentTimeMillis() }
) : BillingRepository, PurchasesUpdatedListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val appContext = context.applicationContext
    private var productDetails: ProductDetails? = null
    private var debugPremiumOverrideEnabled = false

    private val _premiumState = MutableStateFlow(PremiumState.Free)
    override val premiumState: StateFlow<PremiumState> = _premiumState

    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    override fun start() {
        if (billingClient.isReady) {
            markBillingReady()
            refreshProduct()
            restorePurchases()
            return
        }

        _premiumState.updateWithDebugOverride {
            it.copy(lastMessage = "Connecting to Google Play Billing.")
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    markBillingReady()
                    refreshProduct()
                    restorePurchases()
                } else {
                    markBillingUnavailable("Billing unavailable: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                _premiumState.updateWithDebugOverride {
                    it.copy(
                        billingReady = false,
                        productAvailable = false,
                        lastMessage = "Billing disconnected. Restore or buy will reconnect."
                    )
                }
            }
        })
    }

    override fun refreshProduct() {
        if (!billingClient.isReady) {
            start()
            return
        }

        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(REMOVE_ADS_PREMIUM_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                productDetails = null
                _premiumState.updateWithDebugOverride {
                    it.copy(
                        billingReady = billingClient.isReady,
                        productAvailable = false,
                        priceLabel = null,
                        lastMessage = "Premium unavailable: ${billingResult.debugMessage}"
                    )
                }
                return@queryProductDetailsAsync
            }

            val details = productDetailsResult.productDetailsList
                .firstOrNull { it.productId == REMOVE_ADS_PREMIUM_PRODUCT_ID }
            productDetails = details

            _premiumState.updateWithDebugOverride {
                it.copy(
                    billingReady = billingClient.isReady,
                    productAvailable = details != null,
                    priceLabel = details?.formattedOneTimePrice(),
                    lastMessage = if (details == null) {
                        "Premium product is not configured in Play Console yet."
                    } else {
                        null
                    }
                )
            }
        }
    }

    override fun restorePurchases() {
        if (!billingClient.isReady) {
            start()
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                _premiumState.updateWithDebugOverride {
                    it.copy(
                        billingReady = billingClient.isReady,
                        lastMessage = "Restore unavailable: ${billingResult.debugMessage}"
                    )
                }
                return@queryPurchasesAsync
            }

            val premiumPurchase = purchases.firstOrNull { it.isRemoveAdsPurchase() }
            if (premiumPurchase == null) {
                _premiumState.updateWithDebugOverride {
                    it.copy(
                        isPremium = false,
                        source = PremiumSource.None,
                        purchaseToken = null,
                        acknowledged = false,
                        purchaseInProgress = false,
                        lastMessage = "No premium purchase found to restore."
                    )
                }
                return@queryPurchasesAsync
            }

            handlePurchase(premiumPurchase)
        }
    }

    override fun launchRemoveAdsPurchase(activity: Activity): BillingLaunchResult {
        val state = premiumState.value
        if (state.isPremium) return BillingLaunchResult.AlreadyPremium

        if (!billingClient.isReady) {
            start()
            return BillingLaunchResult.BillingUnavailable
        }

        val details = productDetails ?: return BillingLaunchResult.ProductUnavailable.also {
            refreshProduct()
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply {
                details.oneTimeOfferToken()?.let(::setOfferToken)
            }
            .build()

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        _premiumState.updateWithDebugOverride {
            it.copy(purchaseInProgress = true, lastMessage = "Launching purchase.")
        }

        val result = billingClient.launchBillingFlow(activity, params)
        return if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            BillingLaunchResult.Launched
        } else {
            _premiumState.updateWithDebugOverride {
                it.copy(
                    purchaseInProgress = false,
                    lastMessage = "Purchase could not start: ${result.debugMessage}"
                )
            }
            BillingLaunchResult.Failed(result.responseCode, result.debugMessage)
        }
    }

    override fun setDebugPremiumOverride(enabled: Boolean) {
        debugPremiumOverrideEnabled = allowDebugPremiumOverride && enabled
        _premiumState.updateWithDebugOverride { it }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val premiumPurchase = purchases.orEmpty().firstOrNull { it.isRemoveAdsPurchase() }
                if (premiumPurchase != null) {
                    handlePurchase(premiumPurchase)
                } else {
                    _premiumState.updateWithDebugOverride {
                        it.copy(purchaseInProgress = false, lastMessage = "Purchase update did not include premium.")
                    }
                }
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _premiumState.updateWithDebugOverride {
                    it.copy(purchaseInProgress = false, lastMessage = "Premium already owned; restoring purchase.")
                }
                restorePurchases()
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _premiumState.updateWithDebugOverride {
                    it.copy(purchaseInProgress = false, lastMessage = "Purchase canceled.")
                }
            }

            else -> {
                _premiumState.updateWithDebugOverride {
                    it.copy(
                        purchaseInProgress = false,
                        lastMessage = "Purchase failed: ${billingResult.debugMessage}"
                    )
                }
            }
        }
    }

    override fun close() {
        scope.cancel()
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            _premiumState.updateWithDebugOverride {
                it.copy(
                    isPremium = false,
                    source = PremiumSource.None,
                    purchaseInProgress = false,
                    lastMessage = "Premium purchase is pending; ads remain until payment completes."
                )
            }
            return
        }

        _premiumState.updateWithDebugOverride {
            PremiumState.fromPlayPurchase(
                purchaseToken = purchase.purchaseToken,
                acknowledged = purchase.isAcknowledged,
                recordedAtMillis = clockMillis()
            ).copy(
                billingReady = billingClient.isReady,
                productAvailable = productDetails != null,
                priceLabel = it.priceLabel,
                purchaseInProgress = false,
                lastMessage = null
            )
        }

        if (!purchase.isAcknowledged) {
            acknowledgePurchase(purchase.purchaseToken)
        }
    }

    private fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _premiumState.updateWithDebugOverride {
                    it.copy(acknowledged = true, lastMessage = null)
                }
            } else {
                _premiumState.updateWithDebugOverride {
                    it.copy(lastMessage = "Premium active, but acknowledgement retry is needed: ${billingResult.debugMessage}")
                }
            }
        }
    }

    private fun markBillingReady() {
        _premiumState.updateWithDebugOverride {
            it.copy(billingReady = true, lastMessage = null)
        }
    }

    private fun markBillingUnavailable(message: String) {
        productDetails = null
        _premiumState.updateWithDebugOverride {
            PremiumState.Free.copy(lastMessage = message, recordedAtMillis = it.recordedAtMillis)
        }
    }

    private fun MutableStateFlow<PremiumState>.updateWithDebugOverride(
        transform: (PremiumState) -> PremiumState
    ) {
        update { current ->
            transform(current).withDebugOverride(
                allowDebugPremiumOverride = allowDebugPremiumOverride,
                debugPremiumEnabled = debugPremiumOverrideEnabled
            )
        }
    }

    private fun ProductDetails.formattedOneTimePrice(): String? =
        oneTimePurchaseOfferDetailsList?.firstOrNull()?.formattedPrice
            ?: oneTimePurchaseOfferDetails?.formattedPrice

    private fun ProductDetails.oneTimeOfferToken(): String? =
        oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken
            ?: oneTimePurchaseOfferDetails?.offerToken

    private fun Purchase.isRemoveAdsPurchase(): Boolean =
        products.contains(REMOVE_ADS_PREMIUM_PRODUCT_ID)
}
