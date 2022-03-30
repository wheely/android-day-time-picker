package com.wheely.daytimepicker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatterBuilder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val dayTimePicker24: DayTimePicker =
            findViewById(R.id.showcase_day_time_picker_24)
        val dayTimePicker24ResultView: TextView =
            findViewById(R.id.showcase_day_time_picker_24_result)
        val dayTimePicker12: DayTimePicker =
            findViewById(R.id.showcase_day_time_picker_12)
        val dayTimePicker12ResultView: TextView =
            findViewById(R.id.showcase_day_time_picker_12_result)

        val currentTime = LocalDateTime.of(1993, Month.SEPTEMBER, 3, 21, 21)
        val startTime = LocalDate.of(1993, Month.SEPTEMBER, 2).atStartOfDay()

        val dayFormatter = DateTimeFormatterBuilder().appendPattern("d MMMM YYYY").toFormatter()

        dayTimePicker24.setDateTimeParams(
            startTime,
            currentTime,
            Duration.ofDays(3)
        ) { dayFormatter.format(it) }
        dayTimePicker24.onUserSelected = {
            dayTimePicker24ResultView.text = it.toString()
        }

        dayTimePicker12.setDateTimeParams(
            startTime,
            currentTime,
            Duration.ofDays(5)
        ) { dayFormatter.format(it) }
        dayTimePicker12.onUserSelected = {
            dayTimePicker12ResultView.text = it.toString()
        }
    }
}
