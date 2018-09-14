package com.ambi.milan.ambiwidgetprototype1;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import com.ambi.milan.ambiwidgetprototype1.databinding.FullWidgetBinding;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link FullWidgetConfigureActivity FullWidgetConfigureActivity}
 */
public class FullWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "FullWidgetProvider";
    private static final String TooColdTag = "too_cold";
    private static final String LittleColdTag = "little_cold";
    private static final String ComfyTag = "comfy";
    private static final String LittleWarmTag = "little_warm";
    private static final String TooWarmTag = "too_warm";
    private static final String ActionFeedback = AiFeedbackService.ACTION_GIVE_FEEDBACK;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                WidgetData widgetData, int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.full_widget);
        Log.d(TAG, "updateAppWidget: RemoteViews object created.");

        //Display the name and location of the device
        views.setTextViewText(R.id.deviceName, widgetData.getDeviceName());
        views.setTextViewText(R.id.location, widgetData.getLocation());

        //Update the temperature and humidity
        views.setTextViewText(R.id.temperature, String.format("%.1f", widgetData.getTemperature()));
        views.setTextViewText(R.id.humidity, String.format("%.1f", widgetData.getHumidity()) + "%");

        //Set onClickPendingIntents for all the buttons.
        views.setOnClickPendingIntent(R.id.button_too_cold, getPendingIntent(context, ActionFeedback, TooColdTag));
        views.setOnClickPendingIntent(R.id.button_little_cold, getPendingIntent(context, ActionFeedback, LittleColdTag));
        views.setOnClickPendingIntent(R.id.button_comfy, getPendingIntent(context, ActionFeedback, ComfyTag));
        views.setOnClickPendingIntent(R.id.button_little_warm, getPendingIntent(context, ActionFeedback, LittleWarmTag));
        views.setOnClickPendingIntent(R.id.button_too_warm, getPendingIntent(context, ActionFeedback, TooWarmTag));

        //Set onClickPendingIntent for the refresh button.
        views.setOnClickPendingIntent(R.id.button_refresh, getPendingIntent(context, AiFeedbackService.ACTION_UPDATE_WIDGET, "Update") );
        
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        Log.d(TAG, "updateAppWidget: Success!");
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
//        for (int appWidgetId : appWidgetIds) {
//            updateAppWidget(context, appWidgetManager, appWidgetId);
//
//        }

        Log.d(TAG, "onUpdate: Executed...");
        //Start the intent service update widget action, the service takes care of updating the widgets UI.
        AiFeedbackService.startActionUpdateWidget(context);
    }

    public static void updateWidgetsData(Context context, AppWidgetManager appWidgetManager,
                                         WidgetData widgetData, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetData, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // Chain up to the super class so the onEnabled, etc callbacks get dispatched
        super.onReceive(context, intent);
        // Handle a different Intent
        Log.d(TAG, "onReceive()" + intent.getAction());

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            FullWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
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

    /**
     * Helper function for creating a pendingIntent.
     *
     * @return PendingIntent
     */
    private static PendingIntent getPendingIntent(Context context, String action, String tag) {
        Intent intent = new Intent(context, AiFeedbackService.class);
        intent.setAction(action);

        //Give the pendingIntent a category
        //If pendingIntents only vary by their "extra" contents, they will be seen as the same and get overwritten.
//        if(tag != null){
            intent.addCategory(tag);
            intent.putExtra(AiFeedbackService.EXTRA_FEEDBACK_TAG, tag);
//        }

        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

