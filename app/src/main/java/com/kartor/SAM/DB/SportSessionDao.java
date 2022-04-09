package com.kartor.SAM.DB;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SportSessionDao {
    
    @Insert(entity = SportSession.class)
    void insertNewSportSession(SportSession sportSession);

    @Query("SELECT * FROM SportSession WHERE :id=id")
    LiveData<SportSession> getSportSessionById(int id);

    @Query("SELECT * FROM SportSession")
    LiveData<List<SportSession>> getAllSportSessions();

    @Query("SELECT * FROM SportSession")
    Cursor getCursorSportSessions();

    @Update(entity = SportSession.class)
    void updateSportSession(SportSession SportSession);

    @Delete(entity = SportSession.class)
    void deleteSportSession(SportSession SportSession);
}
