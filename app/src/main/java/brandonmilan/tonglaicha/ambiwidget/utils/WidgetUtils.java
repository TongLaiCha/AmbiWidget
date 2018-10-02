package brandonmilan.tonglaicha.ambiwidget.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import brandonmilan.tonglaicha.ambiwidget.R;
import brandonmilan.tonglaicha.ambiwidget.WidgetProvider;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
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
        Log.d(TAG, "getPendingIntent: Creating pendingIntent");
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

    public static DeviceObject getDefaultDevice(Context context){
        SharedPreferences sharedPref = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String jsonString = sharedPref.getString(context.getResources().getString((R.string.saved_current_device_key)), "");
        DeviceObject deviceObject = gson.fromJson(jsonString, DeviceObject.class);

        return deviceObject;
    }

    public static DeviceObject getPreferredDevice(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String jsonString = sharedPreferences.getString(String.valueOf(R.string.pref_preferredDevice_key), "");
        DeviceObject deviceObject = gson.fromJson(jsonString, DeviceObject.class);

        return deviceObject;
    }

    public static String getTempScalePreference(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(R.string.pref_tempScale_key);
        String defaultValue = context.getString(R.string.pref_tempScale_value_celsius);
        return sharedPreferences.getString(prefKey, defaultValue);
    }

    public static double convertToFahrenheit(double temperatureCelsius) {
        return (temperatureCelsius * 1.8) + 32;
    }

    public static double roundOneDecimal(double number) {
        return Math.round(number * 10) / 10.0;
    }
}
