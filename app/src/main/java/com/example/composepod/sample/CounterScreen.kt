package com.example.composepod.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brine.composepod.core.select
import com.brine.composepod.compose.rememberProvider
import com.brine.composepod.compose.watchProvider

@Composable
fun CounterScreen() {
    // Watch only the count property from the provider's state
    // This will trigger a recomposition ONLY when the count changes
    val count by watchProvider(counterProvider.select { it.count })

    // Remember the provider instance itself so we can send intents
    // without triggering recomposition on this line
    val viewModel = rememberProvider(counterProvider)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ComposePod App",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Count: $count",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row {
                Button(onClick = { viewModel.processIntent(CounterIntent.Decrement) }) {
                    Text("-")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(onClick = { viewModel.processIntent(CounterIntent.Increment) }) {
                    Text("+")
                }
            }
        }
    }
}
