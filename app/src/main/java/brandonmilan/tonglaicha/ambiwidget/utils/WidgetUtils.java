package brandonmilan.tonglaicha.ambiwidget.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.R;
import brandonmilan.tonglaicha.ambiwidget.WidgetProvider;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.services.WidgetService;

public final class WidgetUtils {
    private static final String TAG = "WidgetUtils";

    /**
     * Helper function for creating a give feedback pendingIntent.
     * The broadcast pendingIntent is send to the {@link WidgetProvider onReceive} method.
     *
     * @return PendingIntent
     */
    public static PendingIntent getGiveFeedbackPendingIntent(Context context, int appWidgetId, RemoteViews views, String FeedbackTag) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(WidgetService.ACTION_GIVE_FEEDBACK);
        //Give the pendingIntent a category
        //If pendingIntents only vary by their "extra" contents, they will be seen as the same and get overwritten.
        intent.addCategory(FeedbackTag);
        intent.putExtra(WidgetService.EXTRA_FEEDBACK_TAG, FeedbackTag);
        intent.putExtra(WidgetService.EXTRA_REMOTEVIEWS_OBJECT, views);
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

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Helper function for creating an update pendingIntent.
     * The broadcast pendingIntent is send to the {@link WidgetProvider onReceive} method.
     *
     * @return PendingIntent
     */
    public static PendingIntent getUpdatePendingIntent(Context context, int appWidgetId, Boolean updateByUser, String feedbackGiven) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(WidgetService.ACTION_UPDATE_WIDGET);
        intent.putExtra(WidgetService.EXTRA_UPDATE_BY_USER, updateByUser);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Send a pendingIntent with the updateWidgetAction.
     * A background service takes care of updating the widgets UI.
     */
    public static void remoteUpdateWidget(Context context, int appWidgetId, String feedbackGiven) {
        PendingIntent pendingIntent = WidgetUtils.getUpdatePendingIntent(context, appWidgetId, false, feedbackGiven);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public static void remoteUpdateAllWidgets(Context context){
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(WidgetService.ACTION_UPDATE_WIDGET);
        intent.putExtra(WidgetService.EXTRA_UPDATE_BY_USER, false);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public static void updateRefreshAnimation(Boolean showAnimation, RemoteViews views) {
        if (showAnimation) {
            views.setViewVisibility(R.id.button_refresh, View.INVISIBLE);
            views.setViewVisibility(R.id.progressBar, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.button_refresh, View.VISIBLE);
            views.setViewVisibility(R.id.progressBar, View.INVISIBLE);
        }
    }

    /**
     * Helper function to get the remoteViews object matching the given widgetId.
     * @return remoteViewFromArray
     */
    public static RemoteViews getRemoteViewsByWidgetId(int appWidgetId) {
        RemoteViews remoteViewsFromArray = null;
        for (int i = 0; i < WidgetProvider.remoteViewsByWidgetIds.size(); i++) {
            Integer key = WidgetProvider.remoteViewsByWidgetIds.keyAt(i);
            if(key.equals(appWidgetId)){
                remoteViewsFromArray = WidgetProvider.remoteViewsByWidgetIds.valueAt(i);
            }
        }

        if (remoteViewsFromArray == null){
            Log.e(TAG, "ERROR: viewFromArray = null.", new Exception("ERROR_REMOTEVIEW_NOT_FOUND"));
        }

        return remoteViewsFromArray;
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

    /**
     * Helper function to set a device in "Off" mode.
     * @param preferredDevice
     */
    public static void turnDeviceOff(final Context context, final int appWidgetId, DeviceObject preferredDevice) {

        new DataManager.PowerOffTask(context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                String confirmToast = "Device is now in off mode.";
                Log.d(TAG, confirmToast);
                WidgetUtils.remoteUpdateWidget(context, appWidgetId, null);
                Toast.makeText(context, confirmToast, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(ReturnObject result) {
//                Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }
        }, preferredDevice).execute();

    }

    /**
     * Helper function to set a device in "Comfort" mode.
     * @param preferredDevice
     */
    public static void setDeviceToComfort(final Context context, final int appWidgetId, DeviceObject preferredDevice) {

        new DataManager.UpdateModeTask(context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                String confirmToast = "Device is now in comfort mode.";
                Log.d(TAG, confirmToast);
                WidgetUtils.remoteUpdateWidget(context, appWidgetId, null);
                Toast.makeText(context, confirmToast, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(ReturnObject result) {
//                Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }
        }, preferredDevice, "comfort", null, false).execute();

    }

    public static double convertToFahrenheit(double temperatureCelsius) {
        return (temperatureCelsius * 1.8) + 32;
    }

    public static double roundOneDecimal(double number) {
        return Math.round(number * 10) / 10.0;
    }
}
