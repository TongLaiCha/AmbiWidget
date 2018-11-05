package brandonmilan.tonglaicha.ambiwidget.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import brandonmilan.tonglaicha.ambiwidget.R;
import brandonmilan.tonglaicha.ambiwidget.WidgetProvider;
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
    public static PendingIntent getUpdatePendingIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(WidgetService.ACTION_UPDATE_WIDGET);
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
     * @return preferred temperature scale.
     */
    public static String getTempScalePreference(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(R.string.pref_tempScale_key);
        String defaultValue = context.getString(R.string.pref_tempScale_value_celsius);

        return sharedPreferences.getString(prefKey, defaultValue);
    }
}
