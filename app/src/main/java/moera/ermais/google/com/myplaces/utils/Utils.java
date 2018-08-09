package moera.ermais.google.com.myplaces.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import moera.ermais.google.com.myplaces.R;
import moera.ermais.google.com.myplaces.activity.AllPlacesActivity;
import moera.ermais.google.com.myplaces.activity.MapActivity;
import moera.ermais.google.com.myplaces.activity.SettingsActivity;

public class Utils {

    public static final float GEO_ACCURACY = (float) 0.0015; // 150 meters

    private Utils() { /* make it static */}

    /**
     * Cleanup all fragments from {@link FragmentManager}
     *
     * @param fragmentManager - activity {@link FragmentManager}
     */
    public static void cleanupFragments(FragmentManager fragmentManager) {
        if (fragmentManager.getFragments().size() > 0) {
            for (Fragment fragment : fragmentManager.getFragments()) {
                fragmentManager.beginTransaction().remove(fragment).commit();
            }
        }
    }

    public static void goMenu(Context context, int viewId) {
        Bundle b = new Bundle();

        Intent intent;

        switch (viewId) {
            case R.id.map:
                intent = new Intent(context, MapActivity.class);
                break;
            case R.id.settings:
                intent = new Intent(context, SettingsActivity.class);
                break;
            case R.id.places:
                intent = new Intent(context, AllPlacesActivity.class);
                break;

            default:
                intent = new Intent(context, MapActivity.class);
                break;
        }

        intent.putExtras(b);
        context.startActivity(intent);
    }
}
