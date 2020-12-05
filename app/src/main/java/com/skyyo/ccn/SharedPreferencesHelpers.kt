package com.skyyo.ccn

import android.content.Context


fun Context.saveValues(interest: Float, interval: Long) {
    getSharedPreferences("ccnPrefs", Context.MODE_PRIVATE)
        .edit()
        .putFloat(INTEREST_VALUE, interest)
        .putLong(INTERVAL_VALUE, interval)
        .apply()
}

fun Context.clearValues() {
    getSharedPreferences("ccnPrefs", Context.MODE_PRIVATE)
        .edit()
        .remove(INTERVAL_VALUE)
        .remove(INTEREST_VALUE)
        .apply()
}

fun Context.clearInterest() {
    getSharedPreferences("ccnPrefs", Context.MODE_PRIVATE)
        .edit()
        .remove(INTEREST_VALUE)
        .apply()
}

fun Context.interest(): Float {
    return getSharedPreferences("ccnPrefs", Context.MODE_PRIVATE).getFloat(INTEREST_VALUE, 0f)
}

fun Context.interval(): Long {
    return getSharedPreferences("ccnPrefs", Context.MODE_PRIVATE).getLong(
        INTERVAL_VALUE,
        DEFAULT_TRACKER_INTERVAL
    )
}