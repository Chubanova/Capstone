package moera.ermais.google.com.myplaces;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

public class MyPlaceService extends IntentService {
    public static final String ACTION_PLACES =
            "moera.ermais.google.com.myplaces";

    public MyPlaceService(String name) {
        super(name);
    }

    public MyPlaceService() {
        super(ACTION_PLACES);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PLACES.equals(action)) {
                final String myPlace = intent.getStringExtra(String.valueOf(R.string.place));
                handlePlaceDescr(myPlace);
            }
        }
    }

    public static void startActionReceiptIngridient(Context context, String myPlace) {
        Intent intent = new Intent(context, MyPlaceService.class);
        intent.setAction(ACTION_PLACES);
        intent.putExtra(String.valueOf(R.string.place), myPlace);

        context.startService(intent);
    }

    private void handlePlaceDescr(String myPlace) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlaceWidget.class));
        //Trigger data update to handle the GridView widgets and force a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.appwidget_text);
        PlaceWidget.updateIngridient(this, appWidgetManager, appWidgetIds, myPlace);

    }
}
