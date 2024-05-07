package com.example.ma_myvix;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    TextView textView1;
    Button button1;
    StockDBManager stockDBManager;
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

        findView();
        initDB();
    }

    private void findView() {
        textView1 = findViewById(R.id.chart);
        button1 = findViewById(R.id.checkButton);

        button1.setOnClickListener(v -> button1ClickListener());
    }

    private void initDB() {
        database = openOrCreateDatabase("StockDB", MODE_PRIVATE,null);
        log("DB 생성");
        stockDBManager = new StockDBManager(getApplicationContext(), this.database);
        stockDBManager.importCSV();
    }

    private void log(String s) { Log.d("@@@@@@LOG@@@@@@", s); }
    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();}

    private void button1ClickListener(){
        String symbol = "aapl";
        String date = "04/21/2024";
        stockDBManager.getPrice(symbol, date);
    }
}