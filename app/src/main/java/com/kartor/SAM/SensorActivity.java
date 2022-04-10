package com.kartor.SAM;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.kartor.SAM.databinding.ActivitySensorBinding;
//TODO: eliminare questa activity
public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mSteps;
    private StringBuilder error = new StringBuilder();
    private int stepCounter = 0;
    private ActivitySensorBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void SetUpSensor() {
        this.mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            this.mAccelerometer =  mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            error.append("l'accellerometro non è presente \n");
        }
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            this.mSteps = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        } else {
            error.append("lo step counter non è presente \n");
        }

        if(!error.toString().isEmpty()) {
            Toast.makeText(this,error.toString(),Toast.LENGTH_SHORT).show();
        }
        mSensorManager.getSensorList(Sensor.TYPE_STEP_COUNTER).forEach(a -> {
            Log.d("sensor",a.toString());
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivitySensorBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        SetUpSensor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            mSensorManager.registerListener(this,mSteps,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int typeSensor = sensorEvent.sensor.getType();
        Log.d("sensor event", String.valueOf(typeSensor));
        if(typeSensor == Sensor.TYPE_ACCELEROMETER) {
            float value = sensorEvent.values[0];
            binding.sensorAccelerometer.setText(String.valueOf(value));
        }
        if(typeSensor == Sensor.TYPE_STEP_COUNTER) {
            Log.d("sensor Step",stepCounter +"");
            stepCounter = (int) sensorEvent.values[0];
            binding.sensorNumberStep.setText(String.valueOf(stepCounter));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}