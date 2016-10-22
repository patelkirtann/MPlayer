package com.example.kt_ki.musicplayer;

import android.app.ListActivity;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaTimestamp;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class FileLocation implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return (name.endsWith(".mp3") || name.endsWith(".MP3"));
    }
}

public class MainActivity extends ListActivity {
    ListView lv;
    private final String Media_Path = Environment.getExternalStorageDirectory().getPath();
    private List<String> songs = new ArrayList<String>();
    private MediaPlayer mp = new MediaPlayer();
    int[] rawSongs = new int[]{R.raw.wall, R.raw.awari, R.raw.down, R.raw.mind};


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = getListView();
//        getSongList();

        String[] check = new String[]{"Wall", "Awari", "Down", "Mind"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.song_list, check);
        setListAdapter(adapter);


        final Button play = (Button) findViewById(R.id.btnPlay);
        final Button pause = (Button) findViewById(R.id.btnPause);


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play.setFocusable(true);
                play.setFocusableInTouchMode(true);
                play.requestFocus();

                mp.start();
                startChronometer(play);
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.pause();
                stopChronometer(pause);

            }
        });


    }

    int position;


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        this.position = position;

        this.position = lv.getPositionForView(v);
        Toast.makeText(this, String.valueOf(this.position), Toast.LENGTH_SHORT).show();

        for (int i = 0; i < rawSongs.length; i++) {
            if (position == i) {
                mp.reset();
                mp = MediaPlayer.create(this, rawSongs[i]);
                mp.start();
            }
        }
    }


    public void getSongList() {
        File home = new File(Media_Path);
        File[] files = home.listFiles(new FileLocation());
        ArrayList<File> arrayList = new ArrayList<>();

        try {
            if (home.listFiles(new FileLocation()).length > 0) {
                for (File file : files) {
                    songs.add(file.getName());
                }
            }
        } catch (NullPointerException n) {
            n.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.song_list, songs);
        setListAdapter(adapter);

    }

    public void startChronometer(View view) {
        ((Chronometer) findViewById(R.id.chronometer2)).start();
    }

    public void stopChronometer(View view) {
        ((Chronometer) findViewById(R.id.chronometer2)).stop();
    }

}
