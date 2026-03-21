package com.example.composepod

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.brine.composepod.compose.ProviderScope
import com.brine.composepod.core.ProviderBase
import com.brine.composepod.core.ProviderContainer
import com.brine.composepod.core.ProviderObserver
import com.brine.composepod.core.overrideWith
import com.example.composepod.demo.ui.NotesAppScreen
import com.example.composepod.demo.di.applicationContextProvider
import com.example.composepod.ui.theme.ComposePodTheme
import android.util.Log

class LoggingObserver : ProviderObserver {
    override fun <T> didAddProvider(provider: ProviderBase<T>, value: T, container: ProviderContainer) {
        Log.d("ComposePodObserver", "Provider Added: ${provider.name}, Value: $value")
    }

    override fun <T> didUpdateProvider(provider: ProviderBase<T>, previousValue: T?, newValue: T, container: ProviderContainer) {
        Log.d("ComposePodObserver", "Provider Updated: ${provider.name}, New Value: $newValue")
    }

    override fun <T> didDisposeProvider(provider: ProviderBase<T>, container: ProviderContainer) {
        Log.d("ComposePodObserver", "Provider Disposed: ${provider.name}")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposePodTheme {
                ProviderScope(
                    observers = listOf(LoggingObserver()),
                    overrides = listOf(
                        applicationContextProvider.overrideWith { applicationContext }
                    )
                ) {
                    NotesAppScreen()
                }
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposePodTheme {
        Greeting("Android")
    }
}