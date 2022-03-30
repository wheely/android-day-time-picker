Widget for choosing Day and Time for Android apps with flexible configuration 

<image src=screenshot.png />

```kotlin
    val dayTimePicker: DayTimePicker = findViewById(R.id.day_time_picker)
    val currentTime = LocalDateTime.of(1993, Month.SEPTEMBER, 3, 21, 21)
    val startTime = LocalDate.of(1993, Month.SEPTEMBER, 2).atStartOfDay()
    val dayFormatter = DateTimeFormatterBuilder().appendPattern("d MMMM YYYY").toFormatter()

    dayTimePicker.setDateTimeParams(
        startTime,
        currentTime,
        Duration.ofDays(3)
    ) { dayFormatter.format(it) }
    dayTimePicker.onUserSelected = { time: LocalDateTime ->
        // handle time
    }
```
