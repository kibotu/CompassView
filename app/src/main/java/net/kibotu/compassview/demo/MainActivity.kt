package net.kibotu.compassview.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val systemDarkTheme = isSystemInDarkTheme()
            var darkTheme by remember { mutableStateOf(systemDarkTheme) }
            
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
            }
            MaterialTheme(
                colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CompassDemoScreen(
                        onToggleTheme = { darkTheme = !darkTheme }
                    )
                }
            }
        }
    }
}
