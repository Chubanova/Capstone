package moera.ermais.google.com.myplaces.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Task;

import butterknife.BindView;
import butterknife.ButterKnife;
import moera.ermais.google.com.myplaces.OnClickListener;
import moera.ermais.google.com.myplaces.R;
import moera.ermais.google.com.myplaces.database.PlaceLoader;
import moera.ermais.google.com.myplaces.utils.Utils;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, OnClickListener {
    @Nullable
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    @Nullable
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    OnClickListener mCallback;

    private static final String TAG = MapActivity.class.getName();

    private static final int DEFAULT_ZOOM = 13;
    private static final LatLng MOSCOW_LOCATION =
            new LatLng(55.755824805504425, 37.61772628873587);
    private static final int PLACE_PICKER_REQUEST = 1;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private PlaceLoader mPlaceLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (mNavigationView != null)
            mNavigationView.setNavigationItemSelectedListener(
                    menuItem -> {
                        menuItem.setChecked(true);
                        Utils.goMenu(this, menuItem.getItemId());
                        mDrawerLayout.closeDrawers();

                        return true;
                    });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        configureMap();
        mPlaceLoader = new PlaceLoader(this, mMap);
        loadPlaces();
    }

    private void configureMap() {
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        boolean isHasLocationAccess = true;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            isHasLocationAccess = false;
        }
        mMap.setMyLocationEnabled(isHasLocationAccess);

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);

        mMap.setOnMapLongClickListener(latLng -> {
            Log.d(TAG, this.getString(R.string.pressed_place) + latLng);
            try {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                LatLngBounds latLngBounds = new LatLngBounds(latLng, latLng);
                builder = builder.setLatLngBounds(latLngBounds);
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                Log.e(TAG,  e.getMessage());
            }
        });

        mMap.setOnMarkerClickListener(marker -> {
            Log.d(TAG, this.getString(R.string.pressed_marker) + marker.getTitle());
            // Edit or remove marker
            Intent intent = new Intent();
            intent.setClass(this, AddPlaceActivity.class);
            Bundle bundle = new Bundle();
            bundle.putDouble(this.getString(R.string.lat), marker.getPosition().latitude);
            bundle.putDouble(this.getString(R.string.lng), marker.getPosition().longitude);
            intent.putExtras(bundle);
            getApplicationContext().startActivity(intent);
            return true;
        });

        getDeviceLocation();
    }

    /**
     * Sets current position on map if app have location permission
     * or default if haven't
     */
    private void getDeviceLocation() {
        try {
            // Try to get last known device location and show this place
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, task -> {
                boolean locationExists = task.isSuccessful();
                if (locationExists) {
                    // Set the map's camera position to the current location of the device.
                    Location mLastKnownLocation = task.getResult();
                    locationExists = mLastKnownLocation != null;
                    if (locationExists)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                }
                if (!locationExists) {
                    Log.d(TAG, this.getString(R.string.current_location_is_null));
                    Log.e(TAG, String.format(this.getString(R.string.exception), task.getException()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MOSCOW_LOCATION, DEFAULT_ZOOM));
                    // If task was unsuccessful app have no location permission so hide this button
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            });
        } catch (SecurityException | NullPointerException e) {
            Log.e(TAG, String.format(this.getString(R.string.exception), e.getMessage()));
        }
    }

    /**
     * Opens AddPlaceActivity if place selected correctly
     *
     * @param requestCode expected {@link MapActivity#PLACE_PICKER_REQUEST}
     * @param resultCode  expected {@link android.app.Activity#RESULT_OK}
     * @param data        {@link Intent} with the {@link PlacePicker}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            if (place == null) {
                Log.i(TAG, this.getString(R.string.no_place_selected));
                return;
            }

            // Show activity to add place and task
            data.setClass(this, AddPlaceActivity.class);
            getApplicationContext().startActivity(data);
        }
    }

    /**
     * Shows all picked places on map
     */
    private void loadPlaces() {
        if (mMap == null) return;

        mMap.clear();

        getLoaderManager().restartLoader(0, new Bundle(), mPlaceLoader);
    }

    /**
     * Refresh places when come back from {@link AddPlaceActivity}
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadPlaces();
    }

    @Override
    public void onClick(double lat, double lng) {
        Log.d(TAG, this.getString(R.string.pressed_place));
        // Edit or remove marker
        Intent intent = new Intent();
        intent.setClass(this, AddPlaceActivity.class);
        Bundle bundle = new Bundle();
        bundle.putDouble(this.getString(R.string.lat), lat);
        bundle.putDouble(this.getString(R.string.lng), lng);
        intent.putExtras(bundle);
        getApplicationContext().startActivity(intent);
    }
}