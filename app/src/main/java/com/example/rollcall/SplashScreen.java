//Okan Ari 200001696
//Yiğitcan Güler 200001709
package com.example.rollcall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginlayout);
        Intent intent = new Intent(this, LogInScreen.class);
        startActivity(intent);
        finish();
    }
}