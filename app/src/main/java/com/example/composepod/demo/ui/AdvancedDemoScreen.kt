package com.example.composepod.demo.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brine.composepod.async.AsyncState
import com.brine.composepod.async.`when`
import com.brine.composepod.compose.*
import com.brine.composepod.core.*
import com.brine.composepod.mvi.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Counter State
 */
data class CounterState(
    val countValue: Int = 0
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
 */
class CounterViewModel : MVIViewModel<CounterState, CounterIntent>(
    initialState = CounterState()
) {
    init {
        Log.d("AdvancedDemo", "🟢 CounterViewModel CREATED!")
    }

    override fun processIntent(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Increment -> {
                state = state.copy(countValue = state.countValue + 1)
            }

            CounterIntent.Decrement -> {
                state = state.copy(countValue = state.countValue - 1)
            }
        }
    }
}

/**
 * 1. AutoDispose Provider
 */
val counterProviderBase = stateNotifierProvider {
    CounterViewModel()
}
val counterAutoDisposeProvider: StateNotifierProvider<CounterViewModel, CounterState> = counterProviderBase.autoDispose()

/**
 * 2. KeepAlive Provider
 */
val persistentCounterProvider = stateNotifierProvider {
    CounterViewModel()
}.keepAlive()

/**
 * 3. Computed Provider (Derived State)
 */
val doubledCounterProvider = computedProvider { ref ->
    val counterVm = ref.watch(counterAutoDisposeProvider)
    counterVm.stateFlow.map { it.countValue * 2 }
}.autoDispose()

/**
 * 4. FutureProvider with Family
 */
val userProfileProvider = FutureProvider.family<String, String>("userProfile") { ref, userId ->
    delay(1500)
    if (userId == "error") throw Exception("User not found!")
    "User: $userId"
}

/**
 * 5. Refreshable Data Provider
 */
val refreshableDataProvider = futureProvider("refreshableData") { ref ->
    delay(1000)
    "Data loaded at ${System.currentTimeMillis() % 10000}"
}.autoDispose()

/**
 * 6. StreamProvider
 */
val streamCounterProvider = streamProvider("streamCounter") { ref ->
    flow {
        var count = 0
        while (true) {
            emit("Stream Count: $count")
            delay(1000)
            count++
        }
    }
}.autoDispose()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedDemoScreen(onBack: () -> Unit) {
    val container = currentProviderContainer()

    BackHandler { onBack() }

    // Providers
    val counterVm = rememberProvider(counterAutoDisposeProvider)
    val counterState = counterVm.stateFlow.collectAsStateWithLifecycle().value

    val doubledFlow by watchProvider(doubledCounterProvider)
    val doubledValue by doubledFlow.collectAsState(initial = 0)

    val persistentVm = rememberProvider(persistentCounterProvider)
    val persistentState = persistentVm.stateFlow.collectAsStateWithLifecycle().value

    val refreshableState by watchProvider(refreshableDataProvider)

    var selectedUserId by remember { mutableStateOf("user_1") }
    val userState by watchProvider(userProfileProvider(selectedUserId).autoDispose())

    val streamState by watchProvider(streamCounterProvider)

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    counterAutoDisposeProvider.listen { _, next ->
        val current = next.countValue
        if (current % 5 == 0 && current != 0) {
            scope.launch {
                snackbarHostState.showSnackbar("Milestone: $current")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚀 ComposePod Advanced") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                title = "🔄 AutoDispose",
                description = "Resets after navigating back"
            ) {
                CounterDemo(
                    count = counterState.countValue,
                    onIncrement = { counterVm.processIntent(CounterIntent.Increment) },
                    onDecrement = { counterVm.processIntent(CounterIntent.Decrement) }
                )
            }

            FeatureCard(
                title = "🧮 Computed",
                description = "Value × 2 = $doubledValue"
            ) {
                Text("$doubledValue", style = MaterialTheme.typography.displayLarge)
            }

            FeatureCard(
                title = "🔒 KeepAlive",
                description = "Persists after navigating back"
            ) {
                CounterDemo(
                    count = persistentState.countValue,
                    onIncrement = { persistentVm.processIntent(CounterIntent.Increment) },
                    onDecrement = { persistentVm.processIntent(CounterIntent.Decrement) }
                )
            }

            FeatureCard(
                title = "🔄 Refresh",
                description = "Manual refresh"
            ) {
                when (val s = refreshableState) {
                    is AsyncState.Loading<*> -> CircularProgressIndicator()
                    is AsyncState.Success<String> -> Text(s.data)
                    is AsyncState.Error<*> -> Text("Error")
                }
                Button(onClick = { container.refresh(refreshableDataProvider) }) {
                    Icon(Icons.Default.Refresh, null)
                    Text("Refresh")
                }
            }

            FeatureCard(
                title = "👥 Family",
                description = "User: $selectedUserId"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("user_1", "user_2", "error").forEach { userId ->
                        FilterChip(
                            selected = selectedUserId == userId,
                            onClick = { selectedUserId = userId },
                            label = { Text(userId) }
                        )
                    }
                }
                when (val s = userState) {
                    is AsyncState.Loading<*> -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    is AsyncState.Success<String> -> Text("✅ ${s.data}")
                    is AsyncState.Error<*> -> Text("❌ Error")
                }
            }

            FeatureCard(
                title = "📡 Stream",
                description = "Flow-based updates"
            ) {
                streamState.`when`<String, Unit>(
                    loading = { CircularProgressIndicator() },
                    data = { d -> Text(d, style = MaterialTheme.typography.headlineSmall) },
                    error = { e -> Text("Error: ${e.message}") }
                )
            }

            FeatureCard(
                title = "👂 listenProvider",
                description = "Side-effects without recomposition"
            ) {
                Text("Increment above to multiples of 5", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun CounterDemo(count: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilledTonalIconButton(onClick = onDecrement) {
            Icon(Icons.Default.KeyboardArrowDown, "Down")
        }
        Text("$count", style = MaterialTheme.typography.displayMedium)
        FilledTonalIconButton(onClick = onIncrement) {
            Icon(Icons.Default.KeyboardArrowDown, "Up", modifier = Modifier.rotate(180f))
        }
    }
}

@Composable
fun FeatureCard(title: String, description: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
