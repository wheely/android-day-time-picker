package com.wheely.daytimepicker

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View

inline fun View.usingStyledAttributes(
    set: AttributeSet?,
    attrs: IntArray,
    defStyleAttr: Int,
    f: TypedArray.() -> Unit
) = usingStyledAttributes(context, set, attrs, defStyleAttr, f)

inline fun usingStyledAttributes(
    context: Context,
    set: AttributeSet?,
    attrs: IntArray,
    defStyleAttr: Int = 0,
    f: TypedArray.() -> Unit
) = context.obtainStyledAttributes(set, attrs, defStyleAttr, 0)
    .run {
        try {
            f()
        } finally {
            recycle()
        }
    }

fun Context.floatDip(value: Float): Float =
    value * resources.displayMetrics.density

fun Context.dip(value: Int): Int =
    (value * resources.displayMetrics.density).toInt()

fun View.floatDip(value: Float): Float =
    context.floatDip(value)

fun View.dip(value: Int): Int =
    context.dip(value)
