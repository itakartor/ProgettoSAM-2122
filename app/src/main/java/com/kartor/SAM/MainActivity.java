package com.kartor.SAM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;
import com.kartor.SAM.DB.Audio;
import com.kartor.SAM.DB.AudioViewModel;
import com.kartor.SAM.MediaPlayer.ListTracesActivity;
import com.kartor.SAM.databinding.ActivityMainBinding;
import com.kartor.SAM.goal.GoalActivity;
import com.kartor.SAM.running.RunningActivity;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationBarView.OnItemSelectedListener {

    private Cursor mCursor;
    private AudioViewModel audioViewModel;
    private ActivityMainBinding binding;
    private int EXTERNAL_STORAGE_PERMISSION_CODE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        audioViewModel = new ViewModelProvider(this).get(AudioViewModel.class);
        setContentView(binding.getRoot());
        binding.bottomNavigation.setOnItemSelectedListener(this);
        binding.bottomNavigation.getMenu().getItem(1).setChecked(true);

        //RecyclerView musicListRecyclerView;
        Log.d("URI EXTERNAL CONTENT", String.valueOf(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI));


        binding.imagePlayButton.setOnClickListener(this);


        // richiesta dei permessi all'utente per leggere i file esterni
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                EXTERNAL_STORAGE_PERMISSION_CODE);

        try {
            storeAudio();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Quando richiamo questa funzione dovrei eseguire una task asincrona oppure utilizzare un cursor Loader
    // per evitare che il thread principale si blocchi
    private void storeAudio() throws IOException {

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
        boolean primo = true;
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

    @Override
    public void onClick(View view) {
        int idButton = view.getId();
        Intent intent;
        switch (idButton) {
            case R.id.image_play_button: {
                Log.d("button","image play button");
                intent = new Intent(this, RunningActivity.class);
                startActivity(intent);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + idButton);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.goal_bottom_nav:
                intent = new Intent(getBaseContext(), GoalActivity.class);
                startActivity(intent);
                return true;
            case R.id.playlist_bottom_nav:
                intent = new Intent(getBaseContext(), ListTracesActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }


}