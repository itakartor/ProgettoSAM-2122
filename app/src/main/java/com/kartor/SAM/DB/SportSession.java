package com.kartor.SAM.DB;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class SportSession {
    @PrimaryKey(autoGenerate = true)
    public int id=0;
    public int nSteps;
    public float maxValueAccelerometer;
    public float minValueAccelerometer;
    public int timeDurationSession;
    public String timestamp;


    public SportSession(int nSteps, float maxValueAccelerometer, float minValueAccelerometer, int timeDurationSession, String timestamp) {
        this.nSteps = nSteps;
        this.maxValueAccelerometer = maxValueAccelerometer;
        this.minValueAccelerometer = minValueAccelerometer;
        this.timeDurationSession = timeDurationSession;
        this.timestamp = timestamp;
    }
}
