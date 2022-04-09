package com.kartor.SAM.DB;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities={Audio.class, SportSession.class},version = 1)
public abstract class AudioDatabase extends RoomDatabase {
    private static volatile AudioDatabase INSTANCE;

    // genero il Dao attraverso il repository
    public abstract AudioDao audioDao();
    public abstract SportSessionDao sportSessionDao();
    // ritorna l'istanza del Database in caso che non esista come se fosse un singoletto
    public static AudioDatabase getInstance(Context context){
        if(INSTANCE==null){
            synchronized (AudioDatabase.class){
                if(INSTANCE==null){
                    INSTANCE= Room.databaseBuilder(context.getApplicationContext(),AudioDatabase.class,"audio_DB3").build();
                }
            }
        }
        return INSTANCE;
    }
}
