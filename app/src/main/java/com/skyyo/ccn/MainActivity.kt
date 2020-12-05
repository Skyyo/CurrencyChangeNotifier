package com.skyyo.ccn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.skyyo.ccn.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit


const val TRACKER_JOB = "CurrencyTrackerWorkManager"
const val DEFAULT_TRACKER_INTERVAL = 4L
const val JOB_BACKOFF_INTERVAL = 30L
const val CURRENCY_APP_ID = "Open Exchange Rates API app id"
const val INTEREST_VALUE = "interestValue"
const val INTERVAL_VALUE = "INTERVAL_VALUE"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with(binding) {
            etInterest.setText("${interest()}")
            etInterval.setText("${interval()}")
            btnStart.setOnClickListener {
                val interval = etInterval.text.toString().toLong()
                val interest = 0f
                saveValues(interest, interval)
                etInterval.setText("${interval()}")
                etInterest.setText("")
                cancelCurrencyTrackingJob()
                dispatchCurrencyTrackingJob()
            }
            btnManualMode.setOnClickListener {
                val interval = etInterval.text.toString().toLong()
                val interest = etInterest.text.toString().toFloat()
                saveValues(interest, interval)
                cancelCurrencyTrackingJob()
                dispatchCurrencyTrackingJob()
            }
            btnStop.setOnClickListener {
                cancelCurrencyTrackingJob()
            }
        }
    }

    private fun cancelCurrencyTrackingJob() {
        WorkManager.getInstance(applicationContext).cancelAllWorkByTag(TRACKER_JOB)
    }

    private fun dispatchCurrencyTrackingJob() {
        val workManager = WorkManager.getInstance(applicationContext)
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val task = PeriodicWorkRequestBuilder<CurrencyTrackerWorkManager>(interval(), TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, JOB_BACKOFF_INTERVAL, TimeUnit.MINUTES)
                .build()
        workManager.enqueueUniquePeriodicWork(TRACKER_JOB, ExistingPeriodicWorkPolicy.REPLACE, task)
    }
}