<h1 align="center">ComposePod 🚀</h1>

<p align="center">
  <strong>A Production-Grade State Management Library for Jetpack Compose</strong>
</p>

<p align="center">
  <a href="https://jitpack.io/#nadiyasagar/compose-pod">
    <img src="https://jitpack.io/v/nadiyasagar/compose-pod.svg" alt="JitPack Badge" />
  </a>
</p>

---

**ComposePod** is a modern state management library inspired by Flutter's Riverpod. It combines **MVI architecture**, **Dependency Injection**, and **reactive state handling** into a simple, test-friendly solution for Jetpack Compose.

---

| Feature | What It Does | When to Use |
|---------|-------------|-------------|
| **🎯 MVI Architecture** | Enforces clean State/Intent/ViewModel separation | Business logic & UI state management |
| **📡 StateNotifierProvider** | Creates observable ViewModels | Main state container for screens |
| **⚡ FutureProvider** | Handles async operations with loading/error states | API calls, database queries |
| **🌊 StreamProvider** | Listens to reactive data streams | Firebase, WebSockets, real-time data |
| **🧮 ComputedProvider** | Derives state from other providers | Calculations, filtered lists |
| **🧬 Family Modifier** | Creates parameterized providers | Dynamic data (user profiles by ID) |
| **🗑️ AutoDispose** | Auto-destroys providers when not in use | Screen-scoped temporary data |
| **🔍 Select** | Watch only specific state fields | Micro-recompositions (performance) |
| **🔧 Override** | Replace providers for testing/mocking | Unit tests, preview data |

---

## 📦 Installation

### 1. Add JitPack Repository
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add Dependency
```kotlin
dependencies {
    implementation("com.github.nadiyasagar:compose-pod:1.0.3")
}
```

---

## 🛠️ Core Concepts

### 1. Wrap Your App with `ProviderScope`
```kotlin
setContent {
    ProviderScope {
        YourApp()
    }
}
```

### 2. Define State & Intents (MVI)
```kotlin
data class NotesState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false
) : UiState

sealed class NotesIntent : UiIntent {
    data class AddNote(val title: String) : NotesIntent()
    object LoadNotes : NotesIntent()
}
```

### 3. Create ViewModel
```kotlin
class NotesViewModel : MVIViewModel<NotesState, NotesIntent>(
    initialState = NotesState()
) {
    override fun processIntent(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.AddNote -> {
                state = state.copy(
                    notes = state.notes + Note(intent.title)
                )
            }
            // ...
        }
    }
}
```

### 4. Create Provider
```kotlin
val notesProvider = stateNotifierProvider {
    NotesViewModel()
}
```

---

## 🚀 Usage Guide

### Watch State (Triggers Recomposition)
```kotlin
@Composable
fun NotesScreen() {
    // Watch entire state
    val state by watchProvider(notesProvider)
    
    // Watch only specific field (better performance!)
    val notes by watchProvider(notesProvider.select { it.notes })
}
```

### Dispatch Actions (No Recomposition)
```kotlin
@Composable
fun AddButton() {
    val viewModel = rememberProvider(notesProvider)
    
    Button(onClick = { 
        viewModel.processIntent(NotesIntent.AddNote("Hello"))
    }) {
        Text("Add")
    }
}
```

### Side-Effects (Snackbars, Navigation)
Use the fluent `.listen` API to handle side-effects without triggering recomposition.
```kotlin
@Composable
fun NotesScreen() {
    notesProvider.listen { previous, next ->
        if (next.notes.size > (previous?.notes?.size ?: 0)) {
            // Show snackbar or navigate
        }
    }
}
```

---

## 🔥 Advanced Features

### FutureProvider & StreamProvider (Async)
ComposePod handles async state elegantly with `AsyncState.when`.

```kotlin
val userProvider = FutureProvider.family<String, User> { ref, userId ->
    api.getUser(userId) 
}

val chatProvider = StreamProvider { ref ->
    repository.observeMessages() // Returns Flow<List<Message>>
}

@Composable
fun UserProfile(userId: String) {
    val userState by watchProvider(userProvider(userId))
    
    userState.`when`(
        loading = { CircularProgressIndicator() },
        success = { user -> Text("Hello, ${user.name}") },
        error = { error -> Text("Failed: ${error.message}") }
    )
}
```

### ComputedProvider (Deriving State)
Automatically re-computes when dependencies change.

```kotlin
val counterProvider = stateProvider { 0 }

val doubledProvider = computedProvider { ref ->
    val count = ref.watch(counterProvider)
    count * 2
}
```

### AutoDispose (Screen-Scoped)
```kotlin
val counterProvider = stateNotifierProvider {
    CounterViewModel()
}.autoDispose() // Destroyed when screen closes
```

### Override Providers (Testing)
```kotlin
ProviderScope(
    overrides = listOf(
        apiProvider.overrideWith { FakeApi() },
        userProvider.overrideWith { mockUser }
    )
) {
    // All overridden providers used here
}
```

### Lifecycle Hooks
Manage external resources directly within your providers.

```kotlin
val socketProvider = provider { ref ->
    val socket = SocketClient().connect()
    
    ref.onDispose { socket.disconnect() }
    ref.onCancel { socket.pause() }
    ref.onResume { socket.resume() }
    
    socket
}
```

### ProviderObserver (Lifecycle Logging)
```kotlin
class LoggingObserver : ProviderObserver {
    override fun <T> didUpdateProvider(
        provider: ProviderBase<T>, 
        previousValue: T?, 
        newValue: T, 
        container: ProviderContainer
    ) {
        Log.d("Pod", "${provider.name} updated: $newValue")
    }
}

ProviderScope(observers = listOf(LoggingObserver())) {
    // App content
}
```

---

## 📊 Provider Types Comparison

| Provider | Use Case | Auto-Handles |
|----------|----------|--------------|
| `provider { }` | Simple values/config | Dependency Injection |
| `stateNotifier { }` | MVI ViewModels | StateFlow & UI Listeners |
| `FutureProvider { }` | Single async tasks | Loading/Success/Error |
| `StreamProvider { }` | Reactive real-time data | Flow collection, cleanup |
| `computedProvider { }` | Derived/filtered state | Automatic re-computation |
| `.family` | Parameterized state | Caching per argument |

---

## 💡 Why ComposePod?

| Problem | ComposePod Solution |
|---------|---------------------|
| Boilerplate DI setup | No Dagger/Hilt needed (Global Providers) |
| ViewModel passing | Accessible anywhere via `watch`/`remember` |
| UI Performance | `.select { }` for micro-recompositions |
| Memory leaks | `.autoDispose()` auto-cleanup & lifecycle hooks |
| State Threading | **Production-Ready**: Thread-safe state updates |
| App Crashes | **Circular dependency protection** built-in |
| Hard to test | `overrideWith()` for instant mocking |

---

## 🤝 Contributing
Contributions welcome!

## 📜 License
Distributed under the MIT License.
