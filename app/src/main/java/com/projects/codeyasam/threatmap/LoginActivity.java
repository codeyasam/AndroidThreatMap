package com.projects.codeyasam.threatmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        String clientId = settings.getString(Session_TM.LOGGED_USER_ID, "");
        if (!clientId.isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    public void loginClick(View v) {
        String username = CYM_UTILITY.getText(LoginActivity.this, R.id.username);
        String password = CYM_UTILITY.getText(LoginActivity.this, R.id.password);

    }

    public void regClick(View v) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}
