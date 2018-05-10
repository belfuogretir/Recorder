package com.example.belfu.seskayit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by belfu on 28.02.2018.
 */

public class DataSource {

    private Context ourContext;
    private sqlite mySqlite;
    private SQLiteDatabase ourSqliteDatabase;

    public DataSource(Context context){
        ourContext = context;

    }



    public class sqlite extends SQLiteOpenHelper {
        String sql;

        public sqlite(Context context){
            super(context,"Note",null,1);
        }

        public void onCreate(SQLiteDatabase db){
            sql = "create table Note (voice text primary key,note text, location text)";
            db.execSQL(sql);
        }

        public void onUpgrade(SQLiteDatabase db, int old, int neww){
            db.execSQL(sql);
            onCreate(db);
        }

    }

    public DataSource open() throws SQLiteException{
        mySqlite = new sqlite(ourContext);
        ourSqliteDatabase = mySqlite.getWritableDatabase();
        return this;
    }

    public DataSource close(){
        mySqlite.close();
        return this;
    }


    public void add(String voiceN, String note, String loc) {
        ContentValues val = new ContentValues();
        val.put("voice",voiceN);
        val.put("note",note);
        val.put("location",loc);
        ourSqliteDatabase.insert("Note",null,val);
    }

    public String getNote(String name) {
        String[] columns = new String[]{"voice","note","location"};
        Cursor c = ourSqliteDatabase.query("Note",columns,null,null,null,null,null,null);

        String result ="";

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
        {
            if(name.equals(c.getString(0)))
                result = c.getString(1);
        }
                return result;
    }

    public String getLoc(String name) {
        String[] columns = new String[]{"voice","note","location"};
        Cursor c = ourSqliteDatabase.query("Note",columns,null,null,null,null,null,null);

        String resultL ="";

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
        {
            if(name.equals(c.getString(0)))
                resultL = c.getString(2);
        }
        return resultL;
    }
}
