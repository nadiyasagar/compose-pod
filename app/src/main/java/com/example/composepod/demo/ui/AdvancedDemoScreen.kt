package com.example.composepod.demo.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brine.composepod.async.AsyncState
import com.brine.composepod.compose.rememberProvider
import com.brine.composepod.compose.watchProvider
import com.brine.composepod.core.autoDispose
import com.brine.composepod.core.family
import com.brine.composepod.core.FutureProvider
import com.brine.composepod.core.provider
import com.brine.composepod.mvi.MVIViewModel
import com.brine.composepod.mvi.UiIntent
import com.brine.composepod.mvi.UiState
import com.brine.composepod.mvi.stateNotifierProvider
import kotlinx.coroutines.delay

/**
 * 0. A simple Provider to test Overrides
 */
val appVersionProvider = provider("appVersion") { "1.0.0" }

/**
 * Counter State
 */
data class CounterState(
    val count: Int = 0
) : UiState

/**
 * Counter Intents
 */
sealed class CounterIntent : UiIntent {
    object Increment : CounterIntent()
    object Decrement : CounterIntent()
}

/**
 * Counter ViewModel with AutoDispose
 * This ViewModel will be destroyed when you leave the screen!
 */
class CounterViewModel : MVIViewModel<CounterState, CounterIntent>(
    initialState = CounterState()
) {
    init {
        // Log when created
        Log.d("AdvancedDemo", "🟢 CounterViewModel CREATED!")
    }

    override fun processIntent(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Increment -> {
                state = state.copy(count = state.count + 1)
                Log.d("AdvancedDemo", "Count incremented to: ${state.count}")
            }
            CounterIntent.Decrement -> {
                state = state.copy(count = state.count - 1)
                Log.d("AdvancedDemo", "Count decremented to: ${state.count}")
            }
        }
    }
}

/**
 * 1. A Simple AutoDispose Provider
 * This will be created when the screen opens, and completely destroyed when we go back.
 * TRY THIS:
 * 1. Increment the counter a few times
 * 2. Press Back to leave this screen
 * 3. Check Logcat - you'll see "DESTROYED!"
 * 4. Re-enter Advanced screen - count resets to 0!
 */
val counterAutoDisposeProvider = stateNotifierProvider {
    CounterViewModel()
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
    
    // Handle system back button
    BackHandler {
        onBack()
    }
    
    // We watch the family provider by passing the argument
    val userState1 by watchProvider(userProfileProvider("user_1"))
    val userStateError by watchProvider(userProfileProvider("error"))
    
    // Get counter viewmodel for dispatching intents
    val counterViewModel = rememberProvider(counterAutoDisposeProvider)
    val counterState = counterViewModel.stateFlow.collectAsStateWithLifecycle().value

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
            // 🔥 AutoDispose Demo Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title with explanation
                    Text(
                        text = "🔄 AutoDispose Demo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Leave screen → Provider DESTROYED → Count RESET",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Counter display
                    Text(
                        text = "${counterState.count}",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // + and - Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decrement button
                        FilledIconButton(
                            onClick = { counterViewModel.processIntent(CounterIntent.Decrement) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrement",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        
                        Text(
                            text = "Tap to change",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        // Increment button
                        FilledIconButton(
                            onClick = { counterViewModel.processIntent(CounterIntent.Increment) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increment",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Tip text
                    Text(
                        text = "💡 Try this: Change count → Go Back → Return → Count is 0!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }

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
