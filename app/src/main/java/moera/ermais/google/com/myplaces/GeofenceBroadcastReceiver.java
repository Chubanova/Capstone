package moera.ermais.google.com.myplaces;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.Objects;

import moera.ermais.google.com.myplaces.activity.MapActivity;

import static android.provider.BaseColumns._ID;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_NOTIFICATION;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_DESCR;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_XY_LAT;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_PLACE_XY_LNG;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.COLUMN_REPLY;
import static moera.ermais.google.com.myplaces.database.PlaceContract.PlaceEntry.CONTENT_URI;
import static moera.ermais.google.com.myplaces.utils.Utils.GEO_ACCURACY;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    /***
     * Handles the Broadcast message sent when the Geofence Transition is triggered
     * Careful here though, this is running on the main thread so make sure you start an AsyncTask for
     * anything that takes longer than say 10 second to run
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the Geofence Event from the Intent sent through
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, String.format(context.getString(R.string.error_code), geofencingEvent.getErrorCode()));
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        // Check which transition type has triggered this event
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            setRingerMode(context, AudioManager.RINGER_MODE_SILENT);
        } else {
            // Log the error.
            Log.e(TAG, String.format(context.getString(R.string.unknown_transition), geofenceTransition));
            // No need to do anything else
            return;
        }
        // Send the notification
        sendNotification(context, geofencingEvent);
    }


    /**
     * Posts a notification in the notification bar when a transition is detected
     * Uses different icon drawables for different transition types
     * If the user clicks the notification, control goes to the MapActivity
     *
     * @param context         The calling context for building a task stack
     * @param geofencingEvent The geofence event
     */
    private void sendNotification(Context context, GeofencingEvent geofencingEvent) {
        Location location = geofencingEvent.getTriggeringLocation();

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, MapActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MapActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.notify_capstone));

        // Check the transition type to display the relevant icon image
        if (geofencingEvent.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {
            builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.ic_volume_up_white_24dp))
                    .setContentTitle(context.getString(R.string.notification_header));
        }

        // Find marker that triggers notification
        Cursor cursor = getNearestLocations(context, location);

        // Continue building the notification
        if (cursor != null && cursor.getCount() > 1) {
            builder.setContentText(context.getString(R.string.notification_text_many));
            MyPlaceService.startActionReceiptIngridient(context, cursor.getString(cursor.getColumnIndex(COLUMN_PLACE_DESCR)));
        } else if (cursor != null && cursor.getCount() == 1) {
            cursor.moveToFirst();
            builder.setContentText(cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATION)));
            MyPlaceService.startActionReceiptIngridient(context, cursor.getString(cursor.getColumnIndex(COLUMN_PLACE_DESCR)));
        } else {
            builder.setContentText(context.getString(R.string.touch_to_relaunch));
        }

        ArrayList<String> ids = new ArrayList<>();

        if (cursor != null) {
            cursor.moveToFirst();
            do {
                String replay = cursor.getString(cursor.getColumnIndex(COLUMN_REPLY));
                if ("2".equals(replay)) {
                    ids.add(Integer.toString(cursor.getInt(cursor.getColumnIndex(_ID))));
                }
            } while (cursor.moveToNext());
            cursor.close();
            if (!ids.isEmpty()) {
                removePlaces(context, ids);
            }

        }

        builder.setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(context.getString(R.string.notify_capstone),
                    context.getString(R.string.channel),
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    private void removePlaces(Context context, ArrayList<String> stringId) {
        Uri uri = CONTENT_URI;
        for (String s : stringId) {
            uri = uri.buildUpon().appendPath(s).build();

            int res = context.getContentResolver().delete(uri, null, null);
            if (res != 0) {
                Log.d(TAG, context.getString(R.string.removed_place) + stringId);
            }
        }

    }

    /**
     * Changes the ringer mode on the device to either silent or back to normal
     *
     * @param context The context to access AUDIO_SERVICE
     * @param mode    The desired mode to switch device to, can be AudioManager.RINGER_MODE_SILENT or
     *                AudioManager.RINGER_MODE_NORMAL
     */
    private void setRingerMode(Context context, int mode) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Check for DND permissions for API 24+
        if (android.os.Build.VERSION.SDK_INT < 24 ||
                (android.os.Build.VERSION.SDK_INT >= 24 && !nm.isNotificationPolicyAccessGranted())) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(mode);
        }
    }

    /**
     * Find markers in DB near current location
     *
     * @param context  application {@link Context}
     * @param location current {@link Location}
     * @return {@link Cursor} with all nearest marks
     */
    private Cursor getNearestLocations(Context context, Location location) {
        // Selection range
        String selection = COLUMN_PLACE_XY_LAT + " >= ? AND " + COLUMN_PLACE_XY_LAT + " <= ? " +
                " AND " + COLUMN_PLACE_XY_LNG + " >= ? AND " + COLUMN_PLACE_XY_LNG + " <= ? ";

        Double minLat = location.getLatitude() - GEO_ACCURACY;
        Double maxLat = location.getLatitude() + GEO_ACCURACY;
        Double minLng = location.getLongitude() - GEO_ACCURACY;
        Double maxLng = location.getLongitude() + GEO_ACCURACY;

        String[] selectionArgs = new String[]{String.valueOf(minLat), String.valueOf(maxLat),
                String.valueOf(minLng), String.valueOf(maxLng)};
        return Objects.requireNonNull(context).getContentResolver()
                .query(CONTENT_URI, null, selection, selectionArgs, null);
    }

}
