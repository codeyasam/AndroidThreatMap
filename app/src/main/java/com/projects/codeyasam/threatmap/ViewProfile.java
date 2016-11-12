package com.projects.codeyasam.threatmap;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewProfile extends AppCompatActivity {

    private Client_TM clientObj;
    private static final String REQUEST_RESPONDER_URL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/respondRequest.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        clientObj = Client_TM.clientTm;
        CYM_UTILITY.displayText(ViewProfile.this, R.id.fullName, clientObj.getFullName());
        //CYM_UTILITY.setImageOnView(ViewProfile.this, R.id.displayPicture, CYM_UTILITY.getResizedBitmap(clientObj.getDisplayPicture(), 100, 100));
        CYM_UTILITY.setImageOnView(ViewProfile.this, R.id.displayPicture, clientObj.getDisplayPicture());
        CYM_UTILITY.displayText(ViewProfile.this, R.id.contactNo, clientObj.getContactNo());
        CYM_UTILITY.displayText(ViewProfile.this, R.id.address, "Address: " + clientObj.getAddress());
        CYM_UTILITY.displayText(ViewProfile.this, R.id.identificationNo, "ID No: " + clientObj.getIdentificationNo());
        CYM_UTILITY.displayText(ViewProfile.this, R.id.emailAddr, clientObj.getEmail());
        CYM_UTILITY.displayText(ViewProfile.this, R.id.personNotify, "Person to Notify: " + clientObj.getPersonNotif());
    }

    public void respondRequest(View view) {
        CYM_UTILITY.callYesNoMessage("Are you sure to respond to this request?", ViewProfile.this, respondToRequest());
    }

    public DialogInterface.OnClickListener respondToRequest() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new RequestResponder().execute();
            }
        };
    }

    class RequestResponder extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("respondRequest", "true"));
                params.add(new BasicNameValuePair("notifId", clientObj.requestId));

                JSONObject json = JSONParser.makeHttpRequest(REQUEST_RESPONDER_URL, "POST", params);
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
                        CYM_UTILITY.okActionDialog("Request Successfully Responded.", ViewProfile.this, okDialogClick());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private DialogInterface.OnClickListener okDialogClick() {
            return new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            };
        }
    }
}
