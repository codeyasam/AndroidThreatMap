package com.projects.codeyasam.threatmap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class EditProfile extends AppCompatActivity {

    private static final String PROFILE_LOADER_URL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/profileLoader.php";
    private static final String PROFILE_EDITOR_URL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/editProfileUser.php";

    private static final int SELECT_FILE = 888;
    private static final int REQUEST_CAMERA = 777;

    private SharedPreferences settings;
    private Client_TM userObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userObj = new Client_TM();
        userObj.setId(settings.getString(Session_TM.LOGGED_USER_ID, ""));
        CYM_UTILITY.setDefaultImage(EditProfile.this, R.id.displayPicture, R.drawable.defaultavatar);

        String userType = settings.getString(Session_TM.LOGGED_USER_TYPE, "");
        if (userType.equals("CLIENT")) {
            EditText identificationTxt = (EditText) findViewById(R.id.identificationTxt);
            EditText personToNotifyTxt = (EditText) findViewById(R.id.personNotifyTxt);
            EditText relationTxt = (EditText) findViewById(R.id.relationTxt);
            identificationTxt.setVisibility(View.VISIBLE);
            personToNotifyTxt.setVisibility(View.VISIBLE);
            relationTxt.setVisibility(View.VISIBLE);
        }
        new ProfileLoader().execute();
    }

    private Uri imageUri;
    public void choosePicture(View v) {
        final CharSequence[] items = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CHOOSE PHOTO FROM");
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("Camera")) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                    EditProfile.this.imageUri = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[which].equals("Gallery")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_FILE);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });

        builder.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    handleRequestCamera();
                    break;
                case SELECT_FILE:
                    handleSelectFile(data);
                    break;
                default:

            }
        }
    }

    private void handleRequestCamera() {
        int dimenDP = (int) CYM_UTILITY.dipToPixels(EditProfile.this, 150);
        String imagePath = CYM_UTILITY.getPath(imageUri, EditProfile.this);
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
        bmp = Bitmap.createScaledBitmap(bmp, dimenDP, dimenDP, false);
        processImage(bmp, imagePath);
    }

    private void handleSelectFile(Intent data) {
        int dimenDP = (int) CYM_UTILITY.dipToPixels(EditProfile.this, 250);
        Uri imageUri = data.getData();
        String imagePath = CYM_UTILITY.getPath(imageUri, EditProfile.this);
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
        bmp = Bitmap.createScaledBitmap(bmp, dimenDP, dimenDP, false);
        processImage(bmp, imagePath);
    }

    private void processImage(Bitmap bmp, String imagePath) {
        try {
            Matrix matrix = CYM_UTILITY.getMatrixAngle(imagePath);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            userObj.setDisplayPicture(bmp);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            bmp = CYM_UTILITY.getRoundedCornerBitmap(bmp);
            CYM_UTILITY.setImageOnView(EditProfile.this, R.id.displayPicture, bmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onEditProfile(View view) {
        if (CYM_UTILITY.getText(EditProfile.this, R.id.firstNameTxt).isEmpty() || CYM_UTILITY.getText(EditProfile.this, R.id.middleNameTxt).isEmpty() ||
                CYM_UTILITY.getText(EditProfile.this, R.id.lastNameTxt).isEmpty() || CYM_UTILITY.getText(EditProfile.this, R.id.contactNoTxt).isEmpty()) {
            CYM_UTILITY.mAlertDialog("Fill all required fields.", EditProfile.this);
            return;
        }
        String userId = settings.getString(Session_TM.LOGGED_USER_ID, "");
        userObj.setId(userId);
        userObj.setFirstName(CYM_UTILITY.getText(EditProfile.this, R.id.firstNameTxt));
        userObj.setMiddleName(CYM_UTILITY.getText(EditProfile.this, R.id.middleNameTxt));
        userObj.setLastName(CYM_UTILITY.getText(EditProfile.this, R.id.lastNameTxt));
        userObj.setContactNo(CYM_UTILITY.getText(EditProfile.this, R.id.contactNoTxt));

        String userType = settings.getString(Session_TM.LOGGED_USER_TYPE, "");
        if (userType.equals("CLIENT")) {
            if (CYM_UTILITY.getText(EditProfile.this, R.id.identificationTxt).isEmpty() ||
                    CYM_UTILITY.getText(EditProfile.this, R.id.personNotifyTxt).isEmpty() ||
                    CYM_UTILITY.getText(EditProfile.this, R.id.relationTxt).isEmpty()) {

                CYM_UTILITY.mAlertDialog("Fill all required fields.", EditProfile.this);
                return;
            }
            userObj.setIdentificationNo(CYM_UTILITY.getText(EditProfile.this, R.id.identificationTxt));
            userObj.setPersonNotif(CYM_UTILITY.getText(EditProfile.this, R.id.personNotifyTxt));
            userObj.setRelation(CYM_UTILITY.getText(EditProfile.this, R.id.relationTxt));
        }

        new ProfileEditor().execute();
    }

    class ProfileLoader extends AsyncTask<String, String, String> {

        private Client_TM clientObj;
        private ProgressDialog progressDialog;

        public ProfileLoader() {
            progressDialog = new ProgressDialog(EditProfile.this);
            progressDialog.setMessage("Loading Profile...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                String userId = settings.getString(Session_TM.LOGGED_USER_ID, "");
                String userType = settings.getString(Session_TM.LOGGED_USER_TYPE, "");
                JSONObject json = JSONParser.getJSONfromURL(PROFILE_LOADER_URL + "?userId=" + userId + "&userType=" + userType);
                if (userType.equals("CLIENT")) {
                    clientObj = Client_TM.instantiateJSON(json);
                } else {
                    clientObj = Client_TM.instantiateJSONasUser(json);
                }
                userObj.setDisplayPicture(null);
                userObj.setDisplayPicturePath(clientObj.getDisplayPicturePath());
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    CYM_UTILITY.setText(EditProfile.this, R.id.firstNameTxt, clientObj.getFirstName());
                    CYM_UTILITY.setText(EditProfile.this, R.id.middleNameTxt, clientObj.getMiddleName());
                    CYM_UTILITY.setText(EditProfile.this, R.id.lastNameTxt, clientObj.getLastName());
                    CYM_UTILITY.setText(EditProfile.this, R.id.contactNoTxt, clientObj.getContactNo());
                    CYM_UTILITY.setImageOnView(EditProfile.this, R.id.displayPicture, CYM_UTILITY.getRoundedCornerBitmap(clientObj.getDisplayPicture()));

                    String userType = settings.getString(Session_TM.LOGGED_USER_TYPE, "");
                    if (userType.equals("CLIENT")) {
                        CYM_UTILITY.setText(EditProfile.this, R.id.identificationTxt, clientObj.getIdentificationNo());
                        CYM_UTILITY.setText(EditProfile.this, R.id.personNotifyTxt, clientObj.getPersonNotif());
                        CYM_UTILITY.setText(EditProfile.this, R.id.relationTxt, clientObj.getRelation());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ProfileEditor extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;

        public ProfileEditor() {
            progressDialog = new ProgressDialog(EditProfile.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Updating Profile...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                if (userObj.getDisplayPicture() != null) {
                    Log.i("poop", "has dp");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    userObj.getDisplayPicture().compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    String encodedImage = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
                    params.add(new BasicNameValuePair("image", encodedImage));
                } else {
                    Log.i("poop", "has no dp");
                    params.add(new BasicNameValuePair("display_picture", userObj.getDisplayPicturePath()));
                }
                String userType = settings.getString(Session_TM.LOGGED_USER_TYPE, "");
                params.add(new BasicNameValuePair("user_type", userType));
                params.add(new BasicNameValuePair("user_id", userObj.getId()));
                params.add(new BasicNameValuePair("first_name", userObj.getFirstName()));
                params.add(new BasicNameValuePair("middle_name", userObj.getMiddleName()));
                params.add(new BasicNameValuePair("last_name", userObj.getLastName()));
                params.add(new BasicNameValuePair("contact_no", userObj.getContactNo()));
                params.add(new BasicNameValuePair("submit", "true"));

                if (userType.equals("CLIENT")) {
                    params.add(new BasicNameValuePair("identification_no", userObj.getIdentificationNo()));
                    params.add(new BasicNameValuePair("person_to_notify", userObj.getPersonNotif()));
                    params.add(new BasicNameValuePair("relationship", userObj.getRelation()));
                }

                JSONObject json = JSONParser.makeHttpRequest(PROFILE_EDITOR_URL, "POST", params);
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getString("success").equals("true")) {
                        CYM_UTILITY.mAlertDialog("Successfully Updated Profile.", EditProfile.this);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
