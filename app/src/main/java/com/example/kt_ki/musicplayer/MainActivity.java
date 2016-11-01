package com.example.kt_ki.musicplayer;

import android.Manifest;
import android.app.ListActivity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.internal.zzs.TAG;


public class MainActivity extends ListActivity {
    ListView lv;
    private final String Media_Path = Environment.getExternalStorageDirectory().toString();
    private MediaPlayer mp = new MediaPlayer();

    SeekBar seekBar;
    int currentViewPosition = 0;

    List<File> files;
    private ArrayList<String> songs = new ArrayList<>();

    double startTime = 0;
    double finalTime = 0;
    int oneTimeOnly = 0;

    Handler handler = new Handler();


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = getListView();

        final Button play = (Button) findViewById(R.id.btnPlay);
        final Button pause = (Button) findViewById(R.id.btnPause);
        final Button next = (Button) findViewById(R.id.btnNext);
        final Button previous = (Button) findViewById(R.id.btnPre);

        final ToggleButton repeat = (ToggleButton) findViewById(R.id.toggleButton);

        files = getListFiles(new File(Media_Path));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.song_list, songs);
        setListAdapter(adapter);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setClickable(false);

        if (oneTimeOnly == 0) {
            seekBar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.start();

            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.pause();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (currentViewPosition <= songs.size()) {
                        mp.reset();
                        try {
                            mp.setDataSource(MainActivity.this, Uri.fromFile(files.get(currentViewPosition + 1)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(files.get(currentViewPosition + 1)));
                        mp.start();
                        currentViewPosition++;
                        Toast.makeText(MainActivity.this, songs.get(currentViewPosition), Toast.LENGTH_SHORT).show();
                    }
                } catch (IndexOutOfBoundsException e) {
                    currentViewPosition++;
                    Toast.makeText(MainActivity.this, "No more songs", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    currentViewPosition++;
                    Toast.makeText(MainActivity.this, "No more songs", Toast.LENGTH_SHORT).show();
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (currentViewPosition >= 0) {
                        mp.reset();
                        try {
                            mp.setDataSource(MainActivity.this, Uri.fromFile(files.get(currentViewPosition - 1)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(files.get(currentViewPosition - 1)));
                        mp.start();
                        currentViewPosition--;
                        Toast.makeText(MainActivity.this, songs.get(currentViewPosition), Toast.LENGTH_SHORT).show();
                    }
                } catch (IndexOutOfBoundsException e) {
                    currentViewPosition--;
                    Toast.makeText(MainActivity.this, "No more songs", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    currentViewPosition--;
                    Toast.makeText(MainActivity.this, "No more songs", Toast.LENGTH_SHORT).show();
                }

            }
        });


        repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && mp.isPlaying()) {
                    mp.setLooping(true);
                    Toast.makeText(MainActivity.this, "Loop Is On", Toast.LENGTH_SHORT).show();
                } else if (!mp.equals(mp.isPlaying())){
                    mp.setLooping(false);
                    Toast.makeText(MainActivity.this, "Loop Is Off", Toast.LENGTH_SHORT).show();
                }
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mp.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                this.onCompletion(mp);
                Log.i("Completion Listener","Song Complete");
                Toast.makeText(MainActivity.this, "Finished" , Toast.LENGTH_SHORT).show();
                mp.reset();
                try {
                    mp.setDataSource(MainActivity.this, Uri.fromFile(files.get(currentViewPosition + 1)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(files.get(currentViewPosition + 1)));
                mp.start();
            }
        });

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

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            getListFiles(new File(Media_Path));
        }
    }


    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mp.getCurrentPosition();
            seekBar.setProgress((int) startTime);
            handler.postDelayed(this, 100);
        }
    };


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        position = lv.getPositionForView(v);
        for (int i = 0; i < files.size(); i++) {
            if (position == i) {
                mp.reset();
//                try {
//                    mp.setDataSource(this, Uri.fromFile(files.get(i)));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                mp = MediaPlayer.create(this, Uri.fromFile(files.get(i)));
                Toast.makeText(this, songs.get(i), Toast.LENGTH_SHORT).show();
                mp.start();
                currentViewPosition = position;
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }

    //    @Override
//    public void run() {
//        int currentPosition = 0;
//        int soundTotal = mp.getDuration() / 10000;
//        seekBar.setMax(soundTotal);
//
//        while (mp != null && currentPosition < soundTotal) {
//            try {
//                Thread.sleep(300);
//                currentPosition = mp.getCurrentPosition();
//                seekBar.setProgress(currentPosition);
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
