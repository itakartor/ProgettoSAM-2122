package com.kartor.SAM.DB;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AudioDao {
    @Insert(entity = Audio.class)
    void insertNewAudio(Audio audio);

    @Query("SELECT * FROM Audio")
    LiveData<List<Audio>> getAllAudios();

    @Query("SELECT * FROM Audio WHERE :data=data")
    LiveData<Audio> getAudioByData(String data);

    @Update(entity = Audio.class)
    void updateAudio(Audio Audio);

    @Delete(entity = Audio.class)
    void deleteAudio(Audio Audio);
}
