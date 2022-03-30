Widget for choosing Day and Time for Android apps with flexible configuration 

<image src=screenshot.png />

Add it in your root build.gradle at the end of repositories:
```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Add the dependency
```groovy
	dependencies {
	        implementation 'com.github.wheely:android-day-time-picker:1.0.0'
	}
```
Sample usage
```xml
    <com.wheely.daytimepicker.DayTimePicker
        android:id="@+id/showcase_day_time_picker"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:day_time_picker_is_24h_mode="false"
    />
```
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
