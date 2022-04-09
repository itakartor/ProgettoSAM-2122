package com.kartor.SAM.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;
import com.kartor.SAM.DB.Audio;
import com.kartor.SAM.DB.AudioViewModel;
import com.kartor.SAM.goal.GoalActivity;
import com.kartor.SAM.MainActivity;
import com.kartor.SAM.R;
import com.kartor.SAM.databinding.ActivityListTracesBinding;

import java.util.List;

public class ListTracesActivity extends AppCompatActivity implements View.OnClickListener, NavigationBarView.OnItemSelectedListener, MusicListAdapter.ForecastAdapterOnClickHandler {

    private MediaPlayerService player;
    boolean serviceBound = false;
    private AudioViewModel audioViewModel;
    private static ActivityListTracesBinding binding;

    // non ho avuto tempo ma mi sono accorto che
    // in questo caso potrei migliorare l'interazione attraverso un cursore datomi dal DB
    // al posto di tenere una lista in memoria che viene aggiornata, tanto avrei sia il numero
    // di tutti gli elementi ricevuti e mi basterebbe muovere il cursore di un offset a seconda dell'
    // indice della traccia da riprodurre
    private List<Audio> listAudios;
    private MusicListAdapter musicListAdapter;
    private Cursor mCursor;
    private int currentPosition = -1;

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.kartor.SAM.MediaPlayer.PlayNewAudio";
    boolean isPlayerReady = false;

    int indexTrace = 0;
    private Handler handler;
    private boolean loadSaveInstance = false;

    //Binding this Client to the AudioPlayer Service
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
            Toast.makeText(getApplicationContext(), "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListTracesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //gestione recycler view
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.allMusicListView.setLayoutManager(layoutManager);
        binding.allMusicListView.setHasFixedSize(true);
        musicListAdapter = new MusicListAdapter(mCursor,this,this);
        binding.allMusicListView.setAdapter(musicListAdapter);

        // bottom Nav
        binding.bottomNavigation.setOnItemSelectedListener(this);
        binding.bottomNavigation.getMenu().getItem(2).setChecked(true);

        //String filename = "android.resource://" + getPackageName() + "/" + R.raw.kolony;
        // per gestire la posizione temporale della traccia
        handler = new Handler();

        // audioView Model e observer
        audioViewModel = new ViewModelProvider(this).get(AudioViewModel.class);
        // when the value of view model change i try to refresh Audio list
        audioViewModel.saved.observe(this, aBoolean -> {
            Log.d("Change bool","change bool");
            loadAudio(true);
        });

        // init of all Audio on smartphone
        // store all' audio in DB for manager playlist in app
        storeAudio();
        loadAudio(false);


        //assign variable
        binding.playButtonList.setOnClickListener(this);
        binding.precButton.setOnClickListener(this);
        binding.pauseButton.setOnClickListener(this);
        binding.postButton.setOnClickListener(this);
        binding.textLisTrace.setOnClickListener(this);

        audioViewModel.audios.observe(this, new Observer<List<Audio>>() {
            @Override
            public void onChanged(List<Audio> audioList) {
                if(audioList != null) {
                    Log.d("ObserverAudioList","audioList NOT null");
                    listAudios = audioList;
                    Log.d("Play",listAudios.get(indexTrace).data);
                    playAudio(listAudios.get(indexTrace).data);
                    binding.infoTracePick.setText(listAudios.get(indexTrace).title);
                } else {
                    Log.d("Observer","Lista vuota");
                    Toast.makeText(getBaseContext(),"la lista degli audio è vuota",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // recycler view
    private void loadMusicData() {
        showMusicDataView();
        new getMusicDataRecycler().execute();
    }

    Runnable runnableSeekBar = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            binding.currentPositionMusic.setText(MediaPlayerService.convertFormat(player.getCurrentPosition()));
            handler.postDelayed(this, 500);
        }
    };

    //lifecycle for activity
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(getString(R.string.lastIndex),indexTrace);
        savedInstanceState.putInt(getString(R.string.currentPosition),player.getCurrentPosition());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loadSaveInstance = true;
        indexTrace = savedInstanceState.getInt(getString(R.string.lastIndex));
        currentPosition = savedInstanceState.getInt(getString(R.string.currentPosition));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isPlayerReady) {
            handler.removeCallbacks(runnableSeekBar);
        }
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    @Override
    public void onClick(View view) {
        int idView = view.getId();
        switch (idView) {
            case R.id.play_button_list: {
                Log.d("button","button play music");
                binding.timeDurationMusic.setText(player.getDurationAudio());
                this.isPlayerReady = true;
                player.playMedia();
                binding.playButtonList.setVisibility(View.GONE);
                binding.pauseButton.setVisibility(View.VISIBLE);
                handler.postDelayed(runnableSeekBar,0);
                break;
            }
            case R.id.pause_button: {
                Log.d("button","button pause music");
                player.pauseMedia();
                this.isPlayerReady = false;
                handler.removeCallbacks(runnableSeekBar);
                binding.pauseButton.setVisibility(View.GONE);
                binding.playButtonList.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.post_button: {
                Log.d("button","button post music");
                binding.pauseButton.setVisibility(View.GONE);
                binding.playButtonList.setVisibility(View.VISIBLE);
                indexTrace += 1;
                if(this.listAudios.size() < indexTrace ) {
                    indexTrace -= 1;
                    Toast.makeText(this, "End of Audios List",Toast.LENGTH_SHORT).show();
                }
                playAudio(this.listAudios.get(indexTrace).data);
                this.isPlayerReady = true;
                binding.currentPositionMusic.setText(R.string.startTime);
                binding.infoTracePick.setText(listAudios.get(indexTrace).title);
                break;
            }
            case R.id.prec_button: {
                binding.pauseButton.setVisibility(View.GONE);
                binding.playButtonList.setVisibility(View.VISIBLE);
                Log.d("button","button prec music");
                indexTrace -= 1;
                if(indexTrace <0) { //this.listAudios.size()
                    indexTrace += 1;
                }
                playAudio(this.listAudios.get(indexTrace).data);
                this.isPlayerReady = true;
                binding.currentPositionMusic.setText(R.string.startTime);
                binding.infoTracePick.setText(listAudios.get(indexTrace).title);
                break;
            }
            // it's only for debug
            /*case R.id.text_lis_trace: {
                this.storeAudio();
                Toast.makeText(this,"Ricarico la lista",Toast.LENGTH_SHORT).show();
                if(this.listAudios == null) {
                    Toast.makeText(this,"lista vuota",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"lista caricata",Toast.LENGTH_SHORT).show();
                    //audioViewModel.deleteAllMusic(this.listAudios);
                }
                break;
            }*/
            default:
                throw new IllegalStateException("Unexpected value: " + idView);
        }
    }

    private void playAudio(String mediaFile) {
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra(getString(R.string.mediaUri), mediaFile);
            if(loadSaveInstance) {
                loadSaveInstance = false;
                playerIntent.putExtra(getString(R.string.currentPosition),currentPosition);
            }

            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Service is active
            //Send media with BroadcastReceiver
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            broadcastIntent.putExtra(getString(R.string.mediaUri), mediaFile);
            sendBroadcast(broadcastIntent);
        }
    }

    private void storeAudio() {
        // recupero il content resolver di sistema
        ContentResolver contentResolver = getContentResolver();

        // uri per recuperare tutti i file audio sul dispositivo
        Uri uriExt = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // valore per la query sul content Resolver di sistema
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        // do query for all audio
        mCursor = contentResolver.query(uriExt, null, selection, null, sortOrder);
        // for all audio save in db if is absent
        if (mCursor != null && mCursor.getCount() > 0) {
            while (mCursor.moveToNext()) {
                String data = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String playlist = "-1";
                String duration = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                Log.d("LoadMusic Internal",data + " " + title + " ");
                // Save to db
                Audio newAudio = new Audio(data, title,playlist,duration);
                audioViewModel.saveAudio(newAudio);
            }
        } else {
            Toast.makeText(this,"no audio found internal",Toast.LENGTH_SHORT).show();
        }
    }

    // load file audio in audios List<Audio> similar to cache
    private void loadAudio(Boolean refresh) {
        loadMusicData();
        if(audioViewModel.audios == null || refresh) {
            try {
                audioViewModel.getAllMusic();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // bottom nav
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.goal_bottom_nav: {
                player.pauseMedia();
                intent = new Intent(getBaseContext(), GoalActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.home: {
                player.pauseMedia();
                intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return false;
    }

    // function for recycler view
    private void showErrorMessage() {
        binding.allMusicListView.setVisibility(View.INVISIBLE);
        binding.errorText.setVisibility(View.VISIBLE);
    }
    private void showMusicDataView() {
        binding.errorText.setVisibility(View.INVISIBLE);
        binding.allMusicListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick() {
        Context context = this;
        /*Toast.makeText(context, "hai cambiato colore ", Toast.LENGTH_SHORT)
                .show();*/
    }

    private class getMusicDataRecycler extends AsyncTask<Void,Void,Cursor> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.pbLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            ContentResolver contentResolver = getContentResolver();

            // uri del content provider
            Uri uriExt = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
            return contentResolver.query(uriExt, null, selection, null, sortOrder);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            binding.pbLoadingIndicator.setVisibility(View.INVISIBLE);
            if(cursor != null) {
                showMusicDataView();
                musicListAdapter.swapCursor(cursor);
            } else {
                showErrorMessage();
            }
        }
    }
}