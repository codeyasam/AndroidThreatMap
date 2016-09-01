package com.projects.codeyasam.threatmap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RegisterActivity extends AppCompatActivity {

    private static final int SELECT_FILE = 888;
    private static final int REQUEST_CAMERA = 777;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void nextBtnClick(View v) {
        Intent intent = new Intent(getApplicationContext(), SetLocation.class);
        startActivity(intent);
    }

}
