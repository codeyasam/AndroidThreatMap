package com.projects.codeyasam.threatmap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SetLoginDetails extends AppCompatActivity {

    private static final String THREAT_MAP_ANDROID_REG = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/androidReg.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_login_details);

        if (RegisterActivity.clientObj.getDisplayPicture() != null) {
            CYM_UTILITY.setImageOnView(SetLoginDetails.this, R.id.displayPicture, RegisterActivity.clientObj.getDisplayPicture());
        } else {
            Client_TM.setDefaultImage(SetLoginDetails.this, R.id.displayPicture, R.drawable.defaultavatar);
        }
    }

    public void regClick(View v) {
        String username = CYM_UTILITY.getText(SetLoginDetails.this, R.id.username);
        String password = CYM_UTILITY.getText(SetLoginDetails.this, R.id.password);
        String confPass = CYM_UTILITY.getText(SetLoginDetails.this, R.id.confPass);
        Log.i("poop", password);
        Log.i("poop", confPass);
        if (username.isEmpty() || password.isEmpty() || confPass.isEmpty()) {
            CYM_UTILITY.mAlertDialog("Fill all required fields.", SetLoginDetails.this);
            return;
        } else if (!password.equals(confPass)) {
            CYM_UTILITY.mAlertDialog("Password don't match.", SetLoginDetails.this);
            return;
        }

        RegisterActivity.clientObj.setUsername(username);
        RegisterActivity.clientObj.setPassword(password);
        RegisterHandler registerHandler = new RegisterHandler(RegisterActivity.clientObj);
        registerHandler.execute();
    }

    public class RegisterHandler extends AsyncTask<String, String, String> {

        private Client_TM clientObj;

        public RegisterHandler(Client_TM clientObj) {
            this.clientObj = clientObj;
        }

        @Override
        protected String doInBackground(String... args) {

            try {
                List<NameValuePair> params = new ArrayList<>();
                if (clientObj.getDisplayPicture() != null) {
                    Bitmap display_picture = clientObj.getDisplayPicture();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    display_picture.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    String encodedImage = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
                    params.add(new BasicNameValuePair("image", encodedImage));
                } else {
                    params.add(new BasicNameValuePair("display_picture", clientObj.getDisplayPicturePath()));
                }

                params.add(new BasicNameValuePair("first_name", clientObj.getFirstName()));
                params.add(new BasicNameValuePair("middle_name", clientObj.getMiddleName()));
                params.add(new BasicNameValuePair("last_name", clientObj.getLastName()));
                params.add(new BasicNameValuePair("address", clientObj.getAddress()));
                params.add(new BasicNameValuePair("lat", clientObj.getLat()));
                params.add(new BasicNameValuePair("lng", clientObj.getLng()));
                params.add(new BasicNameValuePair("contact_no", clientObj.getContactNo()));
                params.add(new BasicNameValuePair("person_to_notify", clientObj.getPersonNotif()));
                params.add(new BasicNameValuePair("relationship", clientObj.getRelation()));
                params.add(new BasicNameValuePair("identification_number", clientObj.getIdentificationNo()));
                params.add(new BasicNameValuePair("username", clientObj.getUsername()));
                params.add(new BasicNameValuePair("password", clientObj.getPassword()));
                params.add(new BasicNameValuePair("submit", "true"));

                JSONObject json = JSONParser.makeHttpRequest(THREAT_MAP_ANDROID_REG, "POST", params);
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("poop", result);
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getString("success").equals("true")) {
                        JSONObject jsonClient = json.getJSONObject("Client");
                        Session_TM.logUser(SetLoginDetails.this, jsonClient.getString("id"));
                        SetLoginDetails.this.finish();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
