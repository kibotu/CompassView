package net.kibotu.compassview.compose

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.text.DecimalFormat
import kotlin.math.roundToInt

@Stable
class CompassState(
    private val context: Context,
    private val onSensorChanged: ((SensorEvent) -> Unit)? = null,
    private val onAccuracyChanged: ((Sensor, Int) -> Unit)? = null
) : SensorEventListener {
    
    var currentDegree by mutableFloatStateOf(0f)
        private set
    
    val direction by derivedStateOf {
        calculateDirection(currentDegree.toDouble())
    }
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
    
    fun start() {
        sensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }
    
    fun stop() {
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        onSensorChanged?.invoke(event)
        val degree = event.values[0].roundToInt().toFloat()
        currentDegree = -degree
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        onAccuracyChanged?.invoke(sensor, accuracy)
    }
    
    private fun calculateDirection(degree: Double): String {
        val deg = 360 + degree
        val decimalFormat = DecimalFormat("###.#")
        return when {
            deg in 0.0..90.0 -> "${decimalFormat.format(-degree)}° NE"
            deg in 90.0..180.0 -> "${decimalFormat.format(-degree)}° ES"
            deg in 180.0..270.0 -> "${decimalFormat.format(-degree)}° SW"
            else -> "${decimalFormat.format(-degree)}° WN"
        }
    }
}

@Composable
fun rememberCompassState(
    onSensorChanged: ((SensorEvent) -> Unit)? = null,
    onAccuracyChanged: ((Sensor, Int) -> Unit)? = null
): CompassState {
    val context = LocalContext.current
    val state = remember(context) {
        CompassState(context, onSensorChanged, onAccuracyChanged)
    }
    
    DisposableEffect(state) {
        state.start()
        onDispose {
            state.stop()
        }
    }
    
    return state
}
