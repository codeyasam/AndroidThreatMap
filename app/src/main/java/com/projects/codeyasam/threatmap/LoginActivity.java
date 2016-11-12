package com.projects.codeyasam.threatmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String THREAT_MAP_LOGIN = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/androidLogin.php";

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
            String userType = settings.getString(Session_TM.LOGGED_USER_TYPE, "");
            if (userType.equals("CLIENT")) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else if (userType.equals("USER") || userType.equals("HEAD")) {
                Intent intent = new Intent(getApplicationContext(), MainAdminActivity.class);
                startActivity(intent);
            }

        }
    }

    public void loginClick(View v) {
        String username = CYM_UTILITY.getText(LoginActivity.this, R.id.usernameTxt);
        String password = CYM_UTILITY.getText(LoginActivity.this, R.id.passwordTxt);

        new LoginLoader(username, password).execute();
    }

    public void regClick(View v) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    class LoginLoader extends AsyncTask<String, String, String> {

        private String username;
        private String password;

        public LoginLoader(String username, String password) {
            this.username = username;
            this.password = password;
        }


        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));
                params.add(new BasicNameValuePair("login", "true"));

                JSONObject json = JSONParser.makeHttpRequest(THREAT_MAP_LOGIN, "POST", params);
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    Log.i("poop", result.toString());
                    JSONObject json = new JSONObject(result);
                    if (json.getString("login").equals("true")) {
                        Intent intent = new Intent();
                        if (json.getString("access_level").equals("CLIENT")) {
                            intent = new Intent(LoginActivity.this.getApplicationContext(), MainActivity.class);
                        } else {
                            intent = new Intent(getApplicationContext(), MainAdminActivity.class);
                        }

                        Session_TM.logUser(LoginActivity.this, json.getString("id"), json.getString("user_type"));
                        startActivity(intent);
                    } else {
                        CYM_UTILITY.mAlertDialog("wrong username/password", LoginActivity.this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
