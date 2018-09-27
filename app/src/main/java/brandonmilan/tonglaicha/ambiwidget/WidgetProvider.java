package brandonmilan.tonglaicha.ambiwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.Gson;

import brandonmilan.tonglaicha.ambiwidget.activities.SettingsActivity;
import brandonmilan.tonglaicha.ambiwidget.activities.WidgetConfigureActivity;
import brandonmilan.tonglaicha.ambiwidget.objects.WidgetDataObject;
import brandonmilan.tonglaicha.ambiwidget.services.WidgetService;
import brandonmilan.tonglaicha.ambiwidget.utils.WidgetUtils;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WidgetConfigureActivity WidgetConfigureActivity}
 * @author Milan Sosef
 */
public class WidgetProvider extends AppWidgetProvider implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "WidgetProvider";
    private static final String TooColdTag = "too_cold";
    private static final String LittleColdTag = "bit_cold";
    private static final String ComfyTag = "comfortable";
    private static final String LittleWarmTag = "bit_warm";
    private static final String TooWarmTag = "too_warm";
    private static final String ActionFeedback = WidgetService.ACTION_GIVE_FEEDBACK;
    private static final String ActionUpdate = WidgetService.ACTION_UPDATE_WIDGET;
    private static final Integer JOB_ID = 10;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                WidgetDataObject widgetData, int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.full_widget);
        Gson gson = new Gson();
        Log.i(TAG, "RemoteView: "+gson.toJson(views));
        Log.d(TAG, "updateAppWidget: RemoteViews object created.");

        //Update the temperature, humidity, room name and location name.
        WidgetContentManager.getInstance(appWidgetManager, views, appWidgetId).updateView(context);

        //Set onClickPendingIntents for all the feedback buttons.
        views.setOnClickPendingIntent(R.id.button_too_cold, WidgetUtils.getPendingIntent(context, ActionFeedback, TooColdTag));
        views.setOnClickPendingIntent(R.id.button_little_cold, WidgetUtils.getPendingIntent(context, ActionFeedback, LittleColdTag));
        views.setOnClickPendingIntent(R.id.button_comfy, WidgetUtils.getPendingIntent(context, ActionFeedback, ComfyTag));
        views.setOnClickPendingIntent(R.id.button_little_warm, WidgetUtils.getPendingIntent(context, ActionFeedback, LittleWarmTag));
        views.setOnClickPendingIntent(R.id.button_too_warm, WidgetUtils.getPendingIntent(context, ActionFeedback, TooWarmTag));

        //Set onClickPendingIntent for the settings button.
        Intent configIntent = new Intent(context, SettingsActivity.class);
        //WARNING: Include the widget ID with the pendingIntent, or the configuration activity will not be opened.
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, 0);
        views.setOnClickPendingIntent(R.id.button_settings, configPendingIntent);

        //Set onClickPendingIntent for the refresh button.
        views.setOnClickPendingIntent(R.id.button_refresh,
                WidgetUtils.getPendingIntent(context, ActionUpdate, null)); //TODO: try null for the tag
        
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        Log.d(TAG, "updateAppWidget: Success!");
    }

    //TODO: Make onUpdate only execute after the configuration is done.
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate: Executed...");

        //Send the pendingIntent with the update widget action, a background service takes care of updating the widgets UI.
        PendingIntent pendingIntent = WidgetUtils.getPendingIntent(context, ActionUpdate, null); //TODO: try null for the tag
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

    }

    /**
     * Update all widgets currently active on the screen.
     */
    public static void updateAllWidgets(Context context, AppWidgetManager appWidgetManager,
                                        WidgetDataObject widgetDataObject, int[] appWidgetIds) {
        Log.d(TAG, "updateAllWidgets: Executed..");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetDataObject, appWidgetId);
        }
    }

    /**
     * Recieves and handles broadcast intents sent to the {@link WidgetProvider WidgetProvider}.
     * This method will enqueue new work to be dispatched to and handled by the {@link WidgetService }.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "onReceive()" + intent.getAction());

        WidgetService.enqueueWork(context, WidgetService.class, JOB_ID, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
//            WidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
            PreferenceManager.getDefaultSharedPreferences(context)
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        setupSharedPreferences(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private void setupSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //TODO: update the widget.
//        if (key.equals(getString(R.string.pref_show_celsius_key))) {
//
//        }
    }
}

