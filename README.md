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

## ✨ Features Overview

| Feature | What It Does | When to Use |
|---------|-------------|-------------|
| **🎯 MVI Architecture** | Enforces clean State/Intent/ViewModel separation | Business logic & UI state management |
| **📡 StateNotifierProvider** | Creates observable ViewModels | Main state container for screens |
| **⚡ FutureProvider** | Handles async operations with loading/error states | API calls, database queries |
| **🧬 Family Modifier** | Creates parameterized providers (cached per argument) | Dynamic data (user profiles by ID) |
| **🗑️ AutoDispose** | Auto-destroys providers when not in use | Screen-scoped temporary data |
| **🔍 Select** | Watch only specific state fields | Micro-recompositions (performance) |
| **🔧 Override** | Replace providers for testing/mocking | Unit tests, preview data |
| **👁️ ProviderObserver** | Lifecycle callbacks (create/update/dispose) | Analytics, debugging, logging |

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

---

## 🔥 Advanced Features

### FutureProvider (Async Operations)
```kotlin
val userProvider = FutureProvider.family<String, User> { ref, userId ->
    api.getUser(userId) // Suspend function
}

@Composable
fun UserProfile(userId: String) {
    val userState by watchProvider(userProvider(userId))
    
    when (userState) {
        is AsyncState.Loading -> CircularProgressIndicator()
        is AsyncState.Success -> Text(userState.data.name)
        is AsyncState.Error -> Text("Error: ${userState.throwable.message}")
    }
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

### ProviderObserver (Lifecycle)
```kotlin
class LoggingObserver : ProviderObserver {
    override fun <T> didAddProvider(provider: ProviderBase<T>, value: T, container: ProviderContainer) {
        Log.d("Pod", "Created: ${provider.name}")
    }
    
    override fun <T> didDisposeProvider(provider: ProviderBase<T>, container: ProviderContainer) {
        Log.d("Pod", "Destroyed: ${provider.name}")
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
| `provider { }` | Simple values/config | — |
| `stateNotifierProvider { }` | MVI ViewModels | State flow, recomposition |
| `FutureProvider { }` | Async operations | Loading, Success, Error states |
| `FutureProvider.family { }` | Parameterized async | Caching per argument |

---

## 💡 Why ComposePod?

| Problem | ComposePod Solution |
|---------|---------------------|
| ViewModel passing nightmare | Global providers accessible anywhere |
| Unnecessary recompositions | `.select { }` for micro-recompositions |
| Boilerplate DI setup | No Dagger/Hilt needed |
| Memory leaks | `.autoDispose()` auto-cleanup |
| Async state mess | `AsyncState<T>` handles loading/error |
| Hard to test | `overrideWith()` for mocking |

---

## 🤝 Contributing
Contributions welcome!

## 📜 License
Distributed under the MIT License.
