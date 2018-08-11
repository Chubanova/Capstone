package moera.ermais.google.com.myplaces.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import moera.ermais.google.com.myplaces.R;
import moera.ermais.google.com.myplaces.service.GeoService;

import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_NOTIFICATION;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_DESCR;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_ID;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_NAME;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_XY_LAT;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_XY_LNG;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_REPLY;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.CONTENT_URI;

public class PlaceFragment extends Fragment {
    public static final String TAG = PlaceFragment.class.getSimpleName();
    private int mReply;
    View rootView;

    @BindView(R.id.addButton)
    Button mSave;

    @BindView(R.id.removeButton)
    Button mRemoveButton;

    @BindView(R.id.placeTV)
    TextView mPlaceTV;

    @BindView(R.id.editTextNotify)
    EditText mEditTextNotify;

    // Place data
    private String placeName;
    private String placeAddress;
    private String placeNameVal = "";
    private String placeId;
    private double lat;
    private double lng;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_place, container, false);
        ButterKnife.bind(this, rootView);

        Intent data = Objects.requireNonNull(getActivity()).getIntent();

        Place place = PlacePicker.getPlace(Objects.requireNonNull(getContext()), data);

        if (place == null) {
            configureExistsMarker(data);
        } else {
            configureNewMarker(place);
        }

        // Set up onClick listeners for radio buttons
        for (int id : new int[]{R.id.radButton1, R.id.radButton2}) {
            rootView.findViewById(id).setOnClickListener(this::onPrioritySelected);
        }

        mPlaceTV.setText(placeNameVal);

        return rootView;
    }

    /**
     * onClickAddPlace is called when the "SAVE" button is clicked.
     * It retrieves user input and inserts that new task data into the underlying database.
     */
    public void onClickAddPlace(View view) {
        // Check if EditText is empty, if not retrieve input and store it in a ContentValues object
        // If the EditText input is empty -> don't create an entry
        String notify = mEditTextNotify.getText().toString();
        if (notify.length() == 0) {
            Toast.makeText(getContext(), getContext().getString(R.string.toast), Toast.LENGTH_LONG).show();
            return;
        }
        if (placeId == null) {
            Log.i(TAG, getContext().getString(R.string.no_place_selected));
            return;
        }

        // Insert new task data via a ContentResolver
        // Create new empty ContentValues object
        ContentValues contentValues = new ContentValues();
        // Put the task description and selected mPriority into the ContentValues
        contentValues.put(COLUMN_PLACE_ID, placeId);
        contentValues.put(COLUMN_NOTIFICATION, notify);
        contentValues.put(COLUMN_REPLY, mReply);
        contentValues.put(COLUMN_PLACE_NAME, placeName);
        contentValues.put(COLUMN_PLACE_DESCR, placeAddress);
        contentValues.put(COLUMN_PLACE_XY_LAT, lat);
        contentValues.put(COLUMN_PLACE_XY_LNG, lng);
        // Insert the content values via a ContentResolver
        Uri uri = Objects.requireNonNull(getContext()).getContentResolver()
                .insert(CONTENT_URI, contentValues);

        if (uri != null) {
            Log.d(TAG, getContext().getString(R.string.saved_place) + uri.toString());
        }

        restartService();

        // Finish activity (this returns back to MapActivity)
        Objects.requireNonNull(getActivity()).finish();
    }

    /**
     * onClickUpdPlace is called when the "UPDATE" button is clicked.
     * It retrieves user input and updates marker data in DB.
     */
    public void onClickUpdPlace(View view) {
        // Check if EditText is empty, if not retrieve input and store it in a ContentValues object
        // If the EditText input is empty -> don't create an entry
        String notify = mEditTextNotify.getText().toString();
        if (notify.length() == 0) {
            Toast.makeText(getContext(), getContext().getString(R.string.toast), Toast.LENGTH_LONG).show();
            return;
        }
        if (placeId == null) {
            Log.i(TAG, getContext().getString(R.string.no_place_selected));
            return;
        }

        // Update task data via a ContentResolver
        // Create new empty ContentValues object
        ContentValues contentValues = new ContentValues();
        // Put the task description and selected mPriority into the ContentValues
        contentValues.put(COLUMN_PLACE_ID, placeId);
        contentValues.put(COLUMN_NOTIFICATION, notify);
        contentValues.put(COLUMN_REPLY, mReply);
        contentValues.put(COLUMN_PLACE_NAME, placeName);
        contentValues.put(COLUMN_PLACE_DESCR, placeAddress);
        contentValues.put(COLUMN_PLACE_XY_LAT, lat);
        contentValues.put(COLUMN_PLACE_XY_LNG, lng);
        // Insert the content values via a ContentResolver
        int uri = Objects.requireNonNull(getContext()).getContentResolver()
                .update(CONTENT_URI, contentValues, COLUMN_PLACE_ID + " = ? ", new String[]{placeId});

        if (uri != 0) {
            Log.d(TAG, "Updated place: " + placeId + " " + notify);
        }

        // Finish activity (this returns back to MapActivity)
        Objects.requireNonNull(getActivity()).finish();
    }

    /**
     * onClickRemPlace is called when the "REMOVE" button is clicked.
     * It retrieves user input and removes marker data from DB
     */
    public void onClickRemPlace(View view) {
        if (placeId == null) {
            Log.i(TAG, getContext().getString(R.string.no_place_selected));
            return;
        }

        // Delete the content values via a ContentResolver
        int res = Objects.requireNonNull(getContext()).getContentResolver()
                .delete(CONTENT_URI, COLUMN_PLACE_ID + " = ? ", new String[]{placeId});

        if (res != 0) {
            Log.d(TAG, getContext().getString(R.string.removed_place) + placeId + " " + placeName);
        }

        restartService();

        // Finish activity (this returns back to MapActivity)
        Objects.requireNonNull(getActivity()).finish();
    }

    /**
     * onPrioritySelected is called whenever a priority button is clicked.
     * It changes the value of mPriority based on the selected button.
     */
    public void onPrioritySelected(View view) {
        if (((RadioButton) rootView.findViewById(R.id.radButton1)).isChecked()) {
            mReply = 1;
        } else if (((RadioButton) rootView.findViewById(R.id.radButton2)).isChecked()) {
            mReply = 2;
        }
    }

    /**
     * Set controls and variables for new marker
     *
     * @param place selected {@link Place}
     */
    private void configureNewMarker(Place place) {
        // Set up place data
        placeName = place.getName().toString();
        placeAddress = String.valueOf(place.getAddress());
        placeNameVal = String.format("%s / %s", placeName, placeAddress);
        placeId = place.getId();
        lat = place.getLatLng().latitude;
        lng = place.getLatLng().longitude;

        // Initialize mReply by default (always)
        ((RadioButton) rootView.findViewById(R.id.radButton1)).setChecked(true);
        mReply = 1;

        // Configure listeners
        mSave.setOnClickListener(this::onClickAddPlace);
    }

    /**
     * Set controls and variables for exists marker
     *
     * @param data {@link Intent} with doubles "lat" & "lng" for selected marker
     */
    private void configureExistsMarker(Intent data) {
        // Get params
        double _lat = data.getDoubleExtra(getContext().getString(R.string.lat), -1);
        double _lng = data.getDoubleExtra(getContext().getString(R.string.lng), -1);

        // This is impossible but anyway
        if (_lat == -1 || _lng == -1)
            // Finish activity (this returns back to MapActivity)
            Objects.requireNonNull(getActivity()).finish();

        // Find marker in DB
        String selection = COLUMN_PLACE_XY_LAT + " = ? " +
                " AND " + COLUMN_PLACE_XY_LNG + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(_lat), String.valueOf(_lng)};
        Cursor cursor = Objects.requireNonNull(getContext()).getContentResolver()
                .query(CONTENT_URI, null, selection, selectionArgs, null);

        if (cursor != null && cursor.getCount() > 0) {
            // Get first row
            cursor.moveToFirst();

            // Collect place data
            placeName = cursor.getString(cursor.getColumnIndex(COLUMN_PLACE_NAME));
            placeAddress = cursor.getString(cursor.getColumnIndex(COLUMN_PLACE_DESCR));
            placeNameVal = String.format("%s / %s", placeName, placeAddress);
            placeId = cursor.getString(cursor.getColumnIndex(COLUMN_PLACE_ID));
            lat = cursor.getDouble(cursor.getColumnIndex(COLUMN_PLACE_XY_LAT));
            lng = cursor.getDouble(cursor.getColumnIndex(COLUMN_PLACE_XY_LNG));

            // Setup controls
            ((EditText) rootView.findViewById(R.id.editTextNotify)).setText(
                    cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATION)));

            switch (cursor.getInt(cursor.getColumnIndex(COLUMN_REPLY))) {
                case 2:
                    ((RadioButton) rootView.findViewById(R.id.radButton2)).setChecked(true);
                    mReply = 2;
                    break;
                case 1:
                default:
                    ((RadioButton) rootView.findViewById(R.id.radButton1)).setChecked(true);
                    mReply = 1;
                    break;
            }

            // Close cursor
            cursor.close();

            // Setup buttons
            mSave.setText(R.string.update_button);
            mSave.setOnClickListener(this::onClickUpdPlace);
            mRemoveButton.setVisibility(View.VISIBLE);
            mRemoveButton.setOnClickListener(this::onClickRemPlace);
        }
    }

    private void restartService() {
        Intent service = new Intent(Objects.requireNonNull(getActivity()).getApplicationContext(), GeoService.class);
        getActivity().stopService(service);
        getActivity().startService(service);
    }
}
