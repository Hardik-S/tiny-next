package com.batb4016.tinynext.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.batb4016.tinynext.ui.model.CategoryUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTaskScreen(
    categories: List<CategoryUiModel>,
    initialTitle: String = "",
    onSave: (title: String, categoryId: String, estimateMinutes: Int, starred: Boolean, recurrence: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(initialTitle.take(120)) }
    var selectedCategoryId by remember(categories) { mutableStateOf(categories.firstOrNull()?.id.orEmpty()) }
    var selectedEstimate by remember { mutableIntStateOf(5) }
    var starred by remember { mutableStateOf(false) }
    var recurrence by remember { mutableStateOf("NONE") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Add Task", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it.take(120) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Small task") },
            supportingText = { Text("${title.length}/120") },
            singleLine = true
        )
        Text("Category", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                FilterChip(
                    selected = category.id == selectedCategoryId,
                    onClick = { selectedCategoryId = category.id },
                    label = { Text(category.name) }
                )
            }
        }
        Text("Estimate", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2, 5, 15, 30).forEach { minutes ->
                FilterChip(
                    selected = minutes == selectedEstimate,
                    onClick = { selectedEstimate = minutes },
                    label = { Text("$minutes min") }
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Star this task")
            Switch(checked = starred, onCheckedChange = { starred = it })
        }
        Text("Recurrence", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("NONE" to "None", "DAILY" to "Daily", "WEEKLY" to "Weekly").forEach { (value, label) ->
                FilterChip(selected = recurrence == value, onClick = { recurrence = value }, label = { Text(label) })
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { onSave(title.trim(), selectedCategoryId, selectedEstimate, starred, recurrence) },
            enabled = title.isNotBlank() && selectedCategoryId.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}
