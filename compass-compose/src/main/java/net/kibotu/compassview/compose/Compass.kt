package net.kibotu.compassview.compose

import android.hardware.Sensor
import android.hardware.SensorEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A Compass widget that displays a rotating compass with customizable appearance.
 *
 * @param modifier The modifier to be applied to the compass.
 * @param state The state holder for the compass, managing sensor data and rotation.
 * @param degreeColor The color of the degree markers on the compass.
 * @param degreesStep The step between degree markers (must be > 0 and < 360, and 360 must be divisible by it).
 * @param needle Optional custom painter for the compass needle. If null, a default needle is drawn.
 * @param showOrientationLabels Whether to show orientation labels (N, E, S, W).
 * @param orientationLabelsColor The color of the orientation labels.
 * @param showDegreeValue Whether to show the current degree value as text.
 * @param degreeValueColor The color of the degree value text.
 * @param showBorder Whether to show an outer border circle.
 * @param borderColor The color of the border circle.
 * @param onSensorChanged Optional callback invoked when sensor data changes.
 * @param onAccuracyChanged Optional callback invoked when sensor accuracy changes.
 */
@Composable
fun Compass(
    modifier: Modifier = Modifier,
    state: CompassState = rememberCompassState(),
    degreeColor: Color = MaterialTheme.colorScheme.onSurface,
    degreesStep: Int = CompassDefaults.DegreesStep,
    showOrientationLabels: Boolean = CompassDefaults.ShowOrientationLabels,
    orientationLabelsColor: Color = MaterialTheme.colorScheme.onSurface,
    showDegreeValue: Boolean = CompassDefaults.ShowDegreeValue,
    degreeValueColor: Color = MaterialTheme.colorScheme.onSurface,
    showBorder: Boolean = CompassDefaults.ShowBorder,
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
    onSensorChanged: ((SensorEvent) -> Unit)? = null,
    onAccuracyChanged: ((Sensor, Int) -> Unit)? = null
) {
    val animatedRotation by animateFloatAsState(
        targetValue = state.currentDegree,
        label = "compass_rotation"
    )
    
    BoxWithConstraints(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val compassSize = minOf(maxWidth, maxHeight)
        
        CompassSkeleton(
            modifier = Modifier.fillMaxSize(),
            degreeColor = degreeColor,
            degreesStep = degreesStep,
            showOrientationLabels = showOrientationLabels,
            orientationLabelsColor = orientationLabelsColor,
            showBorder = showBorder,
            borderColor = borderColor
        )
        
        Image(
            painter = painterResource(R.drawable.ic_needle),
            contentDescription = "Compass Needle",
            modifier = Modifier
                .fillMaxSize()
                .padding(64.dp)
                .graphicsLayer {
                    rotationZ = animatedRotation
                },
            contentScale = ContentScale.Fit
        )
        
        if (showDegreeValue) {
            Text(
                text = state.direction,
                color = degreeValueColor,
                fontSize = (compassSize.value * CompassDefaults.TextSizeFactor).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (compassSize.value * CompassDefaults.DataPadding).dp)
            )
        }
    }
}
