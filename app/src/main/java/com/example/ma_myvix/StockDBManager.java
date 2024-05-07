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
    final private Context context;
    final private SQLiteDatabase database;

    public StockDBManager(Context context, SQLiteDatabase database){
        this.context = context;
        this.database = database;
    }

    private void log(String s) {
        Log.d("@@@@@@LOG@@@@@@", s);
    }
    private void log(double i) {
        Log.d("@@@@@@LOG@@@@@@", String.valueOf(i));
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
                + "(date REAL, price REAL, open REAL, high REAL, low REAL, volume INTEGER, change_percentage TEXT)";
        database.execSQL(createTableQuery);

        InputStream is = context.getResources().openRawResource(resId);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String row;
        while((row = br.readLine()) != null){
            row = row.replace("\"", "");
            String[] values = row.split(",");
            String[] date = values[0].split("/");
            values[0] = date[2]+date[0]+date[1];

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

    public double getPrice(String symbol, int date){
        String sql = String.format("SELECT price FROM %s WHERE date = %d", symbol, date);
        Cursor cursor;
        cursor = database.rawQuery(sql, null);

        double result = 0;
        while(cursor.moveToNext()){
            result = cursor.getDouble(0);
        }
        cursor.close();
        log(symbol+" ("+date+"): "+result);
        return result;
    }

    public String getReturnRate(String symbol, int startDate, int endDate){
        double startPrice = getPrice(symbol, startDate);
        double endPrice = getPrice(symbol, endDate);
        double returnRate = ((endPrice - startPrice) / startPrice) * 100;
        return String.format("%.2f%%", returnRate);
    }


    public double getVolatility(String symbol, int startDate, int endDate){
        // Calculate average change percentage
        String avgQuery = String.format("SELECT avg(change_percentage) FROM %s WHERE date >= ? AND date <= ?", symbol);
        Cursor avgCursor = database.rawQuery(avgQuery, new String[]{String.valueOf(startDate), String.valueOf(endDate)});
        double average = 0.0;
        if (avgCursor.moveToFirst()) {
            average = avgCursor.getDouble(0);
        }
        avgCursor.close();

        // Calculate standard deviation using SQL query
        String stdDevQuery = String.format("SELECT sum((change_percentage - ?) * (change_percentage - ?)) / count(*) FROM %s WHERE date >= ? AND date <= ?", symbol);
        Cursor stdDevCursor = database.rawQuery(stdDevQuery, new String[]{String.valueOf(average), String.valueOf(average), String.valueOf(startDate), String.valueOf(endDate)});
        double variance = 0.0;
        if (stdDevCursor.moveToFirst()) {
            variance = stdDevCursor.getDouble(0);
        }
        stdDevCursor.close();

        // Return square root of variance as standard deviation
        return Math.sqrt(variance);
    }
}
