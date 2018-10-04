package brandonmilan.tonglaicha.ambiwidget.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.R;
import brandonmilan.tonglaicha.ambiwidget.WidgetContentManager;
import brandonmilan.tonglaicha.ambiwidget.WidgetProvider;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.services.WidgetService;

public final class WidgetUtils {
    private static final String TAG = "WidgetUtils";
    private static final String ActionUpdate = WidgetService.ACTION_UPDATE_WIDGET;

    /**
     * Helper function for creating a pendingIntent.
     * The broadcast pendingIntent is send to the {@link WidgetProvider onReceive} method.
     *
     * @return PendingIntent
     */
    public static PendingIntent getPendingIntent(Context context, String action, String tag) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(action);

        //Give the pendingIntent a category
        //If pendingIntents only vary by their "extra" contents, they will be seen as the same and get overwritten.
        if(tag != null){
            intent.addCategory(tag);
            intent.putExtra(WidgetService.EXTRA_FEEDBACK_TAG, tag);
        }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Send a pendingIntent with the updateWidgetAction.
     * A background service takes care of updating the widgets UI.
     */
    public static void remoteUpdateWidget(Context context) {
        PendingIntent pendingIntent = WidgetUtils.getPendingIntent(context, ActionUpdate, null);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
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

    /**
     * Helper function to set a device in "Off" mode.
     * @param preferredDevice
     */
    public static void turnDeviceOff(final Context context, DeviceObject preferredDevice) {

        new DataManager.PowerOffTask(context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                String confirmToast = "Device is now in off mode.";
                Log.d(TAG, confirmToast);
                WidgetUtils.remoteUpdateWidget(context);
                Toast.makeText(context, confirmToast, Toast.LENGTH_SHORT).show();
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
    public static void setDeviceToComfort(final Context context, DeviceObject preferredDevice) {

        new DataManager.UpdateModeTask(context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                String confirmToast = "Device is now in comfort mode.";
                Log.d(TAG, confirmToast);
                WidgetUtils.remoteUpdateWidget(context);
                Toast.makeText(context, confirmToast, Toast.LENGTH_SHORT).show();
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
