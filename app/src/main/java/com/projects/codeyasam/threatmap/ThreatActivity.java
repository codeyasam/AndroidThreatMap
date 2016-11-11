package com.projects.codeyasam.threatmap;

import android.*;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreatActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener {

    public static final String THREATS_URL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/getThreats.php?allThreats=true";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threat);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

        hmThreat = new HashMap<>();
        hmMunicipality = new HashMap<>();
        hmProvince = new HashMap<>();
        hmCountry = new HashMap<>();
        markers = new HashMap<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.threat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.provinceMenu) {
            String highestProvince = getHighestCount(hmProvince);
            //Log.i("poop", "Highest Prov: " + highestProvince);
            for (Map.Entry<String, Threat_TM> entry : hmThreat.entrySet()) {
                Threat_TM threatObj = hmThreat.get(entry.getKey());
                Marker marker = markers.get(entry.getKey());
                if (!threatObj.getProvince().equals(highestProvince)) {
                    marker.setVisible(false);
                } else {
                    marker.setVisible(true);
                }

            }
        } else if (item.getItemId() == R.id.cityMenu) {
            String highestMuncipality = getHighestCount(hmMunicipality);
            for (Map.Entry<String, Threat_TM> entry : hmThreat.entrySet()) {
                Threat_TM threaObj = hmThreat.get(entry.getKey());
                Marker marker = markers.get(entry.getKey());
                if (!threaObj.getMunicipality().equals(highestMuncipality)) {
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
            new ThreatLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
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

                Marker marker = mMap.addMarker(options);
                markers.put(threatObj.getId(), marker);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
