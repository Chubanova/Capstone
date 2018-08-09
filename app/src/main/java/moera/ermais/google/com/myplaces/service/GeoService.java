package moera.ermais.google.com.myplaces.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import moera.ermais.google.com.myplaces.Geofencing;
import moera.ermais.google.com.myplaces.R;
import moera.ermais.google.com.myplaces.utils.GoogleClientCallBackHandler;

import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_ID;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.CONTENT_URI;

public class GeoService extends Service {

    private final static String TAG = GeoService.class.getName();

    private final static long INTERVAL = 300;
    private GoogleApiClient mClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed.");
        if (mClient != null)
            mClient.disconnect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bind.");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting service...");
        runGeoMonitoring();
        return super.onStartCommand(intent, flags, startId);
    }

    void runGeoMonitoring() {
        new Thread(() -> {
            Looper.prepare();

            // Build up the LocationServices API client
            // Uses the addApi method to request the LocationServices API
            // Also uses enableAutoManage to automatically when to connect/suspend the client
            GoogleClientCallBackHandler handler = new GoogleClientCallBackHandler();

            mClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(handler)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addOnConnectionFailedListener(handler)
                    .enableAutoManage(new FragmentActivity(), 0, handler)
                    .build();
            mClient.connect();

            Geofencing mGeofencing = new Geofencing(getApplicationContext(), mClient);

            Log.d(TAG, "Loading...");

            List<String> ids = new ArrayList<>();

            try {
                Cursor cursor = getApplicationContext().getContentResolver().
                        query(CONTENT_URI,
                                null,
                                null,
                                null,
                                null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        String id = cursor.getString(cursor.getColumnIndex(COLUMN_PLACE_ID));
                        ids.add(id);
                    } while (cursor.moveToNext());
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }



            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult); // why? this. is. retarded. Android.
                    Location currentLocation = locationResult.getLastLocation();
                }
            };

            while (true) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    continue;
                }

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient,
                        ids.toArray(new String[ids.size()]));

                boolean mIsEnabled = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getBoolean(getApplicationContext().getString(R.string.setting_enabled), false);

                placeResult.setResultCallback(places -> {
                    mGeofencing.updateGeofencesList(places);
                    if (mIsEnabled) mGeofencing.registerAllGeofences();
                });

                LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest,
                        locationCallback, Looper.myLooper());

                Log.d(TAG, "Sleep...");
                try {
                    TimeUnit.SECONDS.sleep(INTERVAL);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error: " + e.getMessage());
                }

                Log.d(TAG, "Next step...");
            }
        }).start();
    }
}
