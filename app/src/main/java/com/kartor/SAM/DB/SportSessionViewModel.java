package com.kartor.SAM.DB;

import android.app.Application;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kartor.SAM.R;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SportSessionViewModel extends AndroidViewModel {
    private final SportSessionRepository sportSessionRepository;
    private MutableLiveData<Boolean> _saved = new MutableLiveData<Boolean>();
    public LiveData<Boolean> saved= _saved;
    public LiveData<List<SportSession>> sportSessions;
    
    public SportSessionViewModel(@NonNull Application application) {
        super(application);
        this.sportSessionRepository = new SportSessionRepository(application);
        this.sportSessions = null;
    }

    public void getAllSportSession(){
        this.sportSessions = sportSessionRepository.getAllSportSessions();
        Log.d("repository","inside getAllSportSessions");
    }
    public Cursor getCursorSportSession() throws ExecutionException, InterruptedException, TimeoutException {
        Log.d("repository","inside getCursorSportSessions");
        return sportSessionRepository.getCursorSportSessions();
    }
    public void saveSportSession(SportSession newSportSession){
        sportSessionRepository.insertNewSportSession(newSportSession);
        _saved.postValue(true); //per triggherare la variabile saved cosi che la view che sta in ascolto sappiamo se ha salvato correttamente (usata in questo caso aper chiudere activity)
        Log.d("saveSportSessions", newSportSession.timestamp.toString());
        Toast.makeText(getApplication(), R.string.save_successful,Toast.LENGTH_SHORT).show();
    }
    public void deleteAllMusic(List<SportSession> sportSessions) {
        sportSessionRepository.deleteAllSportSession(sportSessions);
        _saved.postValue(true); //per triggherare la variabile saved cosi che la view che sta in ascolto sappiamo se ha salvato correttamente (usata in questo caso aper chiudere activity)
        Log.d("deleteAll","ho svutato il DB");
    }
}
