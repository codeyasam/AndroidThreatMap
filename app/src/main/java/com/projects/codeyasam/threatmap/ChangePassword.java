package com.projects.codeyasam.threatmap;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChangePassword extends AppCompatActivity {

    private static final String CHANGE_PASS_URL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/changePassword.php";

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public void onChangePass(View view) {
        String oldPass = CYM_UTILITY.getText(ChangePassword.this, R.id.oldPassowrd);
        String newPass = CYM_UTILITY.getText(ChangePassword.this, R.id.newPass);
        String confPass = CYM_UTILITY.getText(ChangePassword.this, R.id.confirmPass);

        if (!newPass.equals(confPass)) {
            CYM_UTILITY.mAlertDialog("Passwords don't match!", ChangePassword.this);
            return;
        }

        new ChangePasswordExecuter(oldPass, newPass).execute();
    }

    class ChangePasswordExecuter extends AsyncTask<String, String, String> {

        private String oldPass;
        private String newPass;

        public ChangePasswordExecuter(String oldPass, String newPass) {
            this.oldPass = oldPass;
            this.newPass = newPass;
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                String userId = settings.getString(Session_TM.LOGGED_USER_ID, "");
                String userType = settings.getString(Session_TM.LOGGED_USER_TYPE, "");
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("user_id", userId));
                params.add(new BasicNameValuePair("user_type", userType));
                params.add(new BasicNameValuePair("oldPass", oldPass));
                params.add(new BasicNameValuePair("newPass", newPass));
                params.add(new BasicNameValuePair("submit", "true"));
                JSONObject json = JSONParser.makeHttpRequest(CHANGE_PASS_URL, "POST", params);
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
                    JSONObject json = new JSONObject(result);
                    if (json.getString("success").equals("true")) {
                        CYM_UTILITY.mAlertDialog("Successfully Changed Password", ChangePassword.this);
                    } else {
                        if (json.has("msg")) {
                            CYM_UTILITY.mAlertDialog(json.getString("msg"), ChangePassword.this);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
