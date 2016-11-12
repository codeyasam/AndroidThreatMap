package com.projects.codeyasam.threatmap;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class OfficeActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMarkerClickListener {

    public static final String OFFICE_LOADER_URL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/getOffices.php?allOffices=true";
    public static final String REPORT_EMERGENCY_ALL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/askForHelp.php";

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private double mLat;
    private double mLng;
    private SharedPreferences settings;

    private HashMap<String, Office_TM> hmOffices;
    private String currentCountry;
    private String currentMunicipality;
    private String currentProvince;
    private HashMap<String, Marker> markers;

    private Button btnEmer;
    private Notif_TM notifObj;
    private int count = 0;
    private long startMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_office);
        settings = PreferenceManager.getDefaultSharedPreferences(OfficeActivity.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

        hmOffices = new HashMap<>();
        markers = new HashMap<>();

        btnEmer = (Button) findViewById(R.id.emerBtn);
        btnEmer.setEnabled(false);
        Timer btnTimer = new Timer();
        btnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long time = System.currentTimeMillis();
                        if (startMillis == 0 || time-startMillis > 3000) {
                            String prompt = "ASK FOR HELP (Tap 7 times)";
                            btnEmer.setText(prompt);
                            count = 0;
                        }
                    }
                });

            }
        }, 0, 1000);

        notifObj = new Notif_TM();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.threat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.countryMenu) {
            for (Map.Entry<String, Office_TM> entry : hmOffices.entrySet()) {
                Office_TM officeObj = entry.getValue();
                Marker marker = markers.get(entry.getKey());
                if (!currentCountry.equals(officeObj.getCountry())) {
                    marker.setVisible(false);
                } else {
                    marker.setVisible(true);
                }
            }
        } else if (item.getItemId() == R.id.provinceMenu) {
            for (Map.Entry<String, Office_TM> entry : hmOffices.entrySet()) {
                Office_TM officeObj = entry.getValue();
                Marker marker = markers.get(entry.getKey());
                if (!currentProvince.equals(officeObj.getProvince())) {
                    marker.setVisible(false);
                } else {
                    marker.setVisible(true);
                }
            }
        } else if (item.getItemId() == R.id.cityMenu) {
            for (Map.Entry<String, Office_TM> entry : hmOffices.entrySet()) {
                Office_TM officeObj = entry.getValue();
                Marker marker = markers.get(entry.getKey());
                if (!currentMunicipality.equals(officeObj.getMunicipality())) {
                    marker.setVisible(false);
                } else {
                    marker.setVisible(true);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void reportEmer(View v) {
        long time = System.currentTimeMillis();
        startMillis = time;
        count++;
        if (count >= 7) {
            count = 0;
            setupNotifObj();
            new EmergencyReporter(notifObj, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        int numReq = 7 - count;
        String prompt = "ASK FOR HELP (tap " + numReq + " times)";
        btnEmer.setText(prompt);
    }

    private void setupNotifObj() {
        try {
            LatLng ll = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(ll.latitude, ll.longitude, 1);
            if (addressList.size() > 0) {
                notifObj.setAddress(addressList.get(0).getAddressLine(0) + ", " + addressList.get(0).getLocality() + ", " + addressList.get(0).getSubAdminArea() + ", " + addressList.get(0).getCountryName());
                notifObj.setClient_id(settings.getString(Session_TM.LOGGED_USER_ID, ""));
                notifObj.setMunicipality(addressList.get(0).getLocality());
                notifObj.setProvince(addressList.get(0).getSubAdminArea());
                notifObj.setCountry(addressList.get(0).getCountryName());
                notifObj.setLat(String.valueOf(ll.latitude));
                notifObj.setLng(String.valueOf(ll.longitude));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            LatLng ll = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 10);
            mMap.moveCamera(update);
            btnEmer.setEnabled(true);
            new OfficeLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (mLastLocation != null) {
            mLat = mLastLocation.getLatitude();
            mLng = mLastLocation.getLongitude();
            LatLng latLng = new LatLng(mLat, mLng);
        }
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        Office_TM officeObj = hmOffices.get(marker.getTitle());
        CYM_UTILITY.callYesNoMessage(officeObj.getName() + "\n Ask help from this office?", OfficeActivity.this, sendRequestSpecific(officeObj));
        return true;
    }

    private DialogInterface.OnClickListener sendRequestSpecific(final Office_TM officeObj) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setupNotifObj();
                notifObj.setOffice_id(officeObj.getId());
                dialog.dismiss();
                new EmergencyReporter(notifObj, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        };
    }

    class OfficeLoader extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                JSONObject json = JSONParser.getJSONfromURL(OFFICE_LOADER_URL);
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
                    JSONArray jsonArray = json.getJSONArray("allOffices");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        EachOfficeLoader task = new EachOfficeLoader(jsonArray.getJSONObject(i));
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class EachOfficeLoader extends AsyncTask<String, String, String> {

        private JSONObject officeJSON;
        private Office_TM officeObj;

        public EachOfficeLoader(JSONObject officeJSON) {
            this.officeJSON = officeJSON;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                officeObj = Office_TM.instantiateJSON(officeJSON);
                hmOffices.put(officeObj.getId(), officeObj);
                return "true";
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
                    LatLng latLng = new LatLng(Double.parseDouble(officeObj.getLat()), Double.parseDouble(officeObj.getLng()));
                    MarkerOptions options = new MarkerOptions()
                            .position(latLng)
                            .title(officeObj.getId());

                    markers.put(officeObj.getId(), mMap.addMarker(options));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class EmergencyReporter extends AsyncTask<String, String, String> {

        private Notif_TM notifObj;
        private ProgressDialog progressDialog;
        private boolean specificOffice;

        public EmergencyReporter(Notif_TM notifObj, boolean specificOffice) {
            this.notifObj = notifObj;
            this.specificOffice = specificOffice;
            progressDialog = new ProgressDialog(OfficeActivity.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Sending Request...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                if (!specificOffice)  {
                    params.add(new BasicNameValuePair("allNearest", "true"));
                } else {
                    params.add(new BasicNameValuePair("specificOffice", "true"));
                    params.add(new BasicNameValuePair("office_id", notifObj.getOffice_id()));
                }
                Log.i("poop", "specificOffice: " + specificOffice);
                params.add(new BasicNameValuePair("client_id", notifObj.getClient_id()));
                params.add(new BasicNameValuePair("lat", notifObj.getLat()));
                params.add(new BasicNameValuePair("lng", notifObj.getLng()));
                params.add(new BasicNameValuePair("address", notifObj.getAddress()));
                params.add(new BasicNameValuePair("municipality", notifObj.getMunicipality()));
                params.add(new BasicNameValuePair("province", notifObj.getProvince()));
                params.add(new BasicNameValuePair("country", notifObj.getCountry()));

                JSONObject json = JSONParser.makeHttpRequest(REPORT_EMERGENCY_ALL, "POST", params);
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
            CYM_UTILITY.mAlertDialog("Request Sent!", OfficeActivity.this);
        }

    }
}
