package com.projects.codeyasam.threatmap;

import android.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainAdminActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener {

    public static final String REQUEST_LOADER_URL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/getUserRequests.php?getNeeded=true";

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private SharedPreferences settings;
    private HashMap<String, Client_TM> hmClientsInDanger;
    private HashMap<String, Notif_TM> hmNotifs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);
        settings = PreferenceManager.getDefaultSharedPreferences(MainAdminActivity.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
        hmClientsInDanger = new HashMap<>();
        hmNotifs = new HashMap<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new RequestsLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.incharge_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Session_TM.LOGGED_USER_ID, "");
            editor.commit();
            finish();
        } else if (item.getItemId() == R.id.editProfile) {
            
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
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                Notif_TM notifObj = hmNotifs.get(marker.getTitle());
                for (Map.Entry<String, Client_TM> entry : hmClientsInDanger.entrySet()) {
                    Client_TM clientObj = entry.getValue();
                    if (notifObj.getClient_id().equals(clientObj.getId())) {
                        clientObj.requestId = notifObj.getId();
                        Client_TM.clientTm = clientObj;
                        break;
                    }
                }
                mMap.clear();
                Intent intent = new Intent(MainAdminActivity.this, ViewProfile.class);
                startActivity(intent);
                return true;
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
        return false;
    }

    class RequestsLoader extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String userId = settings.getString(Session_TM.LOGGED_USER_ID, "");
                JSONObject json = JSONParser.getJSONfromURL(REQUEST_LOADER_URL + "&user_id=" + userId);
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
                    JSONArray jsonArray = json.getJSONArray("requests");
                    JSONArray notifsArray = json.getJSONArray("notifs");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        EachRequestLoader task = new EachRequestLoader(jsonArray.getJSONObject(i), notifsArray.getJSONObject(i));
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class EachRequestLoader extends AsyncTask<String, String, String> {

        private JSONObject requestJSON;
        private JSONObject notifsJSON;
        private Client_TM clientObj;
        private Notif_TM notifObj;

        public EachRequestLoader(JSONObject json, JSONObject notifsJSON) {
            this.requestJSON = json;
            this.notifsJSON = notifsJSON;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                clientObj = Client_TM.instantiateJSON(requestJSON);
                notifObj = Notif_TM.instantiateJSON(notifsJSON);
                hmClientsInDanger.put(clientObj.getId(), clientObj);
                hmNotifs.put(notifObj.getId(), notifObj);
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
                    LatLng latLng = new LatLng(Double.parseDouble(clientObj.getLat()), Double.parseDouble(clientObj.getLng()));
                    MarkerOptions options = new MarkerOptions()
                            .title(notifObj.getId())
                            .position(latLng);
                    //options.icon(BitmapDescriptorFactory.fromBitmap(CYM_UTILITY.getRoundedCornerBitmap(CYM_UTILITY.getResizedBitmap(clientObj.getDisplayPicture(), 50, 50))));
                    Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.mnofitlogg);
                    options.icon(BitmapDescriptorFactory.fromBitmap(CYM_UTILITY.getResizedBitmap(bitmap, 50, 50)));
                    mMap.addMarker(options);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
