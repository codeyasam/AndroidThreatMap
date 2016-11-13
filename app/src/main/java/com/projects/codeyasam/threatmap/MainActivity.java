package com.projects.codeyasam.threatmap;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnInfoWindowClickListener {

    public static final String USERS_LOADER_URL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/loadOnlineClients.php?onlineClients=true";
    public static final String SESSION_USER_UPDATER = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/SessionUserUpdater.php";
    public static final String REPORT_EMERGENCY_ALL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/askForHelp.php";
    public static final String DELETE_ACCOUNT_URL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/deleteAccount.php";
    public static final String LOGOUT_ACCOUNT_URL = CYM_UTILITY.THREAT_MAP_ROOT_URL + "android/androidLogout.php";

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private double mLat;
    private double mLng;
    private SharedPreferences settings;

    public static List<Client_TM> onlineClientList;
    public HashMap<String, Client_TM> hmOnlineClients;
    private Button btnEmer;

    private Notif_TM notifObj;

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
    protected void onResume() {
        super.onResume();
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setMessage("Loading Online Users...");
        progressDialog.show();
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
            new UserLogout().execute();

//            SharedPreferences.Editor editor = settings.edit();
//            editor.putString(Session_TM.LOGGED_USER_ID, "");
//            editor.commit();
//            finish();
        } else if (item.getItemId() == R.id.threatMenu) {
            Intent intent = new Intent(getApplicationContext(), ThreatActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.officeMenu) {
            Intent intent = new Intent(getApplicationContext(), OfficeActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.editProfile) {
            Intent intent = new Intent(MainActivity.this, EditProfile.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.deleteAccoutn) {
            CYM_UTILITY.callYesNoMessage("Are you sure you want to delete your account?", MainActivity.this, deleteAccount());
        } else if (item.getItemId() == R.id.changePassword) {
            Intent intent = new Intent(MainActivity.this, ChangePassword.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public DialogInterface.OnClickListener deleteAccount() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AccountPurger().execute();
            }
        };
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    public boolean runOnce = false;

    @Override
    public void onInfoWindowClick(Marker marker) {
        Client_TM.clientTm = hmOnlineClients.get(marker.getTitle());
        Intent intent = new Intent(MainActivity.this, ViewProfile.class);
        startActivity(intent);
        mMap.clear();
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
                    if (!runOnce) {
                        new OnlineClientsLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        runOnce = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private ProgressDialog progressDialog;

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
            progressDialog.dismiss();
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

        private Notif_TM notifObj;
        private ProgressDialog progressDialog;

        public EmergencyReporter(Notif_TM notifObj) {
            this.notifObj = notifObj;
            progressDialog = new ProgressDialog(MainActivity.this);
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
            CYM_UTILITY.mAlertDialog("Request Sent!", MainActivity.this);
        }

    }

    private int count = 0;
    private long startMillis = 0;
    public void reportEmer(View v) {
        long time = System.currentTimeMillis();
        startMillis = time;
        count++;
        if (count >= 7) {
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
            btnEmer.setEnabled(true);
            //new OnlineClientsLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        mMap.setOnInfoWindowClickListener(this);
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

    class AccountPurger extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                String userId = settings.getString(Session_TM.LOGGED_USER_ID, "");
                String userType = settings.getString(Session_TM.LOGGED_USER_TYPE, "");
                Log.i("poop", "user type: " + userType + "user id: " + userId);
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("user_id", userId));
                params.add(new BasicNameValuePair("user_type", userType));
                params.add(new BasicNameValuePair("submit", "true"));
                JSONObject json = JSONParser.makeHttpRequest(DELETE_ACCOUNT_URL, "POST", params);
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
                    Log.i("poop", result);
                    JSONObject json = new JSONObject(result);
                    if (json.getString("success").equals("true")) {
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Session_TM.LOGGED_USER_ID, "");
                        editor.putString(Session_TM.LOGGED_USER_TYPE, "");
                        editor.commit();
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class UserLogout extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                String userId = settings.getString(Session_TM.LOGGED_USER_ID, "");
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("user_id", userId));
                params.add(new BasicNameValuePair("logout", "true"));
                JSONObject json = JSONParser.makeHttpRequest(LOGOUT_ACCOUNT_URL, "POST", params);
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
                    Log.i("poop", result);
                    JSONObject json = new JSONObject(result);
                    if (json.getString("success").equals("true")) {
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Session_TM.LOGGED_USER_ID, "");
                        editor.commit();
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
