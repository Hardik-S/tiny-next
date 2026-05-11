package com.batb4016.tinynext.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.batb4016.tinynext.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private const val GOOGLE_DEMO_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun BannerAdView(
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adUnitId = if (BuildConfig.DEBUG) {
        GOOGLE_DEMO_BANNER_AD_UNIT_ID
    } else {
        BuildConfig.ADMOB_BANNER_AD_UNIT_ID
    }
    var loadFailed by remember(adUnitId, isPremium) { mutableStateOf(false) }

    if (isPremium || loadFailed || adUnitId.isBlank()) {
        return
    }

    val adView = remember(context, adUnitId) {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId
        }
    }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {
                adView.apply {
                    adListener = object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            loadFailed = true
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
