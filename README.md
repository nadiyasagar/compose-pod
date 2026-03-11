<h1 align="center">ComposePod 🚀</h1>

<p align="center">
  <strong>A Production-Grade, Clean Architecture State Management Library for Jetpack Compose</strong>
</p>

<p align="center">
  <a href="https://jitpack.io/#nadiyasagar/compose-pod">
    <img src="https://jitpack.io/v/nadiyasagar/compose-pod.svg" alt="JitPack Badge" />
  </a>
</p>

<hr>

**ComposePod** is a modern state management library crafted specifically for **Jetpack Compose**. Inspired by Flutter's Riverpod, it brings powerful Dependency Injection, MVI (Model-View-Intent) architecture, and Coroutine-based robust state handling securely into your Compose applications.

## ✨ Features

- **Built for Jetpack Compose:** Seamlessly integrates with the Compose lifecycle using robust `ProviderScope` and intuitive composables.
- **MVI Architecture Out of the Box:** Enforces a clean separation between UI components and business logic using explicit States and Intents.
- **Micro-Recompositions `select()`:** Watch only specific parts of your state, eliminating unnecessary UI recompositions automatically!
- **Asynchronous State Handling:** First-class support for loading, data, and error states using built-in `AsyncState` models.
- **Clean Dependency Injection:** Create globally accessible, yet locally scoped `Providers` without heavy DI frameworks like Dagger or Hilt.
- **Test-Friendly:** Business logic remains purely Kotlin (`MVIViewModel`), making unit testing effortless.

---

## 📦 Installation

ComposePod is hosted on **JitPack**.

### 1. Add JitPack repository
In your root `settings.gradle.kts` (or `build.gradle`), include the JitPack repository:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // <-- Add this
    }
}
```

### 2. Add the dependency
In your module's `build.gradle.kts` (e.g., `app/build.gradle.kts`):

```kotlin
dependencies {
    implementation("com.github.nadiyasagar:compose-pod:1.0.1")
}
```

---

## 🛠️ Core Concepts

### 1. The `ProviderScope`
For ComposePod to manage your providers, you must wrap your root compose tree in a `ProviderScope`.

```kotlin
setContent {
    ComposePodTheme {
        ProviderScope {
            NotesAppScreen()
        }
    }
}
```

### 2. Defining States and Intents
Follow the strict **MVI (Model-View-Intent)** guidelines by defining your State and Intents:

```kotlin
data class NotesState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false
) : UiState

sealed class NotesIntent : UiIntent {
    data class AddNote(val title: String, val content: String) : NotesIntent()
    data class DeleteNote(val id: String) : NotesIntent()
}
```

### 3. The `MVIViewModel`
Extend your business logic processor from `MVIViewModel`:

```kotlin
class NotesViewModel : MVIViewModel<NotesState, NotesIntent>(
    initialState = NotesState()
) {
    override fun processIntent(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.AddNote -> {
                // Update State seamlessly!
                state = state.copy(
                    notes = state.notes + Note(UUID.randomUUID().toString(), intent.title, intent.content)
                )
            }
            is NotesIntent.DeleteNote -> {
                state = state.copy(
                    notes = state.notes.filter { it.id != intent.id }
                )
            }
        }
    }
}
```

### 4. Creating a Provider
Expose your ViewModel globally through a powerful `StateNotifierProvider`:

```kotlin
val notesProvider = stateNotifierProvider {
    NotesViewModel()
}
```

---

## 🚀 Usage in Compose

ComposePod offers two primary ways to interact with your providers inside composables:

### Reading State (`watchProvider`)
To reactively rebuild your UI when the state changes:

```kotlin
@Composable
fun NotesScreen() {
    // ❌ Bad: Watches the ENTIRE state. Any change triggers recomposition.
    // val state by watchProvider(notesProvider)
    
    // ✅ Good: Micro-Recomposition! Only triggers when `notes` list itself changes. 
    val notes by watchProvider(notesProvider.select { it.notes })

    LazyColumn {
        items(notes) { note ->
            Text(note.title)
        }
    }
}
```

### Dispatching Actions (`rememberProvider`)
To dispatch intents *without* triggering recompositions when the state changes:

```kotlin
@Composable
fun AddNoteButton() {
    // Gets the ViewModel instance WITHOUT listening to state changes
    val viewModel = rememberProvider(notesProvider)

    Button(onClick = { viewModel.processIntent(NotesIntent.AddNote("Hello", "World")) }) {
        Text("Save Note")
    }
}
```

---

## 🔥 Why ComposePod?

While Google recommends `ViewModel` and `StateFlow`, things get messy in large apps:
1. **Passing ViewModels down the tree** becomes painful. ComposePod's global `val myProvider` solves this cleanly without Hilt.
2. **Recomposition bloat**: Native `collectAsState()` rebuilds the entire screen if one tiny boolean in your state class flips. Using ComposePod's `.select { }` fixes this elegantly!
3. **MVI strictness**: Easily trace bugs by centralizing every state modification inside your `processIntent` function.

---

## 🤝 Contributing
Contributions, issues, and feature requests are welcome!

## 📜 License
Distributed under the MIT License.
