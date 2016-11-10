package com.projects.codeyasam.threatmap;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener {

    public static final String USERS_LOADER_URL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/loadOnlineClients.php?onlineClients=true";
    public static final String SESSION_USER_UPDATER = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/SessionUserUpdater.php";

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private double mLat;
    private double mLng;
    private SharedPreferences settings;

    public static List<Client_TM> onlineClientList;
    public HashMap<String, Client_TM> hmOnlineClients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
        onlineClientList = new ArrayList<>();
        hmOnlineClients = new HashMap<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timer myTimer = new Timer();
        myTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //Log.i("poop", "loading users");
                if (mLastLocation != null) {
                    new SessionUserUpdater().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }, 0, 10000);

//        if (mLastLocation != null) {
//            new OnlineClientsLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Session_TM.LOGGED_USER_ID, "");
            editor.commit();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
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

    class SessionUserUpdater extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                if (mLastLocation != null) {
                    String user_id = settings.getString(Session_TM.LOGGED_USER_ID, "");
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("updateSessionTable", "true"));
                    params.add(new BasicNameValuePair("user_id", user_id));
                    params.add(new BasicNameValuePair("lat", String.valueOf(mLastLocation.getLatitude())));
                    params.add(new BasicNameValuePair("lng", String.valueOf(mLastLocation.getLongitude())));
                    JSONObject json = JSONParser.makeHttpRequest(SESSION_USER_UPDATER, "POST", params);
                    return json.toString();
                }
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
                    //Log.i("poop", "asdf");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class OnlineClientsLoader extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onlineClientList = new ArrayList<>();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                final JSONObject json = JSONParser.getJSONfromURL(USERS_LOADER_URL);
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
                    JSONArray jsonArray = json.getJSONArray("OnlineClients");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        EachClientLoader task = new EachClientLoader(jsonArray.getJSONObject(i));
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class EachClientLoader extends AsyncTask<String, String, String> {

        private JSONObject eachClient;
        private Client_TM foundClient;

        public EachClientLoader(JSONObject eachClient) {
            this.eachClient = eachClient;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.i("poop", "onbakcground");
                foundClient = Client_TM.instantiateJSON(eachClient);
                Log.i("poop", foundClient.getId());
                hmOnlineClients.put(foundClient.getId(), foundClient);
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
                    Log.i("poop", "onPOst level");
                    LatLng latLng = new LatLng(Double.parseDouble(foundClient.getLat()), Double.parseDouble(foundClient.getLng()));
                    MarkerOptions options = new MarkerOptions()
                            .title(foundClient.getId())
                            .position(latLng)
                            .snippet(foundClient.getFullName());


                    options.icon(BitmapDescriptorFactory.fromBitmap(CYM_UTILITY.getRoundedCornerBitmap(CYM_UTILITY.getResizedBitmap(foundClient.getDisplayPicture(), 50, 50))));
                    mMap.addMarker(options);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class EmergencyReporter extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

    }

    public void reportEmer(View v) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            new OnlineClientsLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Client_TM selectedClient = hmOnlineClients.get(marker.getTitle());
                View v = getLayoutInflater().inflate(R.layout.custom_info_window, null);
                Log.i("poop", selectedClient.getFullName());
                CYM_UTILITY.displayText(v, R.id.fullName, selectedClient.getFullName());
                CYM_UTILITY.displayText(v, R.id.contactNo, selectedClient.getContactNo());
                return v;
            }
        });
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
    public boolean onMyLocationButtonClick() {
        if (mLastLocation != null) {
            mLat = mLastLocation.getLatitude();
            mLng = mLastLocation.getLongitude();
            LatLng latLng = new LatLng(mLat, mLng);
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }
}
