package com.example.composepod.demo.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brine.composepod.async.AsyncState
import com.brine.composepod.compose.rememberProvider
import com.brine.composepod.compose.watchProvider
import com.brine.composepod.core.ProviderObserver
import com.brine.composepod.core.autoDispose
import com.brine.composepod.core.family
import com.brine.composepod.core.FutureProvider
import com.brine.composepod.core.provider
import kotlinx.coroutines.delay

/**
 * 0. A simple Provider to test Overrides
 */
val appVersionProvider = provider("appVersion") { "1.0.0" }

/**
 * 1. A Simple AutoDispose Provider
 * This will be created when the screen opens, and completely destroyed when we go back.
 */
val counterAutoDisposeProvider = provider("counter") { ref ->
    var count = 0
    ref.onDispose {
        Log.d("AdvancedDemo", "AutoDisposeProvider was securely destroyed!")
    }
    count
}.autoDispose()

/**
 * 2. A FutureProvider with Family modifier
 * Mocks a network request that takes an argument (userId)
 */
val userProfileProvider = FutureProvider.family<String, String>("userProfile") { ref, userId ->
    // Mock network delay
    delay(2000)
    if (userId == "error") throw Exception("User not found!")
    "Profile Data for $userId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedDemoScreen(onBack: () -> Unit) {
    
    // We watch the family provider by passing the argument
    val userState1 by watchProvider(userProfileProvider("user_1"))
    val userStateError by watchProvider(userProfileProvider("error"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced ComposePod Features") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("AutoDispose Provider State (check Logcat on Back):", style = MaterialTheme.typography.titleMedium)
            
            // Watch autoDispose provider
            val count by watchProvider(counterAutoDisposeProvider)
            Text("Count: $count")

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text("App Version (testing Overrides):", style = MaterialTheme.typography.titleMedium)
            val version by watchProvider(appVersionProvider)
            Text("Version: $version", color = MaterialTheme.colorScheme.primary)

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("FutureProvider + Family Modifier:", style = MaterialTheme.typography.titleMedium)

            // UI for user_1
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Requesting user_1...", style = MaterialTheme.typography.labelSmall)
                    when (val state = userState1) {
                        is AsyncState.Loading -> CircularProgressIndicator()
                        is AsyncState.Success<*> -> Text(state.data.toString(), color = MaterialTheme.colorScheme.primary)
                        is AsyncState.Error<*> -> Text("Error: ${state.throwable.message}")
                    }
                }
            }

            // UI for error
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Requesting 'error' user...", style = MaterialTheme.typography.labelSmall)
                    when (val state = userStateError) {
                        is AsyncState.Loading -> CircularProgressIndicator()
                        is AsyncState.Success<*> -> Text(state.data.toString())
                        is AsyncState.Error<*> -> Text("Error: ${state.throwable.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
