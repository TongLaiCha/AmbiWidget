package brandonmilan.tonglaicha.ambiwidget.utils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.Gson;

import brandonmilan.tonglaicha.ambiwidget.R;
import brandonmilan.tonglaicha.ambiwidget.WidgetProvider;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.services.WidgetService;

public final class WidgetUtils {
    private static final String TAG = "WidgetUtils";

    /**
     * Helper function for creating a give feedback pendingIntent.
     * The broadcast pendingIntent is send to the {@link WidgetProvider onReceive} method.
     *
     * @return PendingIntent
     */
    public static PendingIntent getGiveFeedbackPendingIntent(Context context, int appWidgetId, String FeedbackTag) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(WidgetService.ACTION_GIVE_FEEDBACK);
        //Give the pendingIntent a category
        //If pendingIntents only vary by their "extra" contents, they will be seen as the same and get overwritten.
        intent.addCategory(FeedbackTag);
        intent.putExtra(WidgetService.EXTRA_FEEDBACK_TAG, FeedbackTag);
        intent.putExtra(WidgetService.EXTRA_WIDGET_ID, appWidgetId);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Helper function for creating a switch power pendingIntent.
     * The broadcast pendingIntent is send to the {@link WidgetProvider onReceive} method.
     *
     * @return PendingIntent
     */
    public static PendingIntent getSwitchPowerPendingIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(WidgetService.ACTION_SWITCH_ON_OFF);
        intent.putExtra(WidgetService.EXTRA_WIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Helper function for creating an update pendingIntent.
     * The broadcast pendingIntent is send to the {@link WidgetProvider onReceive} method.
     *
     * @return PendingIntent
     */
    public static PendingIntent getUpdatePendingIntent(Context context, int appWidgetId, Boolean updateByUser) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(WidgetService.ACTION_UPDATE_WIDGET);
        intent.putExtra(WidgetService.EXTRA_UPDATE_BY_USER, updateByUser);
        intent.putExtra(WidgetService.EXTRA_WIDGET_ID, appWidgetId);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Helper function for creating a pendingIntent to switch to the previous or next device.
     * The broadcast pendingIntent is send to the {@link WidgetProvider onReceive} method.
     *
     * @return PendingIntent
     */
    public static PendingIntent getSwitchDevicePendingIntent(Context context, int appWidgetId, String switchDirection) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(WidgetService.ACTION_SWITCH_DEVICE);
        intent.addCategory(switchDirection);
        intent.putExtra(WidgetService.EXTRA_DEVICE_SWITCH_DIRECTION, switchDirection);
        intent.putExtra(WidgetService.EXTRA_WIDGET_ID, appWidgetId);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Helper function for updating a widget by its ID.
     * A background service takes care of updating the widgets UI.
     */
    public static void remoteUpdateWidget(Context context, int appWidgetId) {
        PendingIntent pendingIntent = WidgetUtils.getUpdatePendingIntent(context, appWidgetId, false);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper function for updating all widgets on the screen.
     * A background service takes care of updating the widgets UI.
     */
    public static void remoteUpdateAllWidgets(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

        for (int appWidgetId : appWidgetIds) {
            remoteUpdateWidget(context, appWidgetId);
        }
    }

    /**
     * Returns the device that the widget will use by default.
     * @return DeviceObject
     */
    public static DeviceObject getDefaultDevice(Context context){
        SharedPreferences sharedPref = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String jsonString = sharedPref.getString(context.getResources().getString((R.string.saved_current_device_key)), "");
        DeviceObject deviceObject = gson.fromJson(jsonString, DeviceObject.class);

        return deviceObject;
    }

    /**
     * Returns the device that the user has currently selected for the widget to use.
     * @return DeviceObject
     */
    public static DeviceObject getPreferredDevice(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String jsonString = sharedPreferences.getString(String.valueOf(R.string.pref_preferredDevice_key), "");
        DeviceObject deviceObject = null;
        if(Utils.isJson(jsonString)){
            deviceObject = gson.fromJson(jsonString, DeviceObject.class);
        }
        return deviceObject;
    }

    /**
     * @return preferred temperature scale.
     */
    public static String getTempScalePreference(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(R.string.pref_tempScale_key);
        String defaultValue = context.getString(R.string.pref_tempScale_value_celsius);

        return sharedPreferences.getString(prefKey, defaultValue);
    }
}
