package com.batb4016.tinynext

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TinyNextApp(modifier: Modifier = Modifier) {
    MaterialTheme {
        Surface(modifier = modifier) {
            Text("Tiny Next")
        }
    }
}

