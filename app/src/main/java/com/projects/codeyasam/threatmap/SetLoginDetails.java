package com.projects.codeyasam.threatmap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SetLoginDetails extends AppCompatActivity {

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

    }
}
