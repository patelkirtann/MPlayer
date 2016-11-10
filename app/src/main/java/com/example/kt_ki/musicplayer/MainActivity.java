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
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.internal.zzs.TAG;


public class MainActivity extends ListActivity implements SeekBar.OnSeekBarChangeListener, MediaPlayer.OnPreparedListener {

    private final String Media_Path = Environment.getExternalStorageDirectory().toString();
    private MediaPlayer mp = new MediaPlayer();
    int currentSongPosition = 0;

    Button play_pause;
    Button stop;
    Button next;
    Button previous;
    Button repeat;
    ListView lv;
    SeekBar seekBar;
    TextView duration;

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

        repeat = (Button) findViewById(R.id.loop);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        duration = (TextView) findViewById(R.id.duration);

        files = getListFiles(new File(Media_Path));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.song_list, songs);
        setListAdapter(adapter);

//      Play and Pause button events
        play_pause.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
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

//      Stop button event
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mp.isPlaying() || mp != null) {
                        if (mp.isLooping()) {
                            repeat.setBackgroundResource(R.drawable.repeat_off);
                        }
                        mp.stop();
                        seekBar.setProgress(0);
                        play_pause.setBackgroundResource(android.R.drawable.ic_media_play);
                        mp.reset();

                        mp.setDataSource(MainActivity.this, Uri.fromFile(files.get(currentSongPosition)));
                        mp.prepare();
                    } else {
                        Toast.makeText(MainActivity.this, "select song from list", Toast.LENGTH_SHORT).show();
                    }
                } catch (NullPointerException | IllegalStateException n) {
                    Toast.makeText(MainActivity.this, "Music Stopped", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Music Stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });

//      Next button event
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMusic();
            }
        });

//      Previous button event
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousMusic();
            }
        });

//      Repeat button event
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp.isPlaying() && !mp.isLooping() && mp != null) {
                    setLoop();
                } else {
                    removeLoop();
                }
            }
        });

//      Error Listener when no song is selected....Goes to OnCompletionListener for next step
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(MainActivity.this, " Default Play ", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

//      OnCompletion executes when every song ends.
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (mp.isLooping()) {
                    playMusic();
                } else {
                    nextMusic();
                }
            }

        });
    }

    //      Play function
    private void playMusic() {
        mp.start();
        play_pause.setBackgroundResource(android.R.drawable.ic_media_pause);
        initSeekBarProgress();
    }

    //      Pause function
    private void pauseMusic() {
        mp.pause();
        play_pause.setBackgroundResource(android.R.drawable.ic_media_play);
    }

    //      Next function
    private void nextMusic() {
        try {
            if (currentSongPosition < songs.size()) {
                if (mp.isLooping()) {
                    removeLoop();
                }
                mp.reset();
                try {
                    mp.setDataSource(MainActivity.this, Uri.fromFile(files.get(currentSongPosition + 1))); // Sets the source of the next song
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currentSongPosition++;
                playMusic();
            } else if (currentSongPosition > songs.size()) {
                mp.reset();
                try {
                    mp.setDataSource(MainActivity.this, Uri.fromFile(files.get(0))); // starts from the very first song if all songs has been played
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                playMusic();
            }
        } catch (IndexOutOfBoundsException e) {
            currentSongPosition = 0;
            seekBar.setProgress(0);
            mp.reset();
            try {
                mp.setDataSource(MainActivity.this, Uri.fromFile(files.get(currentSongPosition)));
                mp.prepare();
            } catch (IOException i) {
                e.printStackTrace();
            }
            playMusic();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "No more songs", Toast.LENGTH_SHORT).show();
        }
    }

    //      Previous function
    private void previousMusic() {
        try {
            if (currentSongPosition >= 0) {
                mp.reset();
                try {
                    mp.setDataSource(MainActivity.this, Uri.fromFile(files.get(currentSongPosition - 1)));
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currentSongPosition--;
                playMusic();
            }
        } catch (IndexOutOfBoundsException e) {
            seekBar.setProgress(0);
            play_pause.setBackgroundResource(android.R.drawable.ic_media_play);
            Toast.makeText(MainActivity.this, "No more songs", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "No more songs", Toast.LENGTH_SHORT).show();
        }
        if (mp.isLooping()) {
            removeLoop();
        }
    }

    //      Start Repeating a song
    private void setLoop() {
        mp.setLooping(true);
        repeat.setBackgroundResource(R.drawable.repeat_on);
        Toast.makeText(MainActivity.this, "Repeat on", Toast.LENGTH_SHORT).show();
    }

    //      Stops Repeating a song
    private void removeLoop() {
        mp.setLooping(false);
        repeat.setBackgroundResource(R.drawable.repeat_off);
        Toast.makeText(MainActivity.this, "Repeat off", Toast.LENGTH_SHORT).show();
    }

    //      onListItemClick Executes when ListView item gets clicked
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        try {
            position = lv.getPositionForView(v); // Takes the item position
            for (int i = 0; i < files.size(); i++) {
                if (position == i) {
                    if (mp.isLooping()) {
                        removeLoop();
                    }
                    mp.reset();
                    try {
                        mp.setDataSource(this, Uri.fromFile(files.get(i))); // Assigning the List clicked Item to Media Player Source
                        mp.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentSongPosition = position;
                    playMusic();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "File Not Found", Toast.LENGTH_SHORT).show();
        }
    }

//  When Song gets Start , the SeekBar process gets changed here
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

    //  SeekBar progress gets initialized here
    private void initSeekBarProgress() {
        handler = new Handler();
        try {
            if (mp.isPlaying()) {
                int mediaPos = mp.getCurrentPosition(); // gets current song position (in time)
                int mediaMax = mp.getDuration(); // gets maximum duration of song

                seekBar.setMax(mediaMax); // seekbar will seek until the mediaMax position
                seekBar.setProgress(mediaPos); // set the  seekbar progress to mediaPos
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, " No song selected ", Toast.LENGTH_SHORT).show();
        }
        handler.removeCallbacks(moveSeekBarThread);
        handler.postDelayed(moveSeekBarThread, 100); // total delay before seek
    }

    //      Runnable thread to move the seekbar every time when (mp.isPlaying==true)
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
                } else {
                    handler.removeCallbacks(this, 100);
                }
            } catch (NullPointerException | IllegalStateException n) {
                Toast.makeText(MainActivity.this, "Select song", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Select song", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //      Runnable thread will update the song time
    private Runnable mUpdateTime = new Runnable() {
        public void run() {
            int currentDuration;
            try {
                if (mp.isPlaying()) {
                    currentDuration = mp.getCurrentPosition();
                    updatePlayer(currentDuration);
                    duration.postDelayed(this, currentDuration);
                } else {
                    duration.removeCallbacks(this);
                }
            } catch (IllegalStateException i) {
                Toast.makeText(MainActivity.this, " Runnable state ", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, " Runnable state ", Toast.LENGTH_SHORT).show();
            }
        }
    };

    // Update the song duration in TextView object
    private void updatePlayer(int currentDuration) {
        duration.setText("" + milliSecondsToTimer((long) currentDuration));
    }

    // Time conversion for hours, minutes and seconds for song duration
    public String milliSecondsToTimer(long milliseconds) {
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


    //      getListFiles will Read the file from the SDcard .
    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<>(); // stores the files in arraylist
        File[] files = parentDir.listFiles();
        try {
            if (isStoragePermissionGranted()) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        inFiles.addAll(getListFiles(file)); // if file is derectory then move to the next file
                    } else {
                        if (file.getName().endsWith(".mp3")) { // gets only ".mp3" files
                            inFiles.add(file); // adding files to the ArrayList
                            songs.add(file.getName()); // adding file names in the list(gets song name)
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            Toast.makeText(this, "No file found", Toast.LENGTH_SHORT).show();
        }
        return inFiles;
    }

    // Permission needed after version 23 . This will ask the permission if not allowed to access file from the SDcard
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

    //      if Permission is  granted then will return the getListFiles function where it can read the files
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getListFiles(new File(Media_Path));
        }
    }

    // when song is prepared
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    // when pressing the back button or closing the application
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.release();
        System.exit(0);
    }

}

