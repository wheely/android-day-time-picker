package com.wheely.daytimepicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import java.time.Duration
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round

@SuppressLint("ClickableViewAccessibility")
class ScrollSelector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr), GestureDetector.OnGestureListener {

    companion object {
        const val DEFAULT_LINE_HEIGHT_DP = 32f
        const val DEFAULT_TEXT_SIZE_DP = 18f
        const val DEFAULT_CENTER_TEXT_SIZE_DP = 20f
        private val SNAP_PRE_DELAY = Duration.ofMillis(300)
        private val SNAP_DURATION = Duration.ofMillis(200)
    }

    var lineHeight: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var values: List<String> = emptyList()
        set(value) {
            field = value
            invalidate()
        }
    var selectedValue: String
        private set
    var selectedIndex: Int
        get() =
            selectedLine().toInt()
        set(value) {

            if (selectedLine().toInt() == value && scroller.isFinished) {
                return
            }

            scrollY = lineHeight * value
            invalidate()
            scroller.forceFinished(true)
        }

    private var notified: Int? = null

    var infinite: Boolean = true

    var typeface: Typeface = Typeface.DEFAULT
        set(value) {
            field = value
            invalidate()
        }

    var onUserSelected: ((Int) -> Unit)? = null

    private val detector = GestureDetectorCompat(context, this)
    private val scroller = Scroller(context)
    private val startSnap = Runnable {
        scroller.startScroll(
            0,
            scrollY.toInt(),
            0,
            -toSnap.toInt(),
            SNAP_DURATION.toMillis().toInt()
        )
        invalidate()
    }

    private val secondaryTextPaint: Paint = Paint().apply { isAntiAlias = true }
    private val centerTextPaint: Paint = Paint().apply { isAntiAlias = true }
    private val textBounds = Rect()

    private var scrollY: Float = 0f
    private var toSnap: Float = 0f

    init {
        usingStyledAttributes(attrs, R.styleable.ScrollSelector, defStyleAttr) {
            centerTextPaint.textSize = getDimension(
                R.styleable.ScrollSelector_scroll_selector_center_text_size,
                floatDip(DEFAULT_CENTER_TEXT_SIZE_DP)
            )
            centerTextPaint.color = getColor(
                R.styleable.ScrollSelector_scroll_selector_center_text_color,
                Color.BLACK
            )
            secondaryTextPaint.textSize = getDimension(
                R.styleable.ScrollSelector_scroll_selector_secondary_text_size,
                floatDip(DEFAULT_TEXT_SIZE_DP)
            )
            secondaryTextPaint.color = getColor(
                R.styleable.ScrollSelector_scroll_selector_secondary_text_color,
                Color.GRAY
            )
            lineHeight = getDimension(
                R.styleable.ScrollSelector_scroll_selector_line_height,
                floatDip(DEFAULT_LINE_HEIGHT_DP)
            )
            val valuesRes =
                getResourceId(R.styleable.ScrollSelector_scroll_selector_values, R.array.minutes)
            values = resources.getStringArray(valuesRes).asList()

            val fontId = getResourceId(R.styleable.ScrollSelector_scroll_selector_text_font, 0)
            if (fontId != 0) {
                ResourcesCompat.getFont(context, fontId)?.let { typeface = it }
            }

            val centerTypeface =
                typefaceFromStyleIdx(
                    getInt(
                        R.styleable.ScrollSelector_scroll_selector_center_text_style,
                        1
                    )
                )

            centerTextPaint.typeface = centerTypeface

            val secondaryTypeface =
                typefaceFromStyleIdx(
                    getInt(
                        R.styleable.ScrollSelector_scroll_selector_secondary_text_style,
                        0
                    )
                )
            secondaryTextPaint.typeface = secondaryTypeface

            infinite = getBoolean(R.styleable.ScrollSelector_scroll_selector_infinite, true)
        }
        selectedValue = values[0]
    }

    fun setCenterTextSize(textSize: Float) {
        centerTextPaint.textSize = textSize
        invalidate()
    }

    fun setSecondaryTextSize(textSize: Float) {
        secondaryTextPaint.textSize = textSize
        invalidate()
    }

    fun setCenterTextColor(@ColorInt color: Int) {
        centerTextPaint.color = color
        invalidate()
    }

    fun setSecondaryTextColor(@ColorInt color: Int) {
        secondaryTextPaint.color = color
        invalidate()
    }

    fun setCenterTextStyle(typefaceStyle: Int) {
        centerTextPaint.typeface = typefaceFromStyleIdx(typefaceStyle)
        invalidate()
    }

    fun setSecondaryTextStyle(typefaceStyle: Int) {
        secondaryTextPaint.typeface = typefaceFromStyleIdx(typefaceStyle)
        invalidate()
    }

    private fun typefaceFromStyleIdx(styleIdx: Int): Typeface =
        Typeface.create(typeface, styleIdx)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return detector.onTouchEvent(event)
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return true
    }

    override fun onDown(e: MotionEvent): Boolean {
        scroller.forceFinished(true)
        postSnap()
        notifyUserSelected()
        return true
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        scroller.fling(
            0,
            scrollY.toInt(),
            0,
            -velocityY.toInt(),
            0,
            0,
            Int.MIN_VALUE,
            Int.MAX_VALUE
        )
        scroller.finalY = (round(scroller.finalY.toFloat() / lineHeight) * lineHeight).toInt()
        ViewCompat.postInvalidateOnAnimation(this)
        removeCallbacks(startSnap)
        return true
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        scrollY += distanceY
        checkBounds()
        invalidate()
        postSnap()
        notifyUserSelected()
        return true
    }

    override fun onLongPress(e: MotionEvent) {
    }

    override fun onDraw(canvas: Canvas) {

        val top = paddingTop
        val bottom = height - paddingBottom
        val left = paddingLeft
        val right = width - paddingRight
        val centerY = (bottom + top) / 2
        val centerX = (left + right) / 2

        scroller.run {
            if (!isFinished) {
                computeScrollOffset()
                scrollY = currY.toFloat()
                checkBounds()
                ViewCompat.postInvalidateOnAnimation(this@ScrollSelector)
                if (isFinished) {
                    removeCallbacks(startSnap)
                    notifyUserSelected()
                }
            }
        }

        val selectedLine = selectedLine()
        val centerLineIdx = selectedLine.toInt()
        val fractional = selectedLine - floor(selectedLine)
        var topLineIdx: Int = (centerLineIdx - ((centerY - top) / lineHeight).toInt()) - 1
        val topY: Float = centerY - (centerLineIdx - topLineIdx + fractional) * lineHeight
        if (topLineIdx < 0 && infinite) {
            topLineIdx += values.size
        }

        var lineY = topY
        var idx = topLineIdx
        while (lineY < bottom + lineHeight) {

            if (idx >= 0 && idx < values.size) {

                val text = values[idx]
                val paint: Paint

                if (idx == centerLineIdx) {
                    toSnap = centerY - lineHeight / 2 - lineY
                    selectedValue = text
                    paint = centerTextPaint
                } else {
                    paint = secondaryTextPaint
                }

                paint.getTextBounds(text, 0, text.length, textBounds)

                canvas.drawText(
                    text,
                    (centerX - textBounds.width() / 2).toFloat(),
                    lineY + (lineHeight + paint.textSize) / 2,
                    paint
                )
            }

            lineY += lineHeight
            idx++
            if (infinite)
                idx %= values.size
        }
    }

    private fun selectedLine(): Float =
        ((scrollY / lineHeight + 0.50f) % values.size).let {
            if (it < 0) it + values.size
            else it
        }

    private fun postSnap() {
        removeCallbacks(startSnap)
        postDelayed(startSnap, SNAP_PRE_DELAY.toMillis())
    }

    private fun checkBounds() {
        if (infinite) {
            return
        }
        val max = lineHeight * (values.size - 1)
        if (scrollY < 0f) {
            scrollY = 0f
            scroller.forceFinished(true)
        }
        if (scrollY > max) {
            scrollY = max
            scroller.forceFinished(true)
        }
    }

    private fun notifyUserSelected() {
        if (notified == selectedIndex) {
            return
        }
        notified = selectedIndex
        onUserSelected?.invoke(selectedIndex)
    }

    override fun canScrollVertically(direction: Int): Boolean = true

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        centerTextPaint.getTextBounds(values[0], 0, values[0].length, textBounds)
        val desiredWidth = textBounds.right - textBounds.left
        val desiredHeight = lineHeight.toInt() * 3
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        //Measure Width
        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                //Must be this size
                widthSize
            }
            MeasureSpec.AT_MOST -> {
                //Can't be bigger than...
                min(desiredWidth, widthSize)
            }
            else -> {
                //Be whatever you want
                desiredWidth
            }
        }

        //Measure Height
        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                //Must be this size
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                //Can't be bigger than...
                min(desiredHeight, heightSize)
            }
            else -> {
                //Be whatever you want
                desiredHeight
            }
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height)
    }
}
