package net.kibotu.compassview.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun CompassSkeleton(
    modifier: Modifier = Modifier,
    degreeColor: Color,
    degreesStep: Int,
    showOrientationLabels: Boolean,
    orientationLabelsColor: Color,
    showBorder: Boolean,
    borderColor: Color
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawCompassSkeleton(
            degreeColor = degreeColor,
            degreesStep = degreesStep,
            showOrientationLabels = showOrientationLabels,
            orientationLabelsColor = orientationLabelsColor,
            showBorder = showBorder,
            borderColor = borderColor
        )
    }
}

internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCompassSkeleton(
    degreeColor: Color,
    degreesStep: Int,
    showOrientationLabels: Boolean,
    orientationLabelsColor: Color,
    showBorder: Boolean,
    borderColor: Color
) {
    val compassWidth = size.minDimension
    val centerX = compassWidth / 2
    val centerY = compassWidth / 2
    
    val rPadded = centerX - (compassWidth * 0.01f)
    
    var i = 0
    while (i <= 360) {
        val rEnd: Float
        val rText: Float
        val strokeWidth: Float
        val alpha: Int
        
        when {
            (i % 90) == 0 -> {
                rEnd = centerX - (compassWidth * 0.08f)
                rText = centerX - (compassWidth * 0.15f)
                strokeWidth = compassWidth * 0.02f
                alpha = 255
                
                if (showOrientationLabels) {
                    drawOrientationLabel(i, rText, centerX, centerY, orientationLabelsColor, compassWidth)
                }
            }
            (i % 45) == 0 -> {
                rEnd = centerX - (compassWidth * 0.06f)
                strokeWidth = compassWidth * 0.02f
                alpha = 255
            }
            else -> {
                rEnd = centerX - (compassWidth * 0.04f)
                strokeWidth = compassWidth * 0.015f
                alpha = CompassDefaults.MinimizedAlpha
            }
        }
        
        val startX = centerX + rPadded * cos(Math.toRadians(i.toDouble())).toFloat()
        val startY = centerX - rPadded * sin(Math.toRadians(i.toDouble())).toFloat()
        
        val stopX = centerX + rEnd * cos(Math.toRadians(i.toDouble())).toFloat()
        val stopY = centerX - rEnd * sin(Math.toRadians(i.toDouble())).toFloat()
        
        drawLine(
            color = degreeColor.copy(alpha = alpha / 255f),
            start = Offset(startX, startY),
            end = Offset(stopX, stopY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        i += degreesStep
    }
    
    if (showBorder) {
        val strokeWidth = (compassWidth * 0.01f)
        val radius = (compassWidth / 2) - strokeWidth / 2
        
        drawCircle(
            color = borderColor,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = strokeWidth)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOrientationLabel(
    degree: Int,
    rText: Float,
    centerX: Float,
    centerY: Float,
    color: Color,
    compassWidth: Float
) {
    val textX = centerX + rText * cos(Math.toRadians(degree.toDouble())).toFloat()
    val textY = centerY - rText * sin(Math.toRadians(degree.toDouble())).toFloat()
    
    val direction = when (degree) {
        0 -> "E"
        90 -> "N"
        180 -> "W"
        270 -> "S"
        else -> return
    }
    
    val textSize = compassWidth * 0.06f
    val paint = android.graphics.Paint().apply {
        this.color = color.toArgb()
        this.textSize = textSize
        textAlign = android.graphics.Paint.Align.CENTER
    }
    
    drawContext.canvas.nativeCanvas.drawText(
        direction,
        textX,
        textY + textSize / 3,
        paint
    )
}
