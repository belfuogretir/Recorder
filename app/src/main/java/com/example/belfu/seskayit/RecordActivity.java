package com.example.belfu.seskayit;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;


import com.yalantis.audio.lib.AudioUtil;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.belfu.seskayit.MainActivity.RequestPermissionCode;

public class RecordActivity extends AppCompatActivity {
    int i;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder;
    Button button, buttonR;
    Chronometer myChronometer;
    SharedPreferences preferences;
    private static final int REQUEST_PERMISSION_RECORD_AUDIO = 1;

    private static final int RECORDER_SAMPLE_RATE = 44100;
    private static final int RECORDER_CHANNELS = 1;
    private static final int RECORDER_ENCODING_BIT = 16;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int MAX_DECIBELS = 120;

    private AudioRecord audioRecord;
    private Horizon mHorizon;
    private GLSurfaceView glSurfaceView;

    private Thread recordingThread;
    private byte[] buffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        i = preferences.getInt("CountKey", 0);
        button = (Button) findViewById(R.id.button5);
        buttonR = (Button) findViewById(R.id.button3);
        myChronometer = (Chronometer) findViewById(R.id.chronometer);
        glSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface);
        mHorizon = new Horizon(glSurfaceView, getResources().getColor(R.color.colorGray),
                RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_ENCODING_BIT);
        mHorizon.setMaxVolumeDb(MAX_DECIBELS);
        final DataSource db = new DataSource(this);

        buttonR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myChronometer.setBase(SystemClock.elapsedRealtime());
                myChronometer.start();
                checkPermissionsAndStart();
                if (checkPermission()) {
                    i++;

                    File file = new File(Environment.getExternalStorageDirectory(), "Record/");
                    if (!file.exists()) {
                        file.mkdirs();
                    }

                    AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Record/" + "/voice" + i + ".mp3";

                    /*AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath();
                    File audioVoice = new File(AudioSavePathInDevice + File.separator + "Voices");
                    if (!audioVoice.exists()) {
                        audioVoice.mkdir();
                    }
                    AudioSavePathInDevice = AudioSavePathInDevice + File.separator + "Voices/" + "voice" + i + ".mp3";
                    Log.wtf("Audio", AudioSavePathInDevice + "");*/
                  //  AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() + "/voice" + i + ".mp3";

                    MediaRecorderReady();

                    try {
                        Log.wtf("try", "try");
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        Log.wtf("catch 1", "catch 1");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.wtf("catch 2", "catch 2");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Toast.makeText(RecordActivity.this, "Recording started", Toast.LENGTH_SHORT).show();
                } else {
                    requestPermission();
                }
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 myChronometer.stop();
                if (mediaRecorder != null) {
                    mediaRecorder.stop();
                    Toast.makeText(RecordActivity.this, "Recording Completed", Toast.LENGTH_LONG).show();


                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(RecordActivity.this);
                    View mView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
                    final EditText mNote =  mView.findViewById(R.id.editText);
                    final EditText mLoc =  mView.findViewById(R.id.editText4);
                    Button mSave =  mView.findViewById(R.id.button2);
                    mSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            boolean ok = true;
                            String voiceN = "voice" + i + ".mp3";
                            Log.wtf("voicen", voiceN);
                            String note = mNote.getText().toString();
                            Log.wtf("note", note);
                            String loc = mLoc.getText().toString();
                            try {
                                db.open();
                                db.add(voiceN, note, loc);
                                db.close();
                            } catch (Exception e) {
                                ok = false;
                                e.printStackTrace();
                            } finally {
                                if (ok) {
                                    Toast.makeText(RecordActivity.this, "Save!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        }
                    });
                    mBuilder.setView(mView);
                    AlertDialog dialog = mBuilder.create();
                    dialog.show();
                } else {
                    finish();
                }
            }
        });
    }


    private AudioRecord.OnRecordPositionUpdateListener recordPositionUpdateListener = new AudioRecord.OnRecordPositionUpdateListener() {
        @Override
        public void onMarkerReached(AudioRecord recorder) {
            //empty for now
        }

        @Override
        public void onPeriodicNotification(AudioRecord recorder) {
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING
                    && audioRecord.read(buffer, 0, buffer.length) != -1) {
                mHorizon.updateView(buffer);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("CountKey", i);
        editor.commit();
        super.onPause();
        glSurfaceView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (audioRecord != null) {
            audioRecord.release();
        }
        AudioUtil.disposeProcessor();
    }


    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(RecordActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);

    }

    public boolean checkPermission() {

        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_PERMISSION_RECORD_AUDIO);
        } else {
            initRecorder();
            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                startRecording();
            }
        }
    }

    private void initRecorder() {
        final int bufferSize = 2 * AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
        AudioUtil.initProcessor(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_ENCODING_BIT);

        recordingThread = new Thread("recorder") {
            @Override
            public void run() {
                super.run();
                buffer = new byte[bufferSize];
                Looper.prepare();
                audioRecord.setRecordPositionUpdateListener(recordPositionUpdateListener, new Handler(Looper.myLooper()));
                int bytePerSample = RECORDER_ENCODING_BIT / 8;
                float samplesToDraw = bufferSize / bytePerSample;
                audioRecord.setPositionNotificationPeriod((int) samplesToDraw);
                //We need to read first chunk to motivate recordPositionUpdateListener.
                //Mostly, for lower versions - https://code.google.com/p/android/issues/detail?id=53996
                audioRecord.read(buffer, 0, bufferSize);
                Looper.loop();
            }
        };
    }

    private void startRecording() {
        if (audioRecord != null) {
            audioRecord.startRecording();
        }
        recordingThread.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {

                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {

                        Toast.makeText(RecordActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RecordActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();

                    }
                }
                break;
        }
    }
}






