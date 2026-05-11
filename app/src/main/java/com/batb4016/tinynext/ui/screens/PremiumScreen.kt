package com.batb4016.tinynext.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.batb4016.tinynext.BuildConfig
import com.batb4016.tinynext.data.monetization.PremiumSource
import com.batb4016.tinynext.data.monetization.PremiumState

@Composable
fun PremiumScreen(
    premiumState: PremiumState,
    onBuyPremium: () -> Unit,
    onRestorePurchase: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    debugPremiumOverrideEnabled: Boolean = false,
    onDebugPremiumOverrideChange: ((Boolean) -> Unit)? = null
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Make Tiny Next quiet.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (premiumState.isPremium) {
                    "Premium is active. Banner ads are hidden."
                } else {
                    "The free version keeps working. Premium just makes it cleaner."
                },
                style = MaterialTheme.typography.bodyLarge
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("No ads")
                Text("Premium themes")
                Text("Unlimited custom categories")
                Text("Advanced picker controls")
            }

            Text(
                text = if (!premiumState.billingReady || !premiumState.productAvailable) {
                    "Premium is temporarily unavailable."
                } else {
                    premiumState.priceLabel ?: "Price loads from Google Play."
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onBuyPremium,
                    enabled = premiumState.canLaunchPurchase,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (premiumState.purchaseInProgress) "Starting" else premiumState.priceLabel ?: "Upgrade")
                }
                OutlinedButton(
                    onClick = onRestorePurchase,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Restore Purchase")
                }
            }

            premiumState.lastMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }

            if (premiumState.source == PremiumSource.DebugOverride) {
                Text(
                    text = "Debug premium override is active.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (BuildConfig.ALLOW_DEBUG_PREMIUM_OVERRIDE && onDebugPremiumOverrideChange != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Debug premium override")
                    Switch(
                        checked = debugPremiumOverrideEnabled,
                        onCheckedChange = onDebugPremiumOverrideChange
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
        }
    }
}
