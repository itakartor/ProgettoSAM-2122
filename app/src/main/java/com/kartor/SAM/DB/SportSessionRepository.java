package com.kartor.SAM.DB;

import android.app.Application;
import android.database.Cursor;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SportSessionRepository {
    private SportSessionDao sportSessionDao;

    public SportSessionRepository(Application application){
        AudioDatabase audioDatabase = AudioDatabase.getInstance(application);
        this.sportSessionDao = audioDatabase.sportSessionDao();
    }

    public LiveData<List<SportSession>> getAllSportSessions() { return sportSessionDao.getAllSportSessions();}
    public Cursor getCursorSportSessions() { return sportSessionDao.getCursorSportSessions(); }
    public LiveData<SportSession> getSportSessionById(int id) { return sportSessionDao.getSportSessionById(id);}

    public void insertNewSportSession(SportSession SportSession){
        new SportSessionRepository.InsertAsyncTask(sportSessionDao).execute(SportSession);
    }
    public void deleteAllSportSession(List<SportSession> SportSessionList){
        new SportSessionRepository.DeleteAsyncTask(sportSessionDao).execute(SportSessionList);
    }

    private static class InsertAsyncTask extends AsyncTask<SportSession,Void,Void> {
        private SportSessionDao SportSessionDao;

        public InsertAsyncTask(SportSessionDao SportSessionDao)
        {
            this.SportSessionDao=SportSessionDao;
        }

        @Override
        protected Void doInBackground(SportSession... SportSession) {
            SportSessionDao.insertNewSportSession(SportSession[0]);
            return null;
        }
    }
    private static class DeleteAsyncTask extends AsyncTask<List<SportSession>,Void,Void>{
        private SportSessionDao SportSessionDao;

        public DeleteAsyncTask(SportSessionDao SportSessionDao)
        {
            this.SportSessionDao=SportSessionDao;
        }


        @Override
        protected Void doInBackground(List<SportSession>... lists) {
            for (SportSession a: lists[0]) {
                this.SportSessionDao.deleteSportSession(a);
            }
            return null;
        }
    }
}
