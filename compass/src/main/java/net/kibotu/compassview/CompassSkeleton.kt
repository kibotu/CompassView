package net.kibotu.compassview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.widget.RelativeLayout
import java.lang.Exception
import kotlin.math.cos
import kotlin.math.sin

internal class CompassSkeleton : RelativeLayout {
    private var compassWidth = 0
    private var centerX = 0
    private var centerY = 0

    private var degreesColor = DEGREES_COLOR
    private var showOrientationLabel = SHOW_ORIENTATION_LABEL
    private var degreesStep = DEFAULT_DEGREES_STEP
    private var orientationLabelsColor = DEFAULT_ORIENTATION_LABELS_COLOR
    private var showBorder = DEFAULT_SHOW_BORDER
    private var borderColor = DEFAULT_BORDER_COLOR

    /**
     * @param context
     */
    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    /**
     * @param context
     * @param attrs
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    /**
     * @param context
     * @param attrs
     */
    private fun init(context: Context?, attrs: AttributeSet?) {
        Log.d("TAG", "init function")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (widthMeasureSpec < heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        } else {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        compassWidth = if (height > width) width else height

        centerX = compassWidth / 2
        centerY = compassWidth / 2

        drawCompassSkeleton(canvas)
        drawOuterCircle(canvas)
    }

    /**
     * @param canvas
     */
    private fun drawOuterCircle(canvas: Canvas) {
        val strokeWidth = (compassWidth * 0.01f).toInt()
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth.toFloat()
        paint.setColor(borderColor)

        val radius = ((compassWidth / 2) - strokeWidth / 2).toFloat()

        val rectF = RectF()
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        if (showBorder) canvas.drawArc(rectF, 0f, 360f, false, paint)
    }

    /**
     * @param canvas
     */
    private fun drawCompassSkeleton(canvas: Canvas) {
        val paint = Paint()
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeCap = Paint.Cap.ROUND

        val textPaint = TextPaint()
        textPaint.textSize = compassWidth * 0.06f
        textPaint.setColor(orientationLabelsColor)

        val rect = Rect()

        val rPadded = centerX - (compassWidth * 0.01f).toInt()

        var i = 0
        while (i <= 360) {
            var rEnd: Int
            var rText: Int

            if ((i % 90) == 0) {
                rEnd = centerX - (compassWidth * 0.08f).toInt()
                rText = centerX - (compassWidth * 0.15f).toInt()
                paint.setColor(degreesColor)
                paint.strokeWidth = compassWidth * 0.02f

                showOrientationLabel(canvas, textPaint, rect, i, rText)
            } else if ((i % 45) == 0) {
                rEnd = centerX - (compassWidth * 0.06f).toInt()
                paint.setColor(degreesColor)
                paint.strokeWidth = compassWidth * 0.02f
            } else {
                rEnd = centerX - (compassWidth * 0.04f).toInt()
                paint.setColor(degreesColor)
                paint.strokeWidth = compassWidth * 0.015f
                paint.setAlpha(DEFAULT_MINIMIZED_ALPHA)
            }

            val startX = (centerX + rPadded * cos(Math.toRadians(i.toDouble()))).toInt()
            val startY = (centerX - rPadded * sin(Math.toRadians(i.toDouble()))).toInt()

            val stopX = (centerX + rEnd * cos(Math.toRadians(i.toDouble()))).toInt()
            val stopY = (centerX - rEnd * sin(Math.toRadians(i.toDouble()))).toInt()


            canvas.drawLine(
                startX.toFloat(),
                startY.toFloat(),
                stopX.toFloat(),
                stopY.toFloat(),
                paint
            )

            i = i + degreesStep
        }
    }

    /**
     * @param canvas
     * @param textPaint
     * @param rect
     * @param i
     * @param rText
     */
    private fun showOrientationLabel(
        canvas: Canvas,
        textPaint: TextPaint,
        rect: Rect,
        i: Int,
        rText: Int
    ) {
        if (showOrientationLabel) {
            val textX = (centerX + rText * cos(Math.toRadians(i.toDouble()))).toInt()
            val textY = (centerX - rText * sin(Math.toRadians(i.toDouble()))).toInt()

            var direction = EAST_INDEX
            if (i == 0) {
                direction = EAST_INDEX
            } else if (i == 90) {
                direction = NORTH_INDEX
            } else if (i == 180) {
                direction = WEST_INDEX
            } else if (i == 270) {
                direction = SOUTH_INDEX
            }

            textPaint.getTextBounds(direction, 0, 1, rect)
            canvas.drawText(
                direction,
                (textX - rect.width() / 2).toFloat(),
                (textY + rect.height() / 2).toFloat(),
                textPaint
            )
        }
    }

    /**
     * @param degreesColor
     */
    fun setDegreesColor(degreesColor: Int) {
        this@CompassSkeleton.degreesColor = degreesColor
        invalidate()
    }

    /**
     * @param showOrientationLabel
     */
    fun setShowOrientationLabel(showOrientationLabel: Boolean) {
        this@CompassSkeleton.showOrientationLabel = showOrientationLabel
        invalidate()
    }

    /**
     * @param degreesStep
     * @throws Exception
     */
    @Throws(Exception::class)
    fun setDegreesStep(degreesStep: Int) {
        if (degreesStep > 360 || degreesStep < 0 || 360 % degreesStep != 0) {
            throw Exception("Degree step is invalid")
        }
        this@CompassSkeleton.degreesStep = degreesStep
        invalidate()
    }

    /**
     * @param orientationLabelsColor
     */
    fun setOrientationLabelsColor(orientationLabelsColor: Int) {
        this@CompassSkeleton.orientationLabelsColor = orientationLabelsColor
        invalidate()
    }

    /**
     * @param showBorder
     */
    fun setShowBorder(showBorder: Boolean) {
        this@CompassSkeleton.showBorder = showBorder
        invalidate()
    }

    /**
     * @param borderColor
     */
    fun setBorderColor(borderColor: Int) {
        this@CompassSkeleton.borderColor = borderColor
        invalidate()
    }

    companion object {
        private val DEGREES_COLOR = Color.BLACK
        private const val SHOW_ORIENTATION_LABEL = false
        private const val DEFAULT_DEGREES_STEP = 15
        private val DEFAULT_BORDER_COLOR = Color.BLACK

        private const val EAST_INDEX = "E"
        private const val NORTH_INDEX = "N"
        private const val WEST_INDEX = "W"
        private const val SOUTH_INDEX = "S"
        private const val DEFAULT_MINIMIZED_ALPHA = 180
        private val DEFAULT_ORIENTATION_LABELS_COLOR = Color.BLACK
        private const val DEFAULT_SHOW_BORDER = false
    }
}
