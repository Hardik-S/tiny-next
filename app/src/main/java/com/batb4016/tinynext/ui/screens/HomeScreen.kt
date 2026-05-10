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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    activeTaskCount: Int,
    completedToday: Int,
    showAds: Boolean,
    onPick: () -> Unit,
    onAddTask: () -> Unit,
    onViewList: () -> Unit,
    onStats: () -> Unit,
    onPremium: () -> Unit,
    onSettings: () -> Unit,
    adContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tiny Next", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = onSettings) { Text("Settings") }
            }
            Spacer(Modifier.height(24.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Active tasks", style = MaterialTheme.typography.labelLarge)
                    Text(activeTaskCount.toString(), style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("Completed today: $completedToday", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(24.dp))
            if (activeTaskCount == 0) {
                Text(
                    text = "Add a few tiny tasks, then Tiny Next can choose one for you.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onPick,
                enabled = activeTaskCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Pick My Next Task")
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onAddTask, modifier = Modifier.weight(1f)) { Text("Add task") }
                OutlinedButton(onClick = onViewList, modifier = Modifier.weight(1f)) { Text("View list") }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onStats, modifier = Modifier.weight(1f)) { Text("Stats") }
                OutlinedButton(onClick = onPremium, modifier = Modifier.weight(1f)) { Text("Premium") }
            }
        }
        if (showAds) {
            adContent()
        }
    }
}

