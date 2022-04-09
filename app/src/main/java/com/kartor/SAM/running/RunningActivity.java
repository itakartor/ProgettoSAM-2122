package com.kartor.SAM.running;

import static com.kartor.SAM.MediaPlayer.ListTracesActivity.Broadcast_PLAY_NEW_AUDIO;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.SystemClock;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kartor.SAM.DB.Audio;
import com.kartor.SAM.DB.AudioViewModel;
import com.kartor.SAM.MediaPlayer.MediaPlayerService;
import com.kartor.SAM.R;
import com.kartor.SAM.SummaryActivity;
import com.kartor.SAM.databinding.ActivityRunningBinding;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class RunningActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

    public static long timeForChange = 30000;//120000 // 20000
    // mediaplayer
    private MediaPlayerService player;
    boolean serviceBound = false;
    private AudioViewModel audioViewModel;

    // non ho avuto tempo ma mi sono accorto che
    // in questo caso potrei migliorare l'interazione attraverso un cursore datomi dal DB
    // al posto di tenere una lista in memoria che viene aggiornata, tanto avrei sia il numero
    // di tutti gli elementi ricevuti e mi basterebbe muovere il cursore di un offset a seconda dell'
    // indice della traccia da riprodurre
    private List<Audio> listAudios;

    public static final String Broadcast_END_AUDIO = "com.kartor.SAM.MediaPlayer.EndAudio";
    private boolean voidList = false;

    private ActivityRunningBinding binding;
    // serve per evitare che il cronometro continui durante la pausa
    private long pauseOffset = 0;
    private boolean running = false;

    Handler handler = new Handler();
    private int lastIndex = -1;
    // sensori
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mSteps;
    private final StringBuilder error = new StringBuilder();
    private int stepCounter = 0;
    private int counterSteps = 0;
    private float lastValueAccelerometer = 0;
    private float newValueAccelerometer = 0;
    private float nValuerAccelerometer = 0;
    private float maxValueAccelerometer;
    private float minValueAccelerometer;
    // punti per il calcolo della soddifacibilità
    private int satisfiesPointsLast = -1;
    private int satisfiesPointsNew = 0;
    private boolean loadSaveIstance = false;
    private int resumePosition = -1;

    // connessione del servizio per l'audio
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
            // Toast.makeText(getApplicationContext(), "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    // inizializzo i sensori
    public void SetUpSensor() {
        this.mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            this.mAccelerometer =  mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            error.append("accelerometer is absent \n");
        }
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            this.mSteps = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        } else {
            error.append("step counter is absent \n");
        }
        if(!error.toString().isEmpty()) {
            Toast.makeText(this,error.toString(),Toast.LENGTH_SHORT).show();
        }
        // debug
        // mSensorManager.getSensorList(Sensor.TYPE_STEP_COUNTER).forEach(a -> Log.d("sensor",a.toString()));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityRunningBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // in caso che girassi il cellulare vorrei che salvasse questi dati fondamentali per ripartire con la stessa traccia
        // non sono riuscito ad approfondire, ma scatta prima l'observer a 148 rispetto la restoreSaveInstance
        if(savedInstanceState != null) {
            lastIndex = savedInstanceState.getInt(getString(R.string.lastIndex));
            loadSaveIstance = true;
            resumePosition = savedInstanceState.getInt(getString(R.string.currentPosition));
        }
        audioViewModel = new ViewModelProvider(this).get(AudioViewModel.class);
        loadAudio(false);
        SetUpSensor();
        binding.pauseSession.setOnClickListener(this);
        binding.resumeSession.setOnClickListener(this);
        binding.endSession.setOnClickListener(this);
        this.resetChronometer();
        binding.chronometer.start();
        this.running = true;
        // registro un broadcastReciver per ottenere gli intent dal mediaplayerService
        register_EndAudio();
        // passo la lista nella activity corrente e poi dico al servizio di partire
        audioViewModel.saved.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    loadAudio(true);
                }
            }
        });
        audioViewModel.audios.observe(this, audioList -> {
            if(audioList != null && !audioList.isEmpty()) {
                Log.d("ObserverAudioList","audioList NOT null");
                listAudios = audioList;
                if(lastIndex != -1 && loadSaveIstance) {
                    Log.d("getSaveInstance", String.valueOf(lastIndex));
                    loadSaveIstance = false;
                    playAudio(listAudios.get(lastIndex).data);
                    binding.infoMusicReproduce.setText(listAudios.get(lastIndex).title);
                    if(Long.parseLong(listAudios.get(lastIndex).duration) < timeForChange) {
                        timeForChange = player.getDurationAudioInt();
                    } else {
                        timeForChange = 30000;
                    }
                } else {
                    MusicTrace musicTrace = pickMusic();
                    lastIndex = musicTrace.getIndex();
                    playAudio(musicTrace.getMediaFile());
                    binding.infoMusicReproduce.setText(listAudios.get(musicTrace.getIndex()).title);
                    if(Long.parseLong(listAudios.get(musicTrace.getIndex()).duration) < timeForChange) {
                        timeForChange = player.getDurationAudioInt();
                    } else {
                        timeForChange = 30000;
                    }
                }
            } else {
                Log.d("Observer","Lista vuota");
                voidList = true;
                Toast.makeText(getBaseContext(),"la lista degli audio è vuota",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //lifecycle for activity
    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putLong(getString(R.string.baseChronometer),binding.chronometer.getBase());
        savedInstanceState.putFloat(getString(R.string.maxValueAccelerometer),maxValueAccelerometer);
        savedInstanceState.putFloat(getString(R.string.minValueAccelerometer),minValueAccelerometer);
        savedInstanceState.putInt(getString(R.string.satisfiesPoints),satisfiesPointsLast);
        savedInstanceState.putInt(getString(R.string.stepCounter),stepCounter);
        savedInstanceState.putInt(getString(R.string.steps),counterSteps);
        savedInstanceState.putFloat(getString(R.string.nValueAcc),nValuerAccelerometer);
        savedInstanceState.putFloat(getString(R.string.lastValueAcc),lastValueAccelerometer);
        savedInstanceState.putInt(getString(R.string.lastIndex),lastIndex);
        savedInstanceState.putInt(getString(R.string.currentPosition),player.getCurrentPosition());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            maxValueAccelerometer = savedInstanceState.getFloat(getString(R.string.maxValueAccelerometer),maxValueAccelerometer);
            minValueAccelerometer = savedInstanceState.getFloat(getString(R.string.minValueAccelerometer),minValueAccelerometer);
            satisfiesPointsLast = savedInstanceState.getInt(getString(R.string.satisfiesPoints),satisfiesPointsLast);
            stepCounter = savedInstanceState.getInt(getString(R.string.stepCounter),stepCounter);
            counterSteps = savedInstanceState.getInt(getString(R.string.steps),counterSteps);
            nValuerAccelerometer = savedInstanceState.getFloat(getString(R.string.nValueAcc),nValuerAccelerometer);
            lastValueAccelerometer = savedInstanceState.getFloat(getString(R.string.lastValueAcc),lastValueAccelerometer);
            binding.chronometer.setBase(savedInstanceState.getLong(getString(R.string.baseChronometer)));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(running) {
            player.pauseMedia();
        }
        if(handler.hasCallbacks(runnableChangeMusic)) {
            handler.removeCallbacks(runnableChangeMusic);
        }
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void playAudio(String mediaFile) {
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra(getString(R.string.mediaUri), mediaFile);
            playerIntent.putExtra("runningActivity",true);
            playerIntent.putExtra(getString(R.string.currentPosition),resumePosition);

            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {

            //Service is active
            //Send media with BroadcastReceiver
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            broadcastIntent.putExtra(getString(R.string.mediaUri), mediaFile);
            sendBroadcast(broadcastIntent);
        }
        handler.postDelayed(runnableChangeMusic,timeForChange);
    }

    // load file audio in audios List<Audio> similar to cache
    private void loadAudio(Boolean refresh) {
        if(audioViewModel.audios == null || refresh) {
            try {
                audioViewModel.getAllMusic();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        int idButton = view.getId();
        switch (idButton) {

            case R.id.resume_session: {
                if(!running) {
                    Log.d("button", "button resume session");
                    binding.chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                    binding.resumeSession.setVisibility(View.GONE);
                    binding.pauseSession.setVisibility(View.VISIBLE);
                    handler.postDelayed(runnableChangeMusic,timeForChange);
                    binding.chronometer.start();
                    binding.animationHeart.playAnimation();
                    player.playMedia();
                    this.running = true;
                    if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
                    }
                    if(mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
                        mSensorManager.registerListener(this,mSteps,SensorManager.SENSOR_DELAY_NORMAL);
                    }
                }

                break;
            }
            case R.id.pause_session: {
                if(running) {
                    Log.d("button", "button pause session");
                    binding.pauseSession.setVisibility(View.GONE);
                    binding.resumeSession.setVisibility(View.VISIBLE);
                    handler.removeCallbacks(runnableChangeMusic);
                    pauseOffset = SystemClock.elapsedRealtime() - binding.chronometer.getBase();
                    binding.chronometer.stop();
                    binding.animationHeart.pauseAnimation();
                    player.pauseMedia();
                    this.running = false;
                    if(mSensorManager != null) {
                        mSensorManager.unregisterListener(this);
                    }
                }
                break;
            }
            case R.id.end_session: {
                player.pauseMedia();
                // serve solo per debug
                //player.playAfter();
                handler.removeCallbacks(runnableChangeMusic);
                Log.d("button", "button end session");
                Intent intent = new Intent(this, SummaryActivity.class);
                pauseOffset = SystemClock.elapsedRealtime() - binding.chronometer.getBase();
                binding.chronometer.stop();
                binding.animationHeart.pauseAnimation();
                this.running = false;
                // i valori vengono passati in maniera errata quindi bisogna stare attenti al formato
                intent.putExtra(getString(R.string.maxValueAccelerometer),this.maxValueAccelerometer);
                intent.putExtra(getString(R.string.minValueAccelerometer),this.minValueAccelerometer);
                intent.putExtra(getString(R.string.stepCounter),this.stepCounter);
                long elapsedMillis = SystemClock.elapsedRealtime() - binding.chronometer.getBase();
                Log.d("elapsedMillis",String.valueOf(elapsedMillis));
                intent.putExtra(getString(R.string.durationSession),elapsedMillis);
                String currentDateandTime = sdf.format(new Date());
                Log.d("CURRENT DATE",currentDateandTime);
                intent.putExtra(getString(R.string.timestamp),currentDateandTime);
                startActivity(intent);
                // ci dovrebbe essere una activity per il summary
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + idButton);
        }
    }

    public void resetChronometer() {
        binding.chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && running) {
            mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null && running) {
            mSensorManager.registerListener(this,mSteps,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mSensorManager != null && running) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int typeSensor = sensorEvent.sensor.getType();
        Log.d("sensor event", String.valueOf(typeSensor));
        if(typeSensor == Sensor.TYPE_ACCELEROMETER) {
            float value = Math.abs(sensorEvent.values[0]);
            if(this.nValuerAccelerometer == 0) {
                this.lastValueAccelerometer = value;
                this.maxValueAccelerometer = value;
                this.minValueAccelerometer = value;
                this.nValuerAccelerometer += 1;
            } else {
                if(value > maxValueAccelerometer) {
                    maxValueAccelerometer = value;
                }
                if(value < minValueAccelerometer) {
                    minValueAccelerometer = value;
                }
                this.newValueAccelerometer = value;
                float difference = this.newValueAccelerometer - this.lastValueAccelerometer;
                // ottimo > 5
                // normale > 2
                // male > 0
                // pessimo < 0
                if(difference > 8) {
                    binding.imageAccelerometerPlus.setImageResource(R.drawable.ic_baseline_sentiment_satisfied_alt_24);
                    this.satisfiesPointsNew += 2;
                } else if(difference > 5){
                    binding.imageAccelerometerPlus.setImageResource(R.drawable.ic_baseline_sentiment_satisfied_24);
                    this.satisfiesPointsNew += 1;
                } else if (difference > 2) {
                    binding.imageAccelerometerPlus.setImageResource(R.drawable.ic_baseline_sentiment_dissatisfied_24);
                    this.satisfiesPointsNew -= 1;
                } else {
                    binding.imageAccelerometerPlus.setImageResource(R.drawable.ic_baseline_sentiment_very_dissatisfied_24);
                    this.satisfiesPointsNew -= 2;
                }
                Log.d("AVERAGE ACCELEROMETER Difference", String.valueOf(difference));
            }
            binding.userAccelerometer.setText(String.format("%.3f",value));
        }

        if(typeSensor == Sensor.TYPE_STEP_COUNTER) {
            Log.d("sensor Step",stepCounter +"");
            //Since it will return the total number since we registered we need to subtract the initial amount
            //for the current steps since we opened app
            if (counterSteps < 1) {
                // initial value
                counterSteps = (int)sensorEvent.values[0];
            }

            // Calculate steps taken based on first counter value received.
            stepCounter = (int)sensorEvent.values[0] - counterSteps;
            binding.userStep.setText(String.valueOf(stepCounter));
        }
    }
    // questa funzione sceglie la traccia da riprodurre a seconda di alcune condizioni su i dati catturati
    // la scelta dell'indice è banale, perchè in un mondo ideale andrebbe fatta prima
    // una query sui generi delle canzoni
    // in caso che avessero dei generi predefiniti normalizzati
    // e poi per esempio tra tutte le canzone con genere Pop se ne sceglie una casuale
    // oppure nel caso di avere le playlist differenziate basterebbe fare una query per playlist
    // in modo tale da avere 3 canzoni pronte per ogni evenienza
    public MusicTrace pickMusic() {

        String mediaFile = null;
        int index = 0;
        MusicTrace musicTrace = new MusicTrace();
        Random random = new Random();
        int numChoiceHard = random.nextInt(this.listAudios.size()/2);
        int numChoiceMedium = random.nextInt(this.listAudios.size()/3);
        int numChoiceSoft = random.nextInt(this.listAudios.size()/5);
        if(!voidList) {
            // devo avere una canzone "forte" divisori di 2
            // in caso che i punti siano simili nei due intervalli di tempo non cambio canzone

            if(satisfiesPointsNew > 10 || (satisfiesPointsLast < satisfiesPointsNew && satisfiesPointsNew >5) || (stepCounter > 20000 && satisfiesPointsNew >5)) {
                index = numChoiceHard*2;
                mediaFile = listAudios.get(numChoiceHard*2).data;
                // devo avere una canzone "medio" divisori di 3
            } else if(satisfiesPointsNew >5 || (stepCounter > 15000 && satisfiesPointsNew >2) || (satisfiesPointsLast < satisfiesPointsNew && satisfiesPointsNew >2)){
                index =numChoiceMedium*3;
                mediaFile = listAudios.get(numChoiceMedium*3).data;
                // devo avere una canzone "basso" divisori di 5
            } else {
                index =numChoiceSoft*5;
                mediaFile = listAudios.get(numChoiceSoft*5).data;
            }
        }

        if(index == lastIndex) {
            pickMusic();
        } else {
            lastIndex = index;
        }
        musicTrace.setIndex(index);
        musicTrace.setMediaFile(mediaFile);
        this.satisfiesPointsLast = this.satisfiesPointsNew;
        this.satisfiesPointsNew = 0;
        return musicTrace;
    }


    Runnable runnableChangeMusic = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            MusicTrace musicTrace;
            if(satisfiesPointsLast > satisfiesPointsNew + 2 || satisfiesPointsLast < satisfiesPointsNew - 2) {
                musicTrace = pickMusic();
                playAudio(musicTrace.getMediaFile());
                binding.infoMusicReproduce.setText(listAudios.get(musicTrace.getIndex()).title);
            }
        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private BroadcastReceiver endAudio = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            // avviare funzione che decide la prossima canzone

            MusicTrace musicTrace =pickMusic();
            playAudio(musicTrace.getMediaFile());
        }
    };

    private void register_EndAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(Broadcast_END_AUDIO);
        registerReceiver(endAudio, filter);
    }
}
