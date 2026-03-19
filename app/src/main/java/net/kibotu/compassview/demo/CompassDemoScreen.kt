package net.kibotu.compassview.demo

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import net.kibotu.compassview.Compass
import net.kibotu.compassview.compose.Compass as ComposeCompass

@Composable
fun CompassDemoScreen() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding()
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CompassSection(
                    title = "Jetpack Compose",
                    modifier = Modifier.weight(1f)
                )
                CompassSection(
                    title = "Android View",
                    modifier = Modifier.weight(1f),
                    useAndroidView = true
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CompassSection(
                    title = "Jetpack Compose",
                    modifier = Modifier.weight(1f)
                )
                CompassSection(
                    title = "Android View",
                    modifier = Modifier.weight(1f),
                    useAndroidView = true
                )
            }
        }
    }
}

@Composable
private fun CompassSection(
    title: String,
    modifier: Modifier = Modifier,
    useAndroidView: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (useAndroidView) {
            AndroidViewCompass(modifier = Modifier.fillMaxWidth())
        } else {
            ComposeCompass(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ComposeCompass(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        net.kibotu.compassview.compose.Compass(
            modifier = Modifier.fillMaxSize(),
            showDegreeValue = true
        )
    }
}

@Composable
private fun AndroidViewCompass(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                Compass(ctx).apply {
                    showDegreeValue = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
