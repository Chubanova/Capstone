package moera.ermais.google.com.myplaces.database;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import moera.ermais.google.com.myplaces.entity.MyPlace;

import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_NOTIFICATION;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_ID;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_XY_LAT;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_XY_LNG;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.CONTENT_URI;

public class PlaceLoader implements LoaderManager.LoaderCallbacks<List<MyPlace>> {

    private static final String TAG = PlaceLoader.class.getCanonicalName();

    private boolean mIsEnabled;

    private final GoogleMap mMap;

    private final Context mContext;

    public PlaceLoader(Context mContext, GoogleMap mMap) {
        this.mContext = mContext;
        this.mMap = mMap;
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<List<MyPlace>> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<List<MyPlace>>(mContext) {

            List<MyPlace> mMyPlaceData = null;

            @Override
            protected void onStartLoading() {

                if (mMyPlaceData != null && mMyPlaceData.size() > 0) {
                    deliverResult(mMyPlaceData);
                } else {
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public List<MyPlace> loadInBackground() {

                List<MyPlace> myPlaces = new ArrayList<>();

                try {
                    Cursor cursor = mContext.getContentResolver().
                            query(CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    null);
                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            String id = cursor.getString(cursor.getColumnIndex(COLUMN_PLACE_ID));
                            String title = cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATION));
                            double lat = cursor.getDouble(cursor.getColumnIndex(COLUMN_PLACE_XY_LAT));
                            double lng = cursor.getDouble(cursor.getColumnIndex(COLUMN_PLACE_XY_LNG));

                            myPlaces.add(new MyPlace(id, title, lat, lng));
                        } while (cursor.moveToNext());
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

                return myPlaces;

            }

            public void deliverResult(List<MyPlace> data) {
                mMyPlaceData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<MyPlace>> loader, List<MyPlace> data) {
        if (data != null) {
            if (mMap != null) {
                data.forEach(myPlace -> {
                    mMap.addMarker(
                            new MarkerOptions()
                                    .position(new LatLng(myPlace.getLat(), myPlace.getLng()))
                                    .title(myPlace.getTitle()));
                });
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        Log.e(TAG, "The loader has been reset.");
    }
}
