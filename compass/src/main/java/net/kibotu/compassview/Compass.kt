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
import java.lang.Exception
import java.text.DecimalFormat
import kotlin.math.roundToInt

class Compass : RelativeLayout, SensorEventListener {

    private var needleImageView: ImageView? = null
    private var degreeTextView: TextView? = null

    private var currentDegree = 0f

    private var showBorder = false
    private var borderColor = 0

    private var degreesColor = 0
    private var showOrientationLabels = false
    private var orientationLabelsColor = 0

    private var degreeValueColor = 0
    private var showDegreeValue = false

    private var degreesStep = 0

    private var needle: Drawable? = null

    private var compassListener: CompassListener? = null

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

        val mSensorManager = getContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager.registerListener(
            this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_GAME
        )

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.Compass, 0, 0)

        if (typedArray != null) {
            showBorder =
                typedArray.getBoolean(R.styleable.Compass_show_border, DEFAULT_SHOW_BORDER)
            borderColor =
                typedArray.getColor(R.styleable.Compass_border_color, DEFAULT_BORDER_COLOR)

            degreesColor = typedArray.getColor(R.styleable.Compass_degree_color, Color.BLACK)
            showOrientationLabels = typedArray.getBoolean(
                R.styleable.Compass_show_orientation_labels,
                DEFAULT_SHOW_ORIENTATION_LABEL
            )
            orientationLabelsColor = typedArray.getColor(
                R.styleable.Compass_orientation_labels_color,
                DEFAULT_ORIENTATION_LABEL_COLOR
            )

            degreeValueColor =
                typedArray.getColor(R.styleable.Compass_degree_value_color, Color.BLACK)
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
        compassSkeleton.getViewTreeObserver()
            .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    compassSkeleton.getViewTreeObserver().removeOnGlobalLayoutListener(this)
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

        val degree = event.values[0].roundToInt().toFloat()

        val rotateAnimation = RotateAnimation(
            currentDegree,
            -degree,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotateAnimation.setDuration(210)
        rotateAnimation.fillAfter = true
        needleImageView?.startAnimation(rotateAnimation)

        updateTextDirection(currentDegree.toDouble())

        currentDegree = -degree
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        compassListener?.onAccuracyChanged(sensor, accuracy)
    }

    private fun updateTextDirection(degree: Double) {
        val deg = 360 + degree
        val decimalFormat = DecimalFormat("###.#")
        var value: String?
        if (deg > 0 && deg <= 90) {
            value = String.format("%s%s NE", decimalFormat.format(-degree).toString(), DEGREE)
        } else if (deg > 90 && deg <= 180) {
            value = String.format("%s%s ES", decimalFormat.format(-degree).toString(), DEGREE)
        } else if (deg > 180 && deg <= 270) {
            value = String.format("%s%s SW", decimalFormat.format(-degree).toString(), DEGREE)
        } else {
            value = String.format("%s%s WN", decimalFormat.format(-degree).toString(), DEGREE)
        }
        degreeTextView?.text = value
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

