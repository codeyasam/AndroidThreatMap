package com.projects.codeyasam.threatmap;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SetLocation extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker marker;
    private double mLat;
    private double mLng;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("poop", "Place: " + place.getName());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 10);
                mMap.moveCamera(cameraUpdate);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("poop", "An error occurred: " + status);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = PreferenceManager.getDefaultSharedPreferences(SetLocation.this);
        String clientId = settings.getString(Session_TM.LOGGED_USER_ID, "");
        if (!clientId.isEmpty()) {
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
    public void onMapClick(LatLng latLng) {
        mLat = latLng.latitude;
        mLng = latLng.longitude;
        setMapMarkerOnce(latLng);
    }

    private void setMapMarkerOnce(LatLng latLng) {
        if (marker != null) {
            marker.setPosition(latLng);
        } else {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            marker = mMap.addMarker(markerOptions);
        }
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        Log.i("poop", "Connection Failed");
        //buildGoogleApiClient();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (mLastLocation != null) {
            mLat = mLastLocation.getLatitude();
            mLng = mLastLocation.getLongitude();
            LatLng latLng = new LatLng(mLat, mLng);
            setMapMarkerOnce(latLng);
        }
        return false;
    }

    public void nextBtnClick(View v) {
        if (marker == null) {
            CYM_UTILITY.mAlertDialog("Tap the map or click the current location button to set your address/location", SetLocation.this);
            return;
        }
        AddressLocationGetter addressLocationGetter = new AddressLocationGetter(mLat, mLng);
        addressLocationGetter.execute();
    }

    public static String geoCodingMakeUrl(String lat, String lng) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/geocode/json");
        urlString.append("?latlng=");
        urlString.append(lat);
        urlString.append(",");
        urlString.append(lng);
        urlString.append("&key=AIzaSyDDpPDWu9z820FMYyOVsAphuy0ryz4kt2o");
        return urlString.toString();
    }

    class AddressLocationGetter extends AsyncTask<String, String, String> {

        private double lat;
        private double lng;
        private String url;
        private ProgressDialog progressDialog;

        public AddressLocationGetter(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
            this.url = geoCodingMakeUrl(String.valueOf(lat), String.valueOf(lng));
            RegisterActivity.clientObj.setLat(String.valueOf(lat));
            RegisterActivity.clientObj.setLng(String.valueOf(lng));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SetLocation.this);
            progressDialog.setMessage("Fetching your address, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                JSONObject json = JSONParser.getJSONfromURL(url);
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
                    final JSONObject json = new JSONObject(result);
                    JSONObject firstIndexResult = json.getJSONArray("results").getJSONObject(0);
                    RegisterActivity.clientObj.setAddress(firstIndexResult.getString("formatted_address"));
                    //Log.i("poop", RegisterActivity.clientObj.getAddress());
                    Intent intent = new Intent(getApplicationContext(), SetLoginDetails.class);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }
    }
}
