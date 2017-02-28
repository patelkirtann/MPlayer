package com.example.kt_ki.musicplayer;

import android.Manifest;
import android.app.ListActivity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.internal.zzs.TAG;


public class MainActivity extends ListActivity implements
        SeekBar.OnSeekBarChangeListener, MediaPlayer.OnPreparedListener {

    private final String MEDIA_PATH = Environment.getExternalStorageDirectory().toString();
    int mCurrentSongPosition = 0;
    private MediaPlayer mMediaPlayerObject = new MediaPlayer();
    private Button mPlayPause, mStop, mNext, mPrevious, mRepeat;
    private ListView mListData;
    private SeekBar mSeekBar;
    private TextView mDuration, mSongName;
    private Handler handler;
    private ImageView mCoverArt;
    private ProgressBar mLoadingIndicator;

//    ProgressDialog progressDialog;

    private List<File> mFiles;
    private ArrayList<String> mSongs = new ArrayList<>();
    //      Runnable thread will update the song time
    private Runnable mUpdateTime = new Runnable() {
        public void run() {
            int currentDuration;
            try {
                if (mMediaPlayerObject.isPlaying()) {
                    currentDuration = mMediaPlayerObject.getCurrentPosition();
                    updatePlayer(currentDuration);
                    mDuration.postDelayed(this, currentDuration);
                } else {
                    mDuration.removeCallbacks(this);
                }
            } catch (IllegalStateException i) {
                Toast.makeText(MainActivity.this, " Runnable state ", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, " Runnable state ", Toast.LENGTH_SHORT).show();
            }
        }
    };
    //      Runnable thread to move the seekbar every time when (mMediaPlayerObject.isPlaying==true)
    private Runnable moveSeekBarThread = new Runnable() {
        public void run() {
            try {
                if (mMediaPlayerObject.isPlaying()) {
                    int mediaPos_new = mMediaPlayerObject.getCurrentPosition();
                    int mediaMax_new = mMediaPlayerObject.getDuration();
                    mSeekBar.setMax(mediaMax_new);
                    mSeekBar.setProgress(mediaPos_new);

                    handler.postDelayed(this, 100);
                    mDuration.post(mUpdateTime);
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListData = getListView();
        mListData.setSelector(R.color.colour_highlight_grey);

        mPlayPause = (Button) findViewById(R.id.btnPlayPause);
        mStop = (Button) findViewById(R.id.btnStop);
        mNext = (Button) findViewById(R.id.btnNext);
        mPrevious = (Button) findViewById(R.id.btnPre);
        mRepeat = (Button) findViewById(R.id.loop);

        mSongName = (TextView) findViewById(R.id.ItemName);
        mDuration = (TextView) findViewById(R.id.duration);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mCoverArt = (ImageView) findViewById(R.id.img);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.progressIndicator);

//        Toast.makeText(MainActivity.this, "OnCreated ", Toast.LENGTH_SHORT).show();

        mFiles = getListFiles(new File(MEDIA_PATH));

        this.setListAdapter(new ArrayAdapter<>(
                this, R.layout.detailed_list, R.id.ItemName, mSongs));

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.song_list, mSongs);
//        setListAdapter(adapter);

//      Play and Pause button events
        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                try {
                    if (mMediaPlayerObject.isPlaying()) {
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
        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mMediaPlayerObject.isPlaying() || mMediaPlayerObject != null) {
                        if (mMediaPlayerObject.isLooping()) {
                            mRepeat.setBackgroundResource(R.drawable.repeat_offf);
                        }
                        mMediaPlayerObject.stop();
                        mSeekBar.setProgress(0);
                        mPlayPause.setBackgroundResource(android.R.drawable.ic_media_play);
                        mMediaPlayerObject.reset();

                        mMediaPlayerObject.setDataSource(MainActivity.this,
                                Uri.fromFile(mFiles.get(mCurrentSongPosition)));
                        mMediaPlayerObject.prepare();
                    } else {
                        Toast.makeText(MainActivity.this, "select song from list",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (NullPointerException | IllegalStateException n) {
                    Toast.makeText(MainActivity.this, "Music Stopped", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Music Stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });

//      Next button event
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMusic();
            }
        });

//      Previous button event
        mPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousMusic();
            }
        });

//      Repeat button event
        mRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayerObject.isPlaying() &&
                        !mMediaPlayerObject.isLooping() &&
                        mMediaPlayerObject != null) {
                    setLoop();
                } else {
                    removeLoop();
                }
            }
        });

//      Error Listener when no song is selected....Goes to OnCompletionListener for mNext step
        mMediaPlayerObject.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(MainActivity.this, " Default Play ", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

//      OnCompletion executes when every song ends.
        mMediaPlayerObject.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (mMediaPlayerObject.isLooping()) {
                    playMusic();
                } else {
                    nextMusic();
                }
            }

        });

    }

    //      Play function
    private void playMusic() {
        mMediaPlayerObject.start();
        mPlayPause.setBackgroundResource(android.R.drawable.ic_media_pause);

//        mSongName.startAnimation(AnimationUtils.loadAnimation(MainActivity.this , android.R.anim.slide_in_left));
        initSeekBarProgress();
    }

    //      Pause function
    private void pauseMusic() {
        mMediaPlayerObject.pause();
        mPlayPause.setBackgroundResource(android.R.drawable.ic_media_play);
//        mSongName.clearAnimation();
    }

    //      Next function
    private void nextMusic() {
        try {
            if (mCurrentSongPosition < mSongs.size()) {
                if (mMediaPlayerObject.isLooping()) {
                    removeLoop();
                }
                mMediaPlayerObject.reset();
                try {
                    mMediaPlayerObject.setDataSource(MainActivity.this,
                            Uri.fromFile(mFiles.get(mCurrentSongPosition + 1))); // Sets the source of the mNext song
                    mMediaPlayerObject.prepare();
                    mListData.setSelection(mCurrentSongPosition + 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCurrentSongPosition++;
                playMusic();
            } else if (mCurrentSongPosition > mSongs.size()) {
                mMediaPlayerObject.reset();
                try {
                    mMediaPlayerObject.setDataSource(MainActivity.this,
                            Uri.fromFile(mFiles.get(0))); // starts from the very first song if all mSongs has been played
                    mMediaPlayerObject.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                playMusic();
            }
        } catch (IndexOutOfBoundsException e) {
            mCurrentSongPosition = 0;
            mSeekBar.setProgress(0);
            mMediaPlayerObject.reset();
            try {
                mMediaPlayerObject.setDataSource(MainActivity.this,
                        Uri.fromFile(mFiles.get(mCurrentSongPosition)));
                mMediaPlayerObject.prepare();
            } catch (IOException i) {
                e.printStackTrace();
            }
            playMusic();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "No more mSongs", Toast.LENGTH_SHORT).show();
        }
    }

    //      Previous function
    private void previousMusic() {
        try {
            if (mCurrentSongPosition >= 0) {
                if (mMediaPlayerObject.isLooping()) {
                    removeLoop();
                }
                mMediaPlayerObject.reset();
                try {
                    mMediaPlayerObject.setDataSource(MainActivity.this,
                            Uri.fromFile(mFiles.get(mCurrentSongPosition - 1)));
                    mMediaPlayerObject.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCurrentSongPosition--;
                playMusic();
            }
        } catch (IndexOutOfBoundsException e) {
            mSeekBar.setProgress(0);
            mPlayPause.setBackgroundResource(android.R.drawable.ic_media_play);
            Toast.makeText(MainActivity.this, "No more mSongs", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "No more mSongs", Toast.LENGTH_SHORT).show();
        }
        if (mMediaPlayerObject.isLooping()) {
            removeLoop();
        }
    }

    //      Start Repeating a song
    private void setLoop() {
        mMediaPlayerObject.setLooping(true);
        mRepeat.setBackgroundResource(R.drawable.repeat_onn);
        Toast.makeText(MainActivity.this, "Repeat on", Toast.LENGTH_SHORT).show();
    }

    //      Stops Repeating a song
    private void removeLoop() {
        mMediaPlayerObject.setLooping(false);
        mRepeat.setBackgroundResource(R.drawable.repeat_offf);
        Toast.makeText(MainActivity.this, "Repeat off", Toast.LENGTH_SHORT).show();
    }

    //      onListItemClick Executes when ListView item gets clicked
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        try {
            position = mListData.getPositionForView(v); // Takes the item position
            for (int i = 0; i < mFiles.size(); i++) {
                if (position == i) {
                    if (mMediaPlayerObject.isLooping()) {
                        removeLoop();
                    }
                    mMediaPlayerObject.reset();
                    try {
                        mMediaPlayerObject.setDataSource(this, Uri.fromFile(mFiles.get(i))); // Assigning the List clicked Item to Media Player Source
                        mMediaPlayerObject.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCurrentSongPosition = position;
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
            if (fromUser) {
                mMediaPlayerObject.seekTo(progress);
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
            if (mMediaPlayerObject.isPlaying()) {
                int mediaPos = mMediaPlayerObject.getCurrentPosition(); // gets current song position (in time)
                int mediaMax = mMediaPlayerObject.getDuration(); // gets maximum mDuration of song

                mSeekBar.setMax(mediaMax); // seekbar will seek until the mediaMax position
                mSeekBar.setProgress(mediaPos); // set the  seekbar progress to mediaPos
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, " No song selected ", Toast.LENGTH_SHORT).show();
        }
        handler.removeCallbacks(moveSeekBarThread);
        handler.postDelayed(moveSeekBarThread, 100); // total delay before seek
    }

    // Update the song mDuration in TextView object
    private void updatePlayer(int currentDuration) {
        mDuration.setText("" + milliSecondsToTimer((long) currentDuration));
    }

    // Time conversion for hours, minutes and seconds for song mDuration
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

        ArrayList<File> inFiles = new ArrayList<>(); // stores the mFiles in arraylist
        File[] files = parentDir.listFiles();
        try {
            if (isStoragePermissionGranted()) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        inFiles.addAll(getListFiles(file)); // if file is directory then move to the mNext file
                    } else {
                        if (file.getName().endsWith(".mp3") && (file.length() / 1024) >= 500) { // gets only ".mp3" mFiles with size greater than 500kb
                            inFiles.add(file); // adding mFiles to the ArrayList
                            mSongs.add(file.getName()); // adding file names in the list(gets song name)
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
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    //      if Permission is  granted then will return the getListFiles function where it can read the mFiles
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getListFiles(new File(MEDIA_PATH));
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
        Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show();
        mMediaPlayerObject.release();
        System.exit(0);
    }
}

