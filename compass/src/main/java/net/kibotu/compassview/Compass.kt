package net.kibotu.compassview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import java.text.DecimalFormat
import kotlin.math.roundToInt

class Compass : RelativeLayout, SensorEventListener {

    private var needleImageView: ImageView? = null
    private var degreeTextView: TextView? = null

    private var continuousDegree = 0f

    private fun normalizeAngle(angle: Float): Float = ((angle % 360f) + 360f) % 360f

    private var showBorder = false
    private var borderColor = 0

    private var degreesColor = 0
    private var showOrientationLabels = false
    private var orientationLabelsColor = 0

    private var degreeValueColor = 0
    private var _showDegreeValue = false
    var showDegreeValue: Boolean
        get() = _showDegreeValue
        set(value) {
            _showDegreeValue = value
            degreeTextView?.isVisible = value
        }

    private var degreesStep = 0

    private var needle: Drawable? = null

    private var compassListener: CompassListener? = null

    private var sensorManager: SensorManager? = null
    private var rotationVectorSensor: Sensor? = null
    private var magneticFieldSensor: Sensor? = null

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (widthMeasureSpec < heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        } else {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec)
        }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.compass_layout, this, true)

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        magneticFieldSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.Compass, 0, 0)

        typedArray.let {
            showBorder =
                typedArray.getBoolean(R.styleable.Compass_show_border, DEFAULT_SHOW_BORDER)
            borderColor =
                typedArray.getColor(R.styleable.Compass_border_color, DEFAULT_BORDER_COLOR)

            degreesColor = typedArray.getColor(
                R.styleable.Compass_degree_color,
                context.getColor(R.color.compass_degree_color)
            )
            showOrientationLabels = typedArray.getBoolean(
                R.styleable.Compass_show_orientation_labels,
                DEFAULT_SHOW_ORIENTATION_LABEL
            )
            orientationLabelsColor = typedArray.getColor(
                R.styleable.Compass_orientation_labels_color,
                context.getColor(R.color.compass_orientation_labels_color)
            )

            degreeValueColor =
                typedArray.getColor(
                    R.styleable.Compass_degree_value_color,
                    context.getColor(R.color.compass_degree_value_color)
                )
            showDegreeValue = typedArray.getBoolean(
                R.styleable.Compass_show_degree_value,
                DEFAULT_SHOW_DEGREE_VALUE
            )

            degreesStep = typedArray.getInt(R.styleable.Compass_degrees_step, DEFAULT_DEGREES_STEP)
            needle = typedArray.getDrawable(R.styleable.Compass_needle)
            typedArray.recycle()
        }

        updateLayout()
        updateNeedle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registerSensors()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unregisterSensors()
    }

    private fun registerSensors() {
        rotationVectorSensor?.let { sensor ->
            sensorManager?.let { sm ->
                sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
            }
        }
        magneticFieldSensor?.let { sensor ->
            sensorManager?.let { sm ->
                sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    private fun unregisterSensors() {
        sensorManager?.unregisterListener(this)
    }

    private fun updateLayout() {
        degreeTextView = findViewById<TextView>(R.id.tv_degree)

        val compassSkeleton = findViewById<CompassSkeleton>(R.id.compass_skeleton)
        compassSkeleton.setDegreesColor(degreesColor)
        compassSkeleton.setShowOrientationLabel(showOrientationLabels)
        compassSkeleton.setShowBorder(showBorder)
        compassSkeleton.setBorderColor(borderColor)

        try {
            compassSkeleton.setDegreesStep(degreesStep)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        compassSkeleton.setOrientationLabelsColor(orientationLabelsColor)

        val dataLayout = findViewById<View>(R.id.data_layout)
        compassSkeleton.viewTreeObserver
            .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    compassSkeleton.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val width = compassSkeleton.measuredWidth
                    val needlePadding = (width * NEEDLE_PADDING).toInt()
                    compassSkeleton.setPadding(
                        needlePadding,
                        needlePadding,
                        needlePadding,
                        needlePadding
                    )

                    val dataPaddingTop = (width * DATA_PADDING).toInt()
                    dataLayout.setPadding(0, dataPaddingTop, 0, 0)

                    val degreeTextSize = width * TEXT_SIZE_FACTOR
                    degreeTextView?.textSize = degreeTextSize
                }
            })

        degreeTextView?.setTextColor(degreeValueColor)
        degreeTextView?.isVisible = showDegreeValue
    }

    private fun updateNeedle() {
        if (needle == null) {
            needle = ContextCompat.getDrawable(context, R.drawable.ic_needle)
        }
        needleImageView = findViewById<ImageView>(R.id.iv_needle)
        needleImageView?.setImageDrawable(needle)
    }

    override fun onSensorChanged(event: SensorEvent) {
        compassListener?.onSensorChanged(event)

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val displayRotation = getDisplayRotation()
                remapRotationMatrix(displayRotation)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthInRadians = orientationAngles[0]
                val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()

                val newDegree = -azimuthInDegrees
                val normalizedPrevious = normalizeAngle(continuousDegree)
                val delta = newDegree - normalizedPrevious
                val adjustedDelta = when {
                    delta > 180 -> delta - 360
                    delta < -180 -> delta + 360
                    else -> delta
                }
                continuousDegree += adjustedDelta

                needleImageView?.rotation = continuousDegree

                updateTextDirection(continuousDegree.toDouble())
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                // Used for accuracy tracking only
            }
        }
    }

    private fun getDisplayRotation(): Int {
        return try {
            @Suppress("DEPRECATION")
            val display = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                context.display
            } else {
                (context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay
            }
            display?.rotation ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun remapRotationMatrix(displayRotation: Int) {
        val remappedMatrix = FloatArray(9)
        when (displayRotation) {
            android.view.Surface.ROTATION_0 -> {
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Y,
                    remappedMatrix
                )
            }
            android.view.Surface.ROTATION_90 -> {
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_Y,
                    SensorManager.AXIS_MINUS_X,
                    remappedMatrix
                )
            }
            android.view.Surface.ROTATION_180 -> {
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_MINUS_X,
                    SensorManager.AXIS_MINUS_Y,
                    remappedMatrix
                )
            }
            android.view.Surface.ROTATION_270 -> {
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_MINUS_Y,
                    SensorManager.AXIS_X,
                    remappedMatrix
                )
            }
            else -> {
                System.arraycopy(rotationMatrix, 0, remappedMatrix, 0, 9)
            }
        }
        System.arraycopy(remappedMatrix, 0, rotationMatrix, 0, 9)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        compassListener?.onAccuracyChanged(sensor, accuracy)
    }

    private fun updateTextDirection(degree: Double) {
        val normalizedDegree = (degree + 360) % 360
        val decimalFormat = DecimalFormat("###.#")
        val value = when {
            normalizedDegree in 0.0..22.5 || normalizedDegree in 337.5..360.0 -> "N"
            normalizedDegree in 22.5..67.5 -> "NE"
            normalizedDegree in 67.5..112.5 -> "E"
            normalizedDegree in 112.5..157.5 -> "SE"
            normalizedDegree in 157.5..202.5 -> "S"
            normalizedDegree in 202.5..247.5 -> "SW"
            normalizedDegree in 247.5..292.5 -> "W"
            normalizedDegree in 292.5..337.5 -> "NW"
            else -> "N"
        }
        degreeTextView?.text = "${decimalFormat.format(normalizedDegree)}$DEGREE $value"
    }

    fun setListener(compassListener: CompassListener?) {
        this@Compass.compassListener = compassListener
    }

    companion object {
        private const val NEEDLE_PADDING = 0.17f
        private const val DEGREE = "\u00b0"
        private const val DATA_PADDING = 0.35f
        private const val TEXT_SIZE_FACTOR = 0.014f
        private const val DEFAULT_DEGREES_STEP = 15
        private const val DEFAULT_SHOW_ORIENTATION_LABEL = false
        private const val DEFAULT_SHOW_DEGREE_VALUE = false
        private const val DEFAULT_ORIENTATION_LABEL_COLOR = Color.BLACK
        private const val DEFAULT_SHOW_BORDER = false
        private const val DEFAULT_BORDER_COLOR = Color.BLACK
    }
}