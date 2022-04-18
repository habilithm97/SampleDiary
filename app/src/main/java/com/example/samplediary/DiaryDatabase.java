package com.example.samplediary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Paint;
import android.util.Log;

import androidx.annotation.Nullable;

public class DiaryDatabase { // 데이터 베이스 클래스
    private static final String TAG = "DiaryDatabase";

    private static DiaryDatabase database;
    public static String TABLE_DIARY = "DIARY"; // 테이블 이름
    public static int DB_VERSION = 1;

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Context context;

    private DiaryDatabase(Context context) {
        this.context = context;
    }

    public static DiaryDatabase getInstance(Context context) {
        // 데이터 베이스가 없으면 생성
        if(database == null) {
            database = new DiaryDatabase(context);
        }
        return database;
    }

    public boolean open() {
        println("[ " + AppConstants.DATABASE_NAME + " ] 데이터 베이스가 오픈됨. ");

        // 헬퍼 객체 생성하고 db 객체 참조함
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();

        return true;
    }

    public void close() {
        println("[ " + AppConstants.DATABASE_NAME + " ] 데이터 베이스를 닫음. ");

        db.close();
        database = null;
    }

    /*
    -SELECT 명령어를 사용하여 query를 실행함
    -쿼리의 결과는 Cursor 객체를 리턴함
    -Cursor 객체는 쿼리에 의하여 생성된 행들을 가리킴
    -Cursor는 데이터 베이스에서 결과를 순회하고 데이터를 읽는데 사용되는 표준적인 메커니즘임
     */
    public Cursor rawQuery(String SQL) {
        println("\nexecuteQuery가 호출됨. ");

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SQL, null);
            println("cursor count : " + cursor.getCount());
        } catch (Exception e) {
            Log.e(TAG, "executeQuery 호출 중 오류가 발생함. ", e);
        }
        return cursor;
    }

    // SELECT 명령을 제외한 모든 SQL 문장을 실행함
    public boolean execSQL(String SQL) {
        println("\nexecute 호출됨. ");

        try {
            Log.d(TAG, "SQL : " + SQL);
            db.execSQL(SQL);
        } catch (Exception e) {
            Log.e(TAG, "execute 호출 중 오류가 발생함.", e);
        }
        return true;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(@Nullable Context context) {
            super(context, AppConstants.DATABASE_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            println("[ " + AppConstants.DATABASE_NAME + " ] 데이터 베이스 생성됨. ");
            println("[ " + TABLE_DIARY + " ] 테이블 생성됨. ");

            String DROP_SQL = "drop table if exists " + TABLE_DIARY; // 테이블 삭제
            try {
                db.execSQL(DROP_SQL);
            } catch (Exception e) {
                Log.e(TAG, "테이블을 삭제하는데에 오류가 발생함. ");
            }

            String CREATE_SQL = "create table " + TABLE_DIARY + "(" // 테이블 생성
                    + "  _id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + "  WEATHER TEXT DEFAULT '', "
                    + "  ADDRESS TEXT DEFAULT '', "
                    + "  LOCATION_X TEXT DEFAULT '', "
                    + "  LOCATION_Y TEXT DEFAULT '', "
                    + "  CONTENTS TEXT DEFAULT '', "
                    + "  MOOD TEXT, "
                    + "  PICTURE TEXT DEFAULT '', "
                    + "  CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "  MODIFY_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP "
                    + ")";

            try {
                db.execSQL(CREATE_SQL);
            } catch (Exception e) {
                Log.e(TAG, "테이블을 생성하는데에 오류가 발생함. ");
            }

            String CREATE_INDEX_SQL = "create index " + TABLE_DIARY + "_IDX ON " + TABLE_DIARY + "(" + "CREATE_DATE" + ")";
            try {
                db.execSQL(CREATE_INDEX_SQL);
            } catch (Exception e) {
                Log.e(TAG, "테이블 인덱스를 생성하는데에 오류가 발생함. ");
            }
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            println("[ " + AppConstants.DATABASE_NAME + " ] 데이터 베이스 오픈됨. ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            println(oldVersion + " 버전에서 " + newVersion + " 버전으로 데이터 베이스 업데이트됨. ");
        }
    }

    public void println(String data) {
        Log.d(TAG, data);
    }
}
