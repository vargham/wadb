package hu.varghamarkpeter.wadb;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 *
 * <p/>
 * @author <a href="mailto:vmp@varghamarkpeter.hu">Mark Peter Vargha</a>
 */
public class WadbWidgetProvider extends AppWidgetProvider {

    public static final String WIDGET_CLICK = "hu.varghamarkpeter.wadb.WIDGET_CLICK";
    public static final String WIDGET_UPDATE_ALL = "hu.varghamarkpeter.wadb.WIDGET_UPDATE_ALL";

    static void displayState(Context context, String message) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        remoteViews.setTextViewText(R.id.widget_textViewIp, message);
        ComponentName thisWidget = new ComponentName(context, WadbWidgetProvider.class);
        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, remoteViews);
    }

    static void refreshActualState(Context context) {
        displayState(context, context.getString(R.string.widget_working));
        context.startService(new Intent(context, WidgetUpdateService.class));
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context, WadbWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            Intent intent = new Intent(WIDGET_CLICK);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_textViewIp, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        refreshActualState(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(WIDGET_CLICK)) {
            displayState(context, context.getString(R.string.widget_working));
            WirelessAdb.getInstance().flipWirelessAdbState(null);
            refreshActualState(context);
        }
        if (intent.getAction().equals(WIDGET_UPDATE_ALL)) {
            refreshActualState(context);
        }
    }

}
