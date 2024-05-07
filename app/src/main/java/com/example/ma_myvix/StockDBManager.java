package com.example.ma_myvix;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

public class StockDBManager {
    private Context context;
    private SQLiteDatabase database;

    public StockDBManager(Context context, SQLiteDatabase database){
        this.context = context;
        this.database = database;
    }

    private void log(String s) {
        Log.d("@@@@@@LOG@@@@@@", s);
    }

    public void importCSV() {
        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                int resId = field.getInt(null);
                createTable(resId);
            } catch (Exception e) {
                log("csv 파일 불러오기 실패 : " + e);
            }
        }
    }

    private void createTable(int resId) throws IOException {
        String name = context.getResources().getResourceEntryName(resId);
        if(checkTableExists(name)) {
            log("이미 존재하는 테이블: " + name);
            return;
        }

        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + name
                + "(date TEXT, price REAL, open REAL, high REAL, low REAL, volume INTEGER, change_percentage TEXT)";
        database.execSQL(createTableQuery);

        InputStream is = context.getResources().openRawResource(resId);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String row;
        while((row = br.readLine()) != null){
            row = row.replace("\"", "");
            String[] values = row.split(",");

            String insertQuery = "INSERT INTO " + name + " (date, price, open, high, low, volume, change_percentage) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            database.execSQL(insertQuery, values);
        }
        br.close();
        isr.close();
        is.close();
        log(name + "테이블 생성 완료");
    }

    private boolean checkTableExists(String tableName) {
        Cursor cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public float getPrice(String symbol, String date){
        String sql = "SELECT price FROM " + symbol + " WHERE date = '" + date + "'";
        Cursor cursor;
        cursor = database.rawQuery(sql, null);

        float result = -999f;
        while(cursor.moveToNext()){
            result = cursor.getFloat(0);
        }
        log("날짜 : " + date + "\t가격 : " + result);
        cursor.close();
        return result;
    }
}
