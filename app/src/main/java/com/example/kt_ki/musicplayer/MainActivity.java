package com.example.kt_ki.musicplayer;

import android.Manifest;
import android.app.ListActivity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.internal.zzs.TAG;


public class MainActivity extends ListActivity implements SeekBar.OnSeekBarChangeListener {

    private final String Media_Path = Environment.getExternalStorageDirectory().toString();
    private MediaPlayer mp = new MediaPlayer();
    int currentSongPosition = 0;

    Button play_pause;
    Button stop;
    Button next;
    Button previous;
    ToggleButton repeat;
    ListView lv;
    ImageView iv;
    SeekBar seekBar;
    TextView duration;

    private int mediaPos = 0;
    private int mediaMax = 0;
    Handler handler;

    List<File> files;
    private ArrayList<String> songs = new ArrayList<>();


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = getListView();

        play_pause = (Button) findViewById(R.id.btnPlayPause);
        stop = (Button) findViewById(R.id.btnStop);
        next = (Button) findViewById(R.id.btnNext);
        previous = (Button) findViewById(R.id.btnPre);

        repeat = (ToggleButton) findViewById(R.id.toggleButton);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        duration = (TextView) findViewById(R.id.duration);

        files = getListFiles(new File(Media_Path));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.song_list, songs);
        setListAdapter(adapter);


        play_pause.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
//                if (!mp.isPlaying()) {
//                    try {
//                        mp.setDataSource(MainActivity.this , Uri.fromFile(files.get(currentSongPosition)));
//                        mp.prepare();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }catch (IllegalStateException i){
//                    }
//                    mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(files.get(currentSongPosition)));
//                    playMusic();
//                }
                try {
                    if (mp.isPlaying()) {
                        pauseMusic();
                    } else {
                        playMusic();
                    }
                } catch (NullPointerException n) {
                    Toast.makeText(MainActivity.this, " Select Track ", Toast.LENGTH_SHORT).show();
                } catch (IllegalStateException i) {
                    Toast.makeText(MainActivity.this, " Select Track", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Select Track", Toast.LENGTH_SHORT).show();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mp.isPlaying() || mp != null) {
                        mp.stop();
                        mp.release();
                        seekBar.setProgress(0);
                        play_pause.setBackgroundResource(android.R.drawable.ic_media_play);

                        mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(files.get(currentSongPosition)));
                        mp.prepare();
                    }
                } catch (NullPointerException | IllegalStateException n) {
                    Toast.makeText(MainActivity.this, "Music Stopped", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Music Stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMusic();
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (currentSongPosition >= 0) {
                        mp.reset();
                        try {
                            mp.setDataSource(MainActivity.this, Uri.fromFile(files.get(currentSongPosition - 1)));
                            mp.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(files.get(currentSongPosition - 1)));
                        currentSongPosition--;
//                        Toast.makeText(MainActivity.this, songs.get(currentSongPosition), Toast.LENGTH_SHORT).show();
                        playMusic();
                    }
                } catch (IndexOutOfBoundsException e) {
//                    currentSongPosition = 0;
                    seekBar.setProgress(0);
                    play_pause.setBackgroundResource(android.R.drawable.ic_media_play);
                    Toast.makeText(MainActivity.this, "No more songs", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
//                    currentSongPosition = 0;
                    Toast.makeText(MainActivity.this, "No more songs", Toast.LENGTH_SHORT).show();
                }

                repeat.setChecked(false);
            }
        });

        repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && mp.isPlaying()) {
                    mp.setLooping(true);
                    Toast.makeText(MainActivity.this, "Repeat on", Toast.LENGTH_SHORT).show();
                } else if (!mp.equals(mp.isPlaying())) {
                    mp.setLooping(false);
                    Toast.makeText(MainActivity.this, "Repeat Off", Toast.LENGTH_SHORT).show();
                }
            }
        });


        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(MainActivity.this, " Error ", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
//                Toast.makeText(MainActivity.this, " completed ", Toast.LENGTH_SHORT).show();
                if (mp.isLooping()) {
                    playMusic();
                } else {
                    nextMusic();
                }
            }

        });

    }

    private void playMusic() {
        mp.start();
        play_pause.setBackgroundResource(android.R.drawable.ic_media_pause);
        initMediaPlayer();
    }

    private void pauseMusic() {
        mp.pause();
        play_pause.setBackgroundResource(android.R.drawable.ic_media_play);
    }

    private void nextMusic() {
        try {
            if (currentSongPosition < songs.size()) {
                mp.reset();
                try {
                    mp.setDataSource(MainActivity.this, Uri.fromFile(files.get(currentSongPosition + 1)));
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(files.get(currentSongPosition++)));
                currentSongPosition++;
//                        Toast.makeText(MainActivity.this, songs.get(currentSongPosition), Toast.LENGTH_SHORT).show();
                playMusic();
            } else if (currentSongPosition > songs.size()) {
                mp.reset();
                try {
                    mp.setDataSource(MainActivity.this, Uri.fromFile(files.get(0)));
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(files.get(0)));
                playMusic();
            }
        } catch (IndexOutOfBoundsException e) {
            currentSongPosition = 0;
            seekBar.setProgress(0);
//            Toast.makeText(MainActivity.this, "No more songs", Toast.LENGTH_SHORT).show();
            mp.reset();
            try {
                mp.setDataSource(MainActivity.this, Uri.fromFile(files.get(currentSongPosition)));
                mp.prepare();
            } catch (IOException i) {
                e.printStackTrace();
            }
//                mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(files.get(0)));
            playMusic();

        } catch (Exception e) {
//            currentSongPosition = 0;
            Toast.makeText(MainActivity.this, "No more songs", Toast.LENGTH_SHORT).show();
        }
        repeat.setChecked(false);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        try {
            position = lv.getPositionForView(v);
            for (int i = 0; i < files.size(); i++) {
                if (position == i) {
                    mp.reset();
                    try {
                        mp.setDataSource(this, Uri.fromFile(files.get(i)));
                        mp.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    mp = MediaPlayer.create(this, Uri.fromFile(files.get(i)));
//                    Toast.makeText(this, songs.get(i).replace(".mp3", ""), Toast.LENGTH_SHORT).show();
                    currentSongPosition = position;
                    playMusic();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "File Not Found", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        try {
            if (fromUser && mp.isPlaying()) {
                mp.seekTo(progress);
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "No song selected", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private void initMediaPlayer() {
        handler = new Handler();
        try {
            if (mp.isPlaying()) {
                mediaPos = mp.getCurrentPosition();
                mediaMax = mp.getDuration();

                seekBar.setMax(mediaMax);
                seekBar.setProgress(mediaPos);
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, " No song selected ", Toast.LENGTH_SHORT).show();
        }

        handler.removeCallbacks(moveSeekBarThread);
        handler.postDelayed(moveSeekBarThread, 100);

    }

    private Runnable moveSeekBarThread = new Runnable() {

        public void run() {
            try {
                if (mp.isPlaying()) {
                    int mediaPos_new = mp.getCurrentPosition();
                    int mediaMax_new = mp.getDuration();
                    seekBar.setMax(mediaMax_new);
                    seekBar.setProgress(mediaPos_new);

                    handler.postDelayed(this, 100);
                    duration.post(mUpdateTime);
                }else {
                    handler.removeCallbacks(this, 100);
                }
            } catch (NullPointerException | IllegalStateException n) {
                Toast.makeText(MainActivity.this, "Select song", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Select song", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Runnable mUpdateTime = new Runnable() {
        public void run() {
            int currentDuration;
            if (mp.isPlaying()) {
                currentDuration = mp.getCurrentPosition();
                updatePlayer(currentDuration);
                duration.postDelayed(this, 1000);
            }else {
                duration.removeCallbacks(this);
            }
        }
    };

    private void updatePlayer(int currentDuration){
        duration.setText("" + milliSecondsToTimer((long) currentDuration));
    }

    public  String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles();
        try {
            if (isStoragePermissionGranted()) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        inFiles.addAll(getListFiles(file));
                    } else {
                        if (file.getName().endsWith(".mp3")) {
                            inFiles.add(file);
                            songs.add(file.getName());
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            Toast.makeText(this, "No file found", Toast.LENGTH_SHORT).show();
        }
        return inFiles;
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getListFiles(new File(Media_Path));
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        finish();
//    }

}

