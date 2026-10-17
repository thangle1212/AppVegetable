package com.example.appvegetables;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appvegetables.home.Home;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chuyển sang trang Home khi mở app
        Intent intent = new Intent(MainActivity.this, Home.class);
        startActivity(intent);
        finish(); // Đóng MainActivity để không quay lại màn này
    }
}
