package com.kartor.SAM;

import static com.kartor.SAM.MediaPlayer.MediaPlayerService.convertFormat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.kartor.SAM.DB.SportSession;
import com.kartor.SAM.DB.SportSessionViewModel;
import com.kartor.SAM.databinding.ActivitySummaryBinding;

import java.text.DecimalFormat;

public class SummaryActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivitySummaryBinding binding;
    private Float maxValueAccelerometer;
    private Float minValueAccelerometer;
    private int stepCounter;
    private Long durationSession;
    private String date;
    private SportSessionViewModel sportSessionViewModel;
    public static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivitySummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sportSessionViewModel = new ViewModelProvider(this).get(SportSessionViewModel.class);
        super.onCreate(savedInstanceState);
        binding.saveButton.setOnClickListener(this);
        binding.discardButton.setOnClickListener(this);
        Intent intent = getIntent();
        // valori ricevuti dalla RunningActivity
        this.maxValueAccelerometer = intent.getFloatExtra(getString(R.string.maxValueAccelerometer),0);
        this.minValueAccelerometer = intent.getFloatExtra(getString(R.string.minValueAccelerometer),0);
        this.stepCounter = intent.getIntExtra(getString(R.string.stepCounter),0);
        this.durationSession = intent.getLongExtra(getString(R.string.durationSession),0);
        this.date = intent.getStringExtra(getString(R.string.timestamp));
        Log.d("Intent Value",maxValueAccelerometer + " " +minValueAccelerometer + " " + stepCounter + " " +durationSession + " " + date);

        binding.maxAccelerometer.setText(df.format(this.maxValueAccelerometer));
        binding.minAccelerometer.setText(df.format(this.minValueAccelerometer));
        binding.nSteps.setText(String.valueOf(this.stepCounter));
        binding.durationSession.setText(convertFormat(Math.toIntExact(this.durationSession)));
        binding.date.setText(this.date);


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putFloat(getString(R.string.maxValueAccelerometer),maxValueAccelerometer);
        savedInstanceState.putFloat(getString(R.string.minValueAccelerometer),minValueAccelerometer);
        savedInstanceState.putInt(getString(R.string.stepCounter),stepCounter);
        savedInstanceState.putString(getString(R.string.date),date);
        savedInstanceState.putLong(getString(R.string.durationSession),durationSession);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            maxValueAccelerometer = savedInstanceState.getFloat(getString(R.string.maxValueAccelerometer),maxValueAccelerometer);
            minValueAccelerometer = savedInstanceState.getFloat(getString(R.string.minValueAccelerometer),minValueAccelerometer);
            stepCounter = savedInstanceState.getInt(getString(R.string.stepCounter),stepCounter);
            date = savedInstanceState.getString(getString(R.string.date),date);
            durationSession = savedInstanceState.getLong(getString(R.string.durationSession));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
        switch (id) {
            case R.id.discard_button:{
                intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.save_button:{
                SportSession newSportSession = new SportSession(stepCounter,maxValueAccelerometer,minValueAccelerometer, Math.toIntExact(durationSession), date);
                sportSessionViewModel.saveSportSession(newSportSession);
                intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}