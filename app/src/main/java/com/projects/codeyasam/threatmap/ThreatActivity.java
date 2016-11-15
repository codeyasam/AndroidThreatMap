package com.projects.codeyasam.threatmap;

import android.*;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ThreatActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener {

    public static final String THREATS_URL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/getThreats.php?allThreats=true";
    public static final String REPORT_EMERGENCY_ALL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/askForHelp.php";

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private double mLat;
    private double mLng;
    private SharedPreferences settings;

    private HashMap<String, Threat_TM> hmThreat;
    private HashMap<String, Integer> hmMunicipality;
    private HashMap<String, Integer> hmProvince;
    private HashMap<String, Integer> hmCountry;
    private HashMap<String, Marker> markers;

    private Button btnEmer;
    private Notif_TM notifObj;
    private int count = 0;
    private long startMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threat);
        settings = PreferenceManager.getDefaultSharedPreferences(ThreatActivity.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

        hmThreat = new HashMap<>();
        hmMunicipality = new HashMap<>();
        hmProvince = new HashMap<>();
        hmCountry = new HashMap<>();
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.threat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            Log.i("poop", "province: " + hmProvince.toString() + " Municipality: " + hmMunicipality.toString());
            if (item.getItemId() == R.id.provinceMenu) {
                String highestProvince = getHighestCount(hmProvince);
                int highestProvinceCount = hmProvince.get(highestProvince);
                //Log.i("poop", "Highest Prov: " + highestProvinceCount);
                for (Map.Entry<String, Threat_TM> entry : hmThreat.entrySet()) {
                    Threat_TM threatObj = hmThreat.get(entry.getKey());
                    Marker marker = markers.get(entry.getKey());
                    //Log.i("poop", "count: " + hmProvince.get(threatObj.getProvince()) + " " + highestProvinceCount);
                    //if (!threatObj.getProvince().equals(highestProvince)) {

                    if (hmProvince.get(threatObj.getProvince()) != highestProvinceCount) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }

                }
            } else if (item.getItemId() == R.id.cityMenu) {
                String highestMuncipality = getHighestCount(hmMunicipality);
                int highestMunicipalityCount = hmMunicipality.get(highestMuncipality);
                for (Map.Entry<String, Threat_TM> entry : hmThreat.entrySet()) {
                    Threat_TM threaObj = hmThreat.get(entry.getKey());
                    Marker marker = markers.get(entry.getKey());
                    //if (!threaObj.getMunicipality().equals(highestMuncipality)) {
                    //Log.i("poop", "count: " + hmMunicipality.get(threaObj.getMunicipality()) + " " + highestMunicipalityCount);
                    if (hmMunicipality.get(threaObj.getMunicipality()) != highestMunicipalityCount) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            } else if (item.getItemId() == R.id.countryMenu) {
                String highestCountry = getHighestCount(hmCountry);
                for (Map.Entry<String, Threat_TM> entry : hmThreat.entrySet()) {
                    Threat_TM threaObj = hmThreat.get(entry.getKey());
                    Marker marker = markers.get(entry.getKey());
                    if (!threaObj.getCountry().equals(highestCountry)) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            } else if (item.getItemId() == android.R.id.home) {
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            Button button = (Button) findViewById(R.id.emerBtn);
            button.setText("Sending Request...");
            count = 0;
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

                new EmergencyReporter(notifObj).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        int numReq = 7 - count;
        String prompt = "ASK FOR HELP (tap " + numReq + " times)";
        btnEmer.setText(prompt);
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
            new ThreatLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Threat_TM selectedThreat = hmThreat.get(marker.getTitle());
                View v = getLayoutInflater().inflate(R.layout.threat_info_window, null);
                CYM_UTILITY.displayText(v, R.id.threatDescription, selectedThreat.getDescription().isEmpty() ? "Threat Description: unknown" : selectedThreat.getDescription());
                return v;
            }
        });
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

    class ThreatLoader extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;

        public ThreatLoader() {
            progressDialog = new ProgressDialog(ThreatActivity.this);
            progressDialog.setMessage("Loading Threats...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                JSONObject json = JSONParser.getJSONfromURL(THREATS_URL);
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
                    JSONArray jsonArray = json.getJSONArray("allThreats");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        EachThreatLoader task = new EachThreatLoader(jsonArray.getJSONObject(i));
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class EachThreatLoader extends AsyncTask<String, String, String> {

        private JSONObject threatJSON;
        private Threat_TM threatObj;

        public EachThreatLoader(JSONObject threatJSON) {
            this.threatJSON = threatJSON;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                threatObj = Threat_TM.instantiateJSON(threatJSON);
                hmThreat.put(threatObj.getId(), threatObj);
                if (hmProvince.containsKey(threatObj.getProvince())) {
                    int count = hmProvince.get(threatObj.getProvince()) + 1;
                    hmProvince.put(threatObj.getProvince(), count);
                } else  {
                    hmProvince.put(threatObj.getProvince(), 1);
                }

                Log.i("poop", threatObj.getMunicipality());
                if (hmMunicipality.containsKey(threatObj.getMunicipality())) {
                    int count = hmMunicipality.get(threatObj.getMunicipality()) + 1;
                    hmMunicipality.put(threatObj.getMunicipality(), count);
                } else {
                    hmMunicipality.put(threatObj.getMunicipality(), 1);
                }

                if (hmCountry.containsKey(threatObj.getMunicipality())) {
                    int count = hmCountry.get(threatObj.getCountry()) + 1;
                    hmCountry.put(threatObj.getCountry(), count);
                } else {
                    hmCountry.put(threatObj.getCountry(), 1);
                }
                return "true";
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            Log.i("poop", hmProvince.toString());
//            Log.i("poop", hmMunicipality.toString());
            try {
                LatLng latLng = new LatLng(Double.parseDouble(threatObj.getLat()), Double.parseDouble(threatObj.getLng()));
                MarkerOptions options = new MarkerOptions()
                        .title(threatObj.getId())
                        .position(latLng)
                        .snippet(threatObj.getDescription());

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mthreatnotif);
                options.icon(BitmapDescriptorFactory.fromBitmap(CYM_UTILITY.getResizedBitmap(bitmap, 50, 50)));
                Marker marker = mMap.addMarker(options);
                markers.put(threatObj.getId(), marker);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class EmergencyReporter extends AsyncTask<String, String, String> {

        private Notif_TM notifObj;
        private ProgressDialog progressDialog;

        public EmergencyReporter(Notif_TM notifObj) {
            this.notifObj = notifObj;
            progressDialog = new ProgressDialog(ThreatActivity.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Sending Request...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("allNearest", "true"));
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
            CYM_UTILITY.mAlertDialog("Request Sent!", ThreatActivity.this);
        }

    }

    public String getHighestCount(HashMap<String, Integer> hm) {
        int maxValue = Collections.max(hm.values());
        for (Map.Entry<String, Integer> entry : hm.entrySet()) {
            if (entry.getValue() == maxValue) {
                return entry.getKey();
            }
        }
        return null;
    }
}
