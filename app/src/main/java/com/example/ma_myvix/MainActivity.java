package com.example.ma_myvix;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {
    TextView textView1;
    Button button1;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textView1 = findViewById(R.id.chart);
        button1 = findViewById(R.id.checkButton);
//        button1.setOnClickListener(v -> button1ClickListener());

        initStockDB();
    }

    private void log(String s) {
        Log.d("@@@@@@LOG@@@@@@", s);
    }

    private void initStockDB() {
        createDB();
        importCSV();
    }

    private void createDB() {
        database = openOrCreateDatabase("StockDB", MODE_PRIVATE, null);
        log("DB 생성");
    }

    private void importCSV() {
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
        String name = getResources().getResourceEntryName(resId);
        if(checkTableExists(name)) {
            log("이미 존재하는 테이블: " + name);
            return;
        }

        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + name
                + "(date TEXT, price REAL, open REAL, high REAL, low REAL, volume INTEGER, change_percentage TEXT)";
        database.execSQL(createTableQuery);

        InputStream is = getResources().openRawResource(resId);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String row;
        while((row = br.readLine()) != null){
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
}