package com.kartor.SAM.DB;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;

public class AudioViewModel extends AndroidViewModel {
    private AudioRepository audioRepository;
    private MutableLiveData<Boolean> _saved = new MutableLiveData<Boolean>();
    public LiveData<Boolean> saved= _saved;
    public LiveData<List<Audio>> audios;

    public AudioViewModel(@NonNull Application application) {
        super(application);
        this.audioRepository = new AudioRepository(application);
        this.audios = null;
    }

    public void saveAudio(Audio newAudio){
        audioRepository.insertNewAudio(newAudio);
        _saved.postValue(true); //per triggherare la variabile saved cosi che la view che sta in ascolto sappiamo se ha salvato correttamente (usata in questo caso aper chiudere activity)
        Log.d("saveAudioIfAbsentInAudio", newAudio.data);
    }
    public void getAllMusic(){
        this.audios = audioRepository.getAllAudios();
        Log.d("repository","inside getAllMusic");
    }

    public void deleteAllMusic(List<Audio> audioList) {
        audioRepository.deleteAllAudio(audioList);
        _saved.postValue(true); //per triggherare la variabile saved cosi che la view che sta in ascolto sappiamo se ha salvato correttamente (usata in questo caso aper chiudere activity)
        Log.d("deleteAll","ho svutato il DB");
    }


}
