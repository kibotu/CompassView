package net.kibotu.compassview.compose

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.text.DecimalFormat

@Stable
class CompassState(
    private val context: Context,
    private val onSensorChanged: ((SensorEvent) -> Unit)? = null,
    private val onAccuracyChanged: ((Sensor, Int) -> Unit)? = null
) : SensorEventListener {

    var continuousDegree by mutableFloatStateOf(0f)
        private set

    val currentDegree: Float
        get() = continuousDegree

    val displayDegree: Float
        get() = ((continuousDegree % 360f) + 360f) % 360f

    var accuracy by mutableIntStateOf(SensorManager.SENSOR_STATUS_NO_CONTACT)
        private set

    val azimuth: Azimuth by derivedStateOf { Azimuth(displayDegree) }

    val direction by derivedStateOf {
        val decimalFormat = DecimalFormat("###.#")
        "${decimalFormat.format(azimuth.degrees)}° ${azimuth.cardinalDirection.label}"
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var isStarted = false

    fun start() {
        if (isStarted) return

        var hasErrors = false

        rotationVectorSensor?.let { sensor ->
            val success = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
            if (!success) {
                hasErrors = true
            }
        } ?: run { hasErrors = true }

        magneticFieldSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        isStarted = true
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        isStarted = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        onSensorChanged?.invoke(event)

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val displayRotation = getDisplayRotation()
                remapRotationMatrix(displayRotation)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthInRadians = orientationAngles[0]
                val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
                val newDegree = -azimuthInDegrees

                val rawDelta = newDegree - normalizeAngle(continuousDegree)
                val adjustedDelta = when {
                    rawDelta > 180 -> rawDelta - 360
                    rawDelta < -180 -> rawDelta + 360
                    else -> rawDelta
                }
                continuousDegree += adjustedDelta
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                // Used for accuracy tracking only
            }
        }
    }

    private fun normalizeAngle(angle: Float): Float = ((angle % 360f) + 360f) % 360f

    private fun getDisplayRotation(): Int {
        return try {
            val display = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                context.display
            } else {
                @Suppress("DEPRECATION")
                (context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay
            }
            display?.rotation ?: Surface.ROTATION_0
        } catch (e: Exception) {
            Surface.ROTATION_0
        }
    }

    private fun remapRotationMatrix(displayRotation: Int) {
        val remappedMatrix = FloatArray(9)
        when (displayRotation) {
            Surface.ROTATION_0 -> {
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, remappedMatrix)
            }
            Surface.ROTATION_90 -> {
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, remappedMatrix)
            }
            Surface.ROTATION_180 -> {
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, remappedMatrix)
            }
            Surface.ROTATION_270 -> {
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, remappedMatrix)
            }
            else -> {
                System.arraycopy(rotationMatrix, 0, remappedMatrix, 0, 9)
            }
        }
        System.arraycopy(remappedMatrix, 0, rotationMatrix, 0, 9)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        when (sensor.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> {
                this.accuracy = accuracy
                onAccuracyChanged?.invoke(sensor, accuracy)
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                // Rotation vector doesn't have meaningful accuracy
            }
        }
    }
}

@Composable
fun rememberCompassState(
    onSensorChanged: ((SensorEvent) -> Unit)? = null,
    onAccuracyChanged: ((Sensor, Int) -> Unit)? = null
): CompassState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val state = remember(context, onSensorChanged, onAccuracyChanged) {
        CompassState(context, onSensorChanged, onAccuracyChanged)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> state.start()
                Lifecycle.Event.ON_PAUSE -> state.stop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            state.stop()
        }
    }

    return state
}