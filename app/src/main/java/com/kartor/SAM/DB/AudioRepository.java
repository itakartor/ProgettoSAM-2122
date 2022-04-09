package com.kartor.SAM.DB;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

public class AudioRepository {
    private AudioDao audioDao;

    // il costruttore necessità di avere la situazione globale dell'applicazione
    public AudioRepository(Application application) {
        AudioDatabase audioDatabase = AudioDatabase.getInstance(application);
        this.audioDao = audioDatabase.audioDao();
    }

    public LiveData<List<Audio>> getAllAudios() { return audioDao.getAllAudios();}

    public LiveData<Audio> isAbsentAudioByData(String data) { return audioDao.getAudioByData(data);}


    public void insertNewAudio(Audio audio){
        new InsertAsyncTask(audioDao).execute(audio);
    }
    public void deleteAllAudio(List<Audio> audioList){
        new DeleteAsyncTasck(audioDao).execute(audioList);
    }

    public void updateAudio(Audio audio) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                audioDao.updateAudio(audio);
            }
        };
        new Thread(runnable).start();
    }

    public void deleteAudio(Audio audio) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                audioDao.deleteAudio(audio);
            }
        };
        new Thread(runnable).start();
    }

    private static class InsertAsyncTask extends AsyncTask<Audio,Void,Void>{
        private AudioDao audioDao;

        public InsertAsyncTask(AudioDao audioDao)
        {
            this.audioDao=audioDao;
        }

        @Override
        protected Void doInBackground(Audio... audio) {
            try {
                audioDao.insertNewAudio(audio[0]);
            } catch (Exception e) {
                Log.d("Insert Repository","it's in DB " + audio[0].title);
            }


            return null;
        }
    }
    private static class DeleteAsyncTasck extends AsyncTask<List<Audio>,Void,Void>{
        private AudioDao audioDao;

        public DeleteAsyncTasck(AudioDao audioDao)
        {
            this.audioDao=audioDao;
        }


        @Override
        protected Void doInBackground(List<Audio>... lists) {
            for (Audio a: lists[0]) {
                this.audioDao.deleteAudio(a);
            }
            return null;
        }
    }
}
