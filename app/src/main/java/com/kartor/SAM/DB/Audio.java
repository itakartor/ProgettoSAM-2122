package com.kartor.SAM.DB;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

// la gestione dell'indice dovrebbe controllare che non ci siano due traccie con la stessa uri
@Entity(indices = {@Index(value = {"data"}, unique = true)})
public class Audio {
    @PrimaryKey @NotNull
    public String data;
    public String title;
    public String playlist;
    public String duration;


    public Audio(@NotNull String data, String title, String playlist, String duration) {
        this.data = data;
        this.title = title;
        this.playlist = playlist;
        this.duration = duration;
    }
}
