package com.danDay.agsr

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.danDay.agsr.data.PreferencesManager
import javax.inject.Inject

class stepJobService : JobService(), SensorEventListener {
    private lateinit var sensorManager: SensorManager


    var first = false
    var lastStep: Int = 0
    var step = 0


    override fun onStartJob(params: JobParameters?): Boolean {
        sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }


        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        sensorManager.unregisterListener(this)
        return true
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event != null) {
            Log.d("Step", event.values[0].toString() + "test")
        }

        if (event != null) {
            if (first) {
                lastStep= event.values[0].toInt()
                first = false
            } else {
                step += (event.values[0] - lastStep).toInt()
                if (step >= 100) {

                }


            }
        }

    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //TODO("Not yet implemented")
    }


}