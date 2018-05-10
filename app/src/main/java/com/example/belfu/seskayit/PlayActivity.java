package com.example.belfu.seskayit;

import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.R.attr.duration;

public class PlayActivity extends AppCompatActivity {

    TextView tv,tvNote,tvLoc;
    Button bPlay, bStop,bPause;
    MediaPlayer mediaPlayer;
    SeekBar seekBar;
    Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        tv = (TextView) findViewById(R.id.textView3);
        tvNote = (TextView) findViewById(R.id.textView4);
        tvLoc = (TextView) findViewById(R.id.textView6);
        bPlay = (Button) findViewById(R.id.button4);
        bStop = (Button) findViewById(R.id.button7);
        bPause = (Button) findViewById(R.id.button10);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        final String voice = getIntent().getStringExtra("VoiceKey");
        tv.setText(voice);

        final DataSource db = new DataSource(this);
        db.open();
        String data = db.getNote(voice);
        tvNote.setText(data);
        String dataa = db.getLoc(voice);
        tvLoc.setText(dataa);
        db.close();


        String path = Environment.getExternalStorageDirectory().toString() + "/Record";
        Log.wtf("path", path);
        final String v = path + "/" + voice;
        // File directory = new File(path);          **Dosyayı getirir
        // File[] files = directory.listFiles();


        bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setDataSource(v);
                            mediaPlayer.prepareAsync();
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.start();
                                    seekBar.setProgress(0);
                                    seekBar.setMax(mediaPlayer.getDuration());

                                    Log.wtf("Prog", "run: " + mediaPlayer.getDuration());
                                }
                            });
                        }catch (Exception e){}
                  }
                };
              myHandler.postDelayed(runnable,100);



                Toast.makeText(PlayActivity.this, "Recording Playing: " + voice, Toast.LENGTH_SHORT).show();
            }
        });
        Thread t = new runThread();
        t.start();

        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekBar.setMax(100);
                if (mediaPlayer != null)
                {
                    mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
                }
            }
        });

        bPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer != null){
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                    }
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                mediaPlayer.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


    }



    public class runThread extends Thread {


        @Override
        public void run() {
            while (true) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("Runwa", "run: " + 1);
                if (mediaPlayer != null) {
                    seekBar.post(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        }
                    });
                    Log.d("Runwa", "run: " + mediaPlayer.getCurrentPosition());
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        finish();
        super.onBackPressed();
    }


}

