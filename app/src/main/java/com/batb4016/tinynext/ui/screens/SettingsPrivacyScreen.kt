package com.batb4016.tinynext.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsPrivacyScreen(
    isPremium: Boolean,
    appVersion: String,
    onRestorePurchase: () -> Unit,
    onDeleteAllLocalData: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Premium status: ${if (isPremium) "Active" else "Free"}")
        OutlinedButton(onClick = onRestorePurchase, modifier = Modifier.fillMaxWidth()) { Text("Restore purchase") }
        Text(
            text = "Tiny Next stores your tasks on this device. Tiny Next does not require an account. If ads are enabled, Google Mobile Ads may collect data for advertising, analytics, and fraud prevention. Premium removes ads.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(
            onClick = onDeleteAllLocalData,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Delete all local data")
        }
        Text("App version $appVersion", style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

