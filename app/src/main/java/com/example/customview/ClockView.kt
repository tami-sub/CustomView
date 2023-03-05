package com.example.customview

import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import java.util.*
import kotlin.math.*

class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private var fontSize: Float = 0f,
    private var hourHandColor: Int = 0,
    private var hourStrokeWidth: Float = 0f,
    private var minuteHandColor: Int = 0,
    private var minuteStrokeWidth: Float = 0f,
    private var secondHandColor: Int = 0,
    private var secondStrokeWidth: Float = 0f,
    private var digitSize: Float = 0f,
    private var digitColor: Int = 0,
    private var radiusPercent: Float = 0f,
    private var circleColor: Int = 0,
    private var circleStyle: String = "",
) : View(context, attrs, defStyleAttr) {

    private val viewPaddingBySides = 50
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var currentState: Int = 0
    private var radius: Float = 0f

    private fun paintStroke(styleAttr: String) = Paint().apply {
        isAntiAlias = true
        color = circleColor
        strokeWidth = 25f
        style = when (styleAttr) {
            "fill" -> {
                Paint.Style.FILL
            }
            "all" -> {
                Paint.Style.FILL_AND_STROKE
            }
            else -> {
                Paint.Style.STROKE
            }
        }
    }

    private fun paintFill() = Paint().apply {
        isAntiAlias = true
        color = digitColor
        style = Paint.Style.FILL
        strokeWidth = 5f
        textSize = fontSize
    }

    private fun paintHandle(str: String) = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        when (str) {
            "hour" -> {
                color = hourHandColor
                strokeWidth = hourStrokeWidth
            }
            "minute" -> {
                color = minuteHandColor
                strokeWidth = minuteStrokeWidth
            }
            "second" -> {
                color = secondHandColor
                strokeWidth = secondStrokeWidth
            }
        }

    }

    init {
        setBackgroundColor(Color.WHITE)
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ClockView)
        if (hourHandColor == 0) hourHandColor =
            attributes.getColor(R.styleable.ClockView_hourHandColor, Color.BLACK)
        if (hourStrokeWidth == 0f) hourStrokeWidth =
            attributes.getDimension(R.styleable.ClockView_hourStrokeWidth, 20f)
        if (minuteHandColor == 0) minuteHandColor =
            attributes.getColor(R.styleable.ClockView_minuteHandColor, Color.BLACK)
        if (minuteStrokeWidth == 0f) minuteStrokeWidth =
            attributes.getDimension(R.styleable.ClockView_minuteStrokeWidth, 10f)
        if (secondHandColor == 0) secondHandColor =
            attributes.getColor(R.styleable.ClockView_secondHandColor, Color.RED)
        if (secondStrokeWidth == 0f) secondStrokeWidth =
            attributes.getDimension(R.styleable.ClockView_secondStrokeWidth, 8f)
        if (digitSize == 0f) digitSize = attributes.getFloat(R.styleable.ClockView_digitSize, 20f)
        if (digitColor == 0) digitColor =
            attributes.getColor(R.styleable.ClockView_digitColor, Color.BLACK)
        if (radiusPercent == 0f) radiusPercent =
            attributes.getFloat(R.styleable.ClockView_radiusPercent, 0.75f)

        if (circleColor == 0) circleColor =
            attributes.getColor(R.styleable.ClockView_circleColor, Color.BLACK)

        if (circleStyle == "") circleStyle =
            attributes.getString(R.styleable.ClockView_circleStyle) ?: "STROKE"
        attributes.recycle()

        fontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, digitSize, resources.displayMetrics
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h

        radius = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            ((viewWidth - (viewPaddingBySides * 2)) / 2f) * radiusPercent
        } else {
            ((viewHeight - (viewPaddingBySides * 2)) / 2f) * radiusPercent
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(viewWidth / 2f, viewHeight / 2f, radius, paintStroke(circleStyle))
        drawNumeral(canvas)
        drawClockHandles(canvas)
        canvas?.drawCircle(viewWidth / 2f, viewHeight / 2f, hourStrokeWidth * 0.6f, paintFill())
        postInvalidateDelayed(1000)
    }

    private fun drawNumeral(canvas: Canvas?) {
        val centerX = viewWidth / 2f
        val centerY = viewHeight / 2f
        for (number in 1..12) {

            val angle = Math.toRadians((30 * (number - 3)).toDouble())
            val radiusToNumerics = findLengthByPercentage(80, radius)
            val x = (centerX - 20 + cos(angle) * radiusToNumerics).toFloat()
            val y = (centerY + 20 + sin(angle) * radiusToNumerics).toFloat()
            canvas?.drawText(number.toString(), x, y, paintFill())

            val startX = centerX + (radius * 0.88f) * sin(angle).toFloat()
            val startY = centerY - (radius * 0.88f) * cos(angle).toFloat()
            val endX = centerX + (radius * 0.96f) * sin(angle).toFloat()
            val endY = centerY - (radius * 0.96f) * cos(angle).toFloat()
            canvas?.drawLine(startX, startY, endX, endY, paintFill())

            for (j in 1..9) {
                val minuteAngle = PI / 30 * (number * 5 + j)
                val minuteStartX = centerX + (radius * 0.9f) * sin(minuteAngle).toFloat()
                val minuteStartY = centerY - (radius * 0.9f) * cos(minuteAngle).toFloat()
                val minuteEndX = centerX + (radius * 0.95f) * sin(minuteAngle).toFloat()
                val minuteEndY = centerY - (radius * 0.95f) * cos(minuteAngle).toFloat()
                canvas?.drawLine(minuteStartX, minuteStartY, minuteEndX, minuteEndY, paintFill())
            }
        }
    }

    private fun findLengthByPercentage(percentage: Int, totalSize: Float): Float {

        return (totalSize / 100) * percentage

    }

    private fun drawClockHandles(canvas: Canvas?) {
        val calInstance = Calendar.getInstance()

        drawSecondHandle(canvas, calInstance)

        drawMinuteHandle(canvas, calInstance)

        drawHourHandle(canvas, calInstance)
    }

    private fun drawHourHandle(canvas: Canvas?, cal: Calendar) {
        val minute = cal.get(Calendar.HOUR)
        val degreeFromMin = getDegree(minute, true)
        val coordinatesFromDegree = getCoordinatesFromDegree(degreeFromMin - 90, 45)

        canvas?.drawLine(
            viewWidth / 2f,
            viewHeight / 2f,
            coordinatesFromDegree.first,
            coordinatesFromDegree.second,
            paintHandle("hour")
        )

    }

    private fun drawMinuteHandle(canvas: Canvas?, cal: Calendar) {
        val minute = cal.get(Calendar.MINUTE)
        val degreeFromMin = getDegree(minute, false)
        val coordinatesFromDegree = getCoordinatesFromDegree(degreeFromMin - 90, 55)

        canvas?.drawLine(
            viewWidth / 2f,
            viewHeight / 2f,
            coordinatesFromDegree.first,
            coordinatesFromDegree.second,
            paintHandle("minute")
        )

    }

    private fun drawSecondHandle(canvas: Canvas?, cal: Calendar) {
        val second = cal.get(Calendar.SECOND)
        val degreeFromSec = getDegree(second, false)
        val coordinatesFromDegree = getCoordinatesFromDegree(degreeFromSec - 90, 66)

        canvas?.drawLine(
            viewWidth / 2f,
            viewHeight / 2f,
            coordinatesFromDegree.first,
            coordinatesFromDegree.second,
            paintHandle("second")
        )

    }

    private fun getDegree(unit: Int, isHour: Boolean): Int {

        val onePercent = if (isHour) {
            360 / 12f
        } else {
            360 / 60f
        }

        val f = onePercent * unit
        return f.roundToInt()
    }

    private fun getCoordinatesFromDegree(degree: Int, length: Int): Pair<Float, Float> {
        val angle = Math.toRadians(degree.toDouble())
        val radiusToHandle = findLengthByPercentage(length, radius)
        val x = (viewWidth / 2 + cos(angle) * radiusToHandle).toFloat()
        val y = (viewHeight / 2 + sin(angle) * radiusToHandle).toFloat()
        return Pair(x, y)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = CustomViewSavedState(superState)
        savedState.currentState = currentState + 1
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as CustomViewSavedState
        super.onRestoreInstanceState(savedState.superState)
        currentState = savedState.currentState
        Log.d("configChangedNumber", currentState.toString())
        invalidate()
    }

    private class CustomViewSavedState : BaseSavedState {
        var currentState: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            currentState = parcel.readInt()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeInt(currentState)
        }

        companion object CREATOR : Parcelable.Creator<CustomViewSavedState> {
            override fun createFromParcel(parcel: Parcel): CustomViewSavedState {
                return CustomViewSavedState(parcel)
            }

            override fun newArray(size: Int): Array<CustomViewSavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}