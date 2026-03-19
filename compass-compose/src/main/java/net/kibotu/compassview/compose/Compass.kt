package net.kibotu.compassview.compose

import android.hardware.Sensor
import android.hardware.SensorEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
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
    degreeColor: Color = CompassDefaults.DegreeColor,
    degreesStep: Int = CompassDefaults.DegreesStep,
    needle: Painter? = null,
    showOrientationLabels: Boolean = CompassDefaults.ShowOrientationLabels,
    orientationLabelsColor: Color = CompassDefaults.OrientationLabelsColor,
    showDegreeValue: Boolean = CompassDefaults.ShowDegreeValue,
    degreeValueColor: Color = CompassDefaults.DegreeValueColor,
    showBorder: Boolean = CompassDefaults.ShowBorder,
    borderColor: Color = CompassDefaults.BorderColor,
    onSensorChanged: ((SensorEvent) -> Unit)? = null,
    onAccuracyChanged: ((Sensor, Int) -> Unit)? = null
) {
    val animatedRotation by animateFloatAsState(
        targetValue = state.currentDegree,
        label = "compass_rotation"
    )
    
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Compass skeleton (background)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding((LocalDensity.current.run { (CompassDefaults.NeedlePadding * 100).dp }))
        ) {
            drawCompassSkeleton(
                degreeColor = degreeColor,
                degreesStep = degreesStep,
                showOrientationLabels = showOrientationLabels,
                orientationLabelsColor = orientationLabelsColor,
                showBorder = showBorder,
                borderColor = borderColor
            )
        }
        
        // Needle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = animatedRotation
                },
            contentAlignment = Alignment.Center
        ) {
            if (needle != null) {
                androidx.compose.foundation.Image(
                    painter = needle,
                    contentDescription = "Compass Needle",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Default needle using Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val needleLength = size.minDimension * 0.4f
                    
                    // Draw simple red needle pointing north
                    drawLine(
                        color = Color.Red,
                        start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                        end = androidx.compose.ui.geometry.Offset(centerX, centerY - needleLength),
                        strokeWidth = 4.dp.toPx()
                    )
                    
                    // Draw white tail pointing south
                    drawLine(
                        color = Color.White,
                        start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                        end = androidx.compose.ui.geometry.Offset(centerX, centerY + needleLength * 0.5f),
                        strokeWidth = 4.dp.toPx()
                    )
                }
            }
        }
        
        // Degree value text
        if (showDegreeValue) {
            Text(
                text = state.direction,
                color = degreeValueColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = (LocalDensity.current.run { (CompassDefaults.DataPadding * 100).dp }))
            )
        }
    }
}
