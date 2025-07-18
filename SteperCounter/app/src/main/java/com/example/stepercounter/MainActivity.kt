package com.example.stepercounter

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import com.google.firebase.database.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.stepercounter.R

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private var totalSteps = 0
    private var previousTotalSteps = 0

    private lateinit var progressBar: ProgressBar
    private lateinit var stepsText: TextView

    private lateinit var inputStepsText: TextView
    private val userId = "user_123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        stepsText = findViewById(R.id.steps)
        inputStepsText = findViewById(R.id.inputsteps)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        loadData()
        resetSteps()
        loadStepGoalFromFirebase()
    }

    private fun loadStepGoalFromFirebase() {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("step_goals").child(userId)

        ref.get().addOnSuccessListener { dataSnapshot ->
            val goal = dataSnapshot.getValue(String::class.java)
            inputStepsText.text = "Goal: $goal steps"
        }.addOnFailureListener {
            inputStepsText.text = "Goal: N/A"
        }
    }
    override fun onResume() {
        super.onResume()
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: run {
            Toast.makeText(this, "This device has no Step Counter Sensor", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            totalSteps = event.values[0].toInt()
            val currentSteps = totalSteps - previousTotalSteps

            stepsText.text = currentSteps.toString()
            progressBar.progress = currentSteps
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun resetSteps() {
        stepsText.setOnClickListener {
            Toast.makeText(this, "Long press to reset steps", Toast.LENGTH_SHORT).show()
        }

        stepsText.setOnLongClickListener {
            previousTotalSteps = totalSteps
            stepsText.text = "0"
            progressBar.progress = 0
            saveData()
            true
        }
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE)
        previousTotalSteps = sharedPreferences.getInt("key1", 0)
    }
}