package com.wheely.daytimepicker

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime


class DayTimePicker constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        R.attr.day_time_picker_style
    )

    constructor(context: Context) : this(context, null)

    companion object {
        private const val DEFAULT_DAY_COLUMN_WIDTH_DP = 150
        private const val DEFAULT_HOUR_COLUMN_WIDTH_DP = 50
        private const val DEFAULT_AM_PM_COLUMN_WIDTH_DP = 50
        private const val DEFAULT_MINUTE_COLUMN_WIDTH_DP = 50
    }

    var is24hMode: Boolean = false
        set(value) {
            field = value
            setupHourValues()
        }

    var onUserSelected: ((LocalDateTime) -> Unit)? = null

    private var startDay: LocalDate =
        if (isInEditMode) {
            LocalDate.MIN
        } else {
            LocalDate.now()
        }

    private var startTime: LocalDateTime = LocalDateTime.MIN

    private val dayColumn: ScrollSelector
    private val hourColumn: ScrollSelector
    private val minuteColumn: ScrollSelector
    private val amPmColumn: ScrollSelector
    private val columns: List<ScrollSelector>

    init {
        View.inflate(context, R.layout.view_time_picker, this)

        dayColumn = findViewById(R.id.time_picker_day)
        hourColumn = findViewById(R.id.time_picker_hour)
        minuteColumn = findViewById(R.id.time_picker_minute)
        amPmColumn = findViewById(R.id.time_picker_am_pm)

        columns = listOf(dayColumn, hourColumn, minuteColumn, amPmColumn)

        columns.forEach {
            it.onUserSelected = {
                var selectedTime = getSelectedTime()
                if (selectedTime.isBefore(startTime) && !isPressed) {
                    setTime(startTime)
                    selectedTime = startTime
                }
                onUserSelected?.invoke(selectedTime)
            }
        }

        usingStyledAttributes(attrs, R.styleable.DayTimePicker, defStyleAttr) {
            val overrideWidth =
                getDimensionPixelSize(R.styleable.DayTimePicker_day_time_picker_override_width, 0)
            dayColumn.layoutParams.width =
                getDimensionPixelSize(
                    R.styleable.DayTimePicker_day_time_picker_day_column_width,
                    dip(DEFAULT_DAY_COLUMN_WIDTH_DP)
                ) + 2 * overrideWidth

            dayColumn.layoutParams.run {
                this as MarginLayoutParams
                marginStart = -overrideWidth
                marginEnd = -overrideWidth
            }

            hourColumn.layoutParams.width =
                getDimensionPixelSize(
                    R.styleable.DayTimePicker_day_time_picker_hour_column_width,
                    dip(DEFAULT_HOUR_COLUMN_WIDTH_DP)
                )
            amPmColumn.layoutParams.width =
                getDimensionPixelSize(
                    R.styleable.DayTimePicker_day_time_picker_am_pm_column_width,
                    dip(DEFAULT_AM_PM_COLUMN_WIDTH_DP)
                )
            minuteColumn.layoutParams.width =
                getDimensionPixelSize(
                    R.styleable.DayTimePicker_day_time_picker_minute_column_width,
                    dip(DEFAULT_MINUTE_COLUMN_WIDTH_DP)
                )

            getDimension(
                R.styleable.DayTimePicker_day_time_picker_center_text_size,
                floatDip(ScrollSelector.DEFAULT_CENTER_TEXT_SIZE_DP)
            ).let { size ->
                columns.forEach { it.setCenterTextSize(size) }
            }
            getDimension(
                R.styleable.DayTimePicker_day_time_picker_secondary_text_size,
                floatDip(ScrollSelector.DEFAULT_TEXT_SIZE_DP)
            ).let { size ->
                columns.forEach { it.setSecondaryTextSize(size) }
            }
            getDimension(
                R.styleable.DayTimePicker_day_time_picker_line_height,
                floatDip(ScrollSelector.DEFAULT_LINE_HEIGHT_DP)
            ).let { h ->
                columns.forEach { it.lineHeight = h }
            }
            getColor(
                R.styleable.DayTimePicker_day_time_picker_center_text_color,
                getThemeColor(android.R.attr.textColorPrimary)
            ).let { color ->
                columns.forEach { it.setCenterTextColor(color) }
            }
            getColor(
                R.styleable.DayTimePicker_day_time_picker_secondary_text_color,
                getThemeColor(android.R.attr.textColorSecondary)
            ).let { color ->
                columns.forEach { it.setSecondaryTextColor(color) }
            }

            getResourceId(R.styleable.DayTimePicker_day_time_picker_text_font, 0)
                .takeIf { it != 0 }
                ?.let { ResourcesCompat.getFont(context, it) }
                ?.let { font ->
                    columns.forEach {
                        it.typeface = font
                    }
                }

            getInt(
                R.styleable.DayTimePicker_day_time_picker_center_text_style,
                Typeface.BOLD
            ).let { style ->
                columns.forEach { it.setCenterTextStyle(style) }
            }
            getInt(
                R.styleable.DayTimePicker_day_time_picker_secondary_text_style,
                Typeface.NORMAL
            ).let { style ->
                columns.forEach { it.setSecondaryTextStyle(style) }
            }
            is24hMode = getBoolean(R.styleable.DayTimePicker_day_time_picker_is_24h_mode, true)
            setupHourValues()
        }
        if (isInEditMode) {
            // for Android Studio preview
            dayColumn.values = (2..7).map { "$it сентября" }
            dayColumn.selectedIndex = 1
        }
    }

    private fun setupHourValues() {
        if (is24hMode) {
            hourColumn.values = resources.getStringArray(R.array.hours24).asList()
        } else {
            hourColumn.values = resources.getStringArray(R.array.hours12).asList()
        }
        amPmColumn.visibility = if (is24hMode) GONE else VISIBLE
    }

    fun setDateTimeParams(
        startTime: LocalDateTime,
        currentTime: LocalDateTime,
        dayCountDuration: Duration,
        dayStringBuilder: (LocalDate) -> String
    ) {
        this.startDay = startTime.toLocalDate()
        this.startTime = startTime
        val dayCount = dayCountDuration.toDays().toInt()
        val values = (0 until dayCount).map { dayStringBuilder(startDay.plusDays(it.toLong())) }
        dayColumn.values = values
        setTime(currentTime)
    }

    private fun setTime(currentTime: LocalDateTime) {
        dayColumn.selectedIndex =
            Duration.between(startDay.atStartOfDay(), currentTime).toDays().toInt()
        if (is24hMode) {
            hourColumn.selectedIndex = currentTime.hour
        } else {
            hourColumn.selectedIndex = currentTime.hour % 12
            amPmColumn.selectedIndex = currentTime.hour / 12
        }
        minuteColumn.selectedIndex = currentTime.minute / 5
    }

    fun getSelectedTime(): LocalDateTime {
        val minute = minuteColumn.selectedIndex * 5
        var hour = hourColumn.selectedIndex
        if (!is24hMode) {
            if (amPmColumn.selectedIndex == 1) {
                hour += 12
            }
        }
        return LocalDateTime.of(startDay.year, startDay.month, startDay.dayOfMonth, hour, minute)
            .plusDays(dayColumn.selectedIndex.toLong())
    }

    private fun getThemeColor(attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        val arr: TypedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(attr))
        val color = arr.getColor(0, -1)
        arr.recycle()
        return color
    }
}
