package com.skyyo.ccn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class CurrencyTrackerWorkManager(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private var notification: Notification? = null
    private var builder: NotificationCompat.Builder? = null
    private val notificationManager: NotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val notificationId = 911

    override suspend fun doWork(): Result {
        if (isWeekend()) return Result.success()
        val currencyText = fetchCurrencies() ?: return Result.retry()
        if (!isUserInterestedInNewValue(currencyText)) return Result.success()
        showNotification("UAH: $currencyText")
        return Result.success()
    }

    private fun formattedTargetValue(currencyText: String): String {
        return currencyText.filter { it.isDigit() || it.toString() == "." }.substring(0, 6)
    }

    private fun isUserInterestedInNewValue(targetValue: String): Boolean {
        val interestValue = applicationContext.interest()
        return when {
            interestValue == 0f -> true // running in non-interest mode
            targetValue.toFloat() >= interestValue -> true
            else -> false
        }
    }

    private fun isWeekend(): Boolean {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return today == Calendar.SATURDAY || today == Calendar.SUNDAY
    }

    private fun fetchCurrencies(): String? {
        val url = URL("https://openexchangerates.org/api/latest.json?app_id=$CURRENCY_APP_ID")
        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"  // optional default is GET
                inputStream.bufferedReader().use {
                    var result: String? = null
                    it.lines().forEach { line ->
                        if (line.contains("UAH")) {
                            result = formattedTargetValue(line)
                            return@forEach
                        }
                    }
                    inputStream.close()
                    return result
                }
            }
        } catch (e: Exception) {

        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "currency_tracking"
        val channel = NotificationChannel(
            channelId,
            "currency_tracking",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            lightColor = Color.CYAN
            importance = NotificationManager.IMPORTANCE_DEFAULT
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
        }
        (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )
        return channelId
    }

    private fun showNotification(title: String) {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel() else ""
        builder = NotificationCompat.Builder(applicationContext, channelId)
        notification = builder!!
            .setSmallIcon(R.drawable.ic_dollar_sign)
            .setColor(ContextCompat.getColor(applicationContext, R.color.purple_200))
            .setContentTitle(title)
            //.setOngoing(true)
            .build()
        notificationManager.notify(notificationId, notification)
    }
}
