package com.batb4016.tinynext.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.batb4016.tinynext.ui.model.TaskUiModel

@Composable
fun ResultScreen(
    task: TaskUiModel?,
    onDone: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit,
    onAnother: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Next up", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(task?.title ?: "No eligible task right now", style = MaterialTheme.typography.headlineSmall)
                if (task != null) {
                    Text("${task.categoryName} • ${task.estimateMinutes} min", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Button(onClick = onDone, enabled = task != null, modifier = Modifier.fillMaxWidth()) { Text("Done") }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 12.dp)) {
            OutlinedButton(onClick = onSnooze, enabled = task != null, modifier = Modifier.weight(1f)) { Text("Snooze") }
            OutlinedButton(onClick = onSkip, enabled = task != null, modifier = Modifier.weight(1f)) { Text("Skip") }
            OutlinedButton(onClick = onAnother, enabled = task != null, modifier = Modifier.weight(1f)) { Text("Another") }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Back") }
    }
}

