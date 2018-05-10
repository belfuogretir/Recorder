package com.example.belfu.seskayit;
//https://www.android-examples.com/android-audio-voice-recording-app-example-tutorial-with-source-code/
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
        import android.media.MediaRecorder;
        import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
        import static android.Manifest.permission.RECORD_AUDIO;
        import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        import android.support.v4.app.ActivityCompat;
        import android.content.pm.PackageManager;
        import android.support.v4.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    Button buttondeneme ;
    ImageButton buttonStart;
    ListView listview;
    Random random ;
    ArrayList<String> voiceList = new ArrayList<>();
    public static final int RequestPermissionCode = 1;
    int i;
    File[] files;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart = (ImageButton) findViewById(R.id.button);
        buttondeneme = (Button) findViewById(R.id.button6);
        listview = (ListView) findViewById(R.id.listview);

        random = new Random();
        final ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, android.R.id.text1, voiceList);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),RecordActivity.class);
                startActivity(intent);
            }
        });

        buttondeneme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voiceList.clear();
                String path = Environment.getExternalStorageDirectory().toString() + "/Record";
                File directory = new File(path);
                files = directory.listFiles();
                for (int i = 0; i < files.length; i++) {
                    voiceList.add(files[i].getName());
                }
                listview.setAdapter(adapter1);
                adapter1.notifyDataSetChanged();
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectVoice = voiceList.get(i);
                Log.wtf("selectVoice",selectVoice);
                Intent intent = new Intent(getApplicationContext(),PlayActivity.class);
                intent.putExtra("VoiceKey",selectVoice);
                startActivity(intent);

            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
                adb.setTitle("Delete?");
                adb.setMessage("Are you sure you want to delete " + files[i].getName());
                final int positionToRemove = i;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            delete(files[i]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        voiceList.remove(positionToRemove);
                        adapter1.notifyDataSetChanged();
                    }});
                adb.show();
                return false;
            }
        });
    }


    public void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                if(f.listFiles().toString().contains((CharSequence) files[i]))
                    delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }


   //kendi cacheini silmek için
    @Override
    protected void onStop(){
        super.onStop();
    }

    //Fires after the OnStop() state
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            trimCache(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

}