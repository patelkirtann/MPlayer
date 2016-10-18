package com.example.kt_ki.musicplayer;

import android.app.ListActivity;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

class FileLocation implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return true;
    }
}

public class MainActivity extends ListActivity {
    ListView lv;

    private static final String STORAGE = Environment.getExternalStorageDirectory().toString() + "/Music/";
    private ArrayList<File> songs = new ArrayList<>();
    int[] items = new int[]{R.raw.awari, R.raw.down, R.raw.mind, R.raw.wall};
    String[] songName = {"awari", "down", "mind", "wall"};
    private MediaPlayer mp = new MediaPlayer();
    private MediaPlayer[] mpArray = new MediaPlayer[items.length];

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = getListView();
//        File directory = new File(STORAGE);
//        ArrayList<File> mySongs = songList(directory);

        for (int i = 0; i < items.length; i++) {
            mpArray[i] = MediaPlayer.create(this, items[i]);

            Toast.makeText(this, String.valueOf(mpArray[i]), Toast.LENGTH_LONG).show();
        }

//        items = new String[mySongs.size()];
//        for (int i = 0; i < mySongs.size(); i++) {
//
//            items[i] = mySongs.get(i).getName();
//        }

        ArrayAdapter<String> songs = new ArrayAdapter<>(getApplicationContext(), R.layout.song_list, songName);
        lv.setAdapter(songs);


        Button play = (Button) findViewById(R.id.button2);
        Button pause = (Button) findViewById(R.id.button);


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

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Object o = this.lv.getCheckedItemPosition();
        Toast.makeText(getApplicationContext(), o.toString(), Toast.LENGTH_LONG).show();
    }


    public ArrayList<File> songList(File file) {
        File[] list = file.listFiles(new FileLocation());

        try {
            for (File singleFile : list) {
                if (singleFile.getName().endsWith(".mp3")) {
                    songs.add((singleFile));
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return songs;
    }


}
