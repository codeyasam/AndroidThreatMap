package com.projects.codeyasam.threatmap;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.ByteArrayOutputStream;

public class RegisterActivity extends AppCompatActivity {

    private static final int SELECT_FILE = 888;
    private static final int REQUEST_CAMERA = 777;

    private SharedPreferences settings;
    public static Client_TM clientObj = new Client_TM();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Client_TM.setDefaultImage(RegisterActivity.this, R.id.displayPicture, R.drawable.defaultavatar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
        String clientId = settings.getString(Session_TM.LOGGED_USER_ID, "");
        if (!clientId.isEmpty()) {
            finish();
        }
    }

    public void nextBtnClick(View v) {
        clientObj.setIdentificationNo(CYM_UTILITY.getText(RegisterActivity.this, R.id.identificationTxt));
        clientObj.setFirstName(CYM_UTILITY.getText(RegisterActivity.this, R.id.firstNameTxt));
        clientObj.setMiddleName(CYM_UTILITY.getText(RegisterActivity.this, R.id.middleNameTxt));
        clientObj.setLastName(CYM_UTILITY.getText(RegisterActivity.this, R.id.lastNameTxt));
        clientObj.setContactNo(CYM_UTILITY.getText(RegisterActivity.this, R.id.contactNoTxt));
        clientObj.setPersonNotif(CYM_UTILITY.getText(RegisterActivity.this, R.id.personNotifyTxt));
        clientObj.setRelation(CYM_UTILITY.getText(RegisterActivity.this, R.id.relationTxt));

        if (clientObj.getIdentificationNo().isEmpty() ||
                clientObj.getFirstName().isEmpty() ||
                clientObj.getMiddleName().isEmpty() ||
                clientObj.getLastName().isEmpty() ||
                clientObj.getContactNo().isEmpty() ||
                clientObj.getPersonNotif().isEmpty() ||
                clientObj.getRelation().isEmpty()) {
            CYM_UTILITY.mAlertDialog("Fill up all required fields", RegisterActivity.this);
            //return;
        }

        Intent intent = new Intent(getApplicationContext(), SetLocation.class);
        startActivity(intent);
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
                    RegisterActivity.this.imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[which].equals("Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_FILE);
                }
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

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
        int dimenDP = (int) CYM_UTILITY.dipToPixels(RegisterActivity.this, 150);
        String imagePath = CYM_UTILITY.getPath(imageUri, RegisterActivity.this);
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
        bmp = Bitmap.createScaledBitmap(bmp, dimenDP, dimenDP, false);
        processImage(bmp, imagePath);
    }

    private void handleSelectFile(Intent data) {
        int dimenDP = (int) CYM_UTILITY.dipToPixels(RegisterActivity.this, 250);
        Uri imageUri = data.getData();
        String imagePath = CYM_UTILITY.getPath(imageUri, RegisterActivity.this);
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
        bmp = Bitmap.createScaledBitmap(bmp, dimenDP, dimenDP, false);
        processImage(bmp, imagePath);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
    }

    private void processImage(Bitmap bmp, String imagePath) {
        try {
            Matrix matrix = CYM_UTILITY.getMatrixAngle(imagePath);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            clientObj.setDisplayPicture(bmp);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            bmp = CYM_UTILITY.getRoundedCornerBitmap(bmp);
            CYM_UTILITY.setImageOnView(RegisterActivity.this, R.id.displayPicture, bmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
