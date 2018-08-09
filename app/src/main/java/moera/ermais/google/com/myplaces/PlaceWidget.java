package moera.ermais.google.com.myplaces;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import moera.ermais.google.com.myplaces.activity.MapActivity;


/**
 * Implementation of App Widget functionality.
 */
public class PlaceWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String myPlace) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setComponent(new ComponentName(context.getPackageName(), MapActivity.class.getName()));

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 0);

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        if (myPlace != null) {
            widgetText = myPlace;
        }

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.place_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Perform any action when one or more AppWidget instances have been deleted
    }

    public static void updateIngridient(Context context, AppWidgetManager appWidgetProvider, int[] appWidgetIds, String myPlace) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetProvider, appWidgetId, myPlace);
        }
    }
}

