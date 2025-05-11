package com.example.healthapp.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.HealthServices
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.data.*
import androidx.lifecycle.lifecycleScope
import com.example.healthapp.R
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var tvHeartRate: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvSteps: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private lateinit var healthClient: ExerciseClient
    private val mainHandler = Handler(Looper.getMainLooper())

    private var sessionStartTime: Long = 0L
    private var dummyCalories = 0
    private var dummySteps = 0
    private var lastBpmDisplay = "--"
    private var lastHeartRateTime: Long = 0L

    private enum class SessionState { IDLE, ACTIVE }

    private val sensorPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) initiateSession()
        else tvHeartRate.text = getString(R.string.permission_denied)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initExerciseClient()
        applyState(SessionState.IDLE)

        btnStart.setOnClickListener { validatePermission() }
        btnStop.setOnClickListener { terminateSession() }
    }

    private fun initViews() {
        tvHeartRate = findViewById(R.id.heartRateText)
        tvTimer = findViewById(R.id.timeText)
        tvCalories = findViewById(R.id.caloriesText)
        tvSteps = findViewById(R.id.stepsText)
        btnStart = findViewById(R.id.startButton)
        btnStop = findViewById(R.id.stopButton)
    }

    private fun initExerciseClient() {
        healthClient = HealthServices.getClient(this).exerciseClient

        healthClient.setUpdateCallback(object : ExerciseUpdateCallback {
            override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                val now = System.currentTimeMillis()
                if (now - lastHeartRateTime >= 10_000) {
                    val heartRateData = update.latestMetrics.getData(DataType.HEART_RATE_BPM)
                    val bpm = heartRateData.firstOrNull()?.value as? Double
                    bpm?.let {
                        lastBpmDisplay = getString(R.string.heart_rate_text, it.toInt())
                        tvHeartRate.text = lastBpmDisplay
                        lastHeartRateTime = now
                    }
                }
            }

            override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {}
            override fun onRegistered() {}
            override fun onRegistrationFailed(throwable: Throwable) {}
            override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {}
        })
    }

    private fun validatePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            initiateSession()
        } else {
            sensorPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
        }
    }

    private fun initiateSession() {
        lifecycleScope.launch {
            val sessionConfig = ExerciseConfig.Builder(ExerciseType.WALKING)
                .setDataTypes(setOf(DataType.HEART_RATE_BPM))
                .build()

            healthClient.startExerciseAsync(sessionConfig).await()

            sessionStartTime = System.currentTimeMillis()
            dummyCalories = 0
            dummySteps = 0
            lastBpmDisplay = "--"

            beginTimer()
            applyState(SessionState.ACTIVE)
        }
    }

    private fun terminateSession() {
        lifecycleScope.launch {
            healthClient.endExerciseAsync().await()
            mainHandler.removeCallbacksAndMessages(null)

            tvHeartRate.text = if (lastBpmDisplay == "--") {
                getString(R.string.exercise_stopped)
            } else {
                lastBpmDisplay
            }

            tvCalories.text = dummyCalories.toString()
            tvSteps.text = dummySteps.toString()

            applyState(SessionState.IDLE)
        }
    }

    private fun beginTimer() {
        mainHandler.postDelayed(object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - sessionStartTime
                val mins = (elapsed / 1000) / 60
                val secs = (elapsed / 1000) % 60

                tvTimer.text = getString(R.string.duration_format, mins, secs)

                dummyCalories = (elapsed / 10_000).toInt()
                dummySteps = (elapsed / 3_000).toInt()

                tvCalories.text = dummyCalories.toString()
                tvSteps.text = dummySteps.toString()

                mainHandler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun applyState(state: SessionState) {
        when (state) {
            SessionState.IDLE -> {
                btnStart.visibility = View.VISIBLE
                btnStop.visibility = View.GONE
            }
            SessionState.ACTIVE -> {
                btnStart.visibility = View.GONE
                btnStop.visibility = View.VISIBLE
            }
        }
    }
}
