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
public class FullWidget extends AppWidgetProvider {
    private static final String TAG = "FullWidget";
    private static final String TooColdTag = "too_cold";
    private static final String LittleColdTag = "little_cold";
    private static final String ComfyTag = "comfy";
    private static final String LittleWarmTag = "little_warm";
    private static final String TooWarmTag = "too_warm";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.full_widget);

        //Display the name and location of the device
        views.setTextViewText(R.id.deviceName, "Interns row");
        views.setTextViewText(R.id.location, "Work");

        //Update the temperature and humidity
        views.setTextViewText(R.id.temperature, "24.3");
        views.setTextViewText(R.id.humidity, "77%");

        //Set onClickPendingIntents for all the buttons.
        views.setOnClickPendingIntent(R.id.button_too_cold, getPendingFeedbackIntent(context, TooColdTag));
        views.setOnClickPendingIntent(R.id.button_little_cold, getPendingFeedbackIntent(context, LittleColdTag));
        views.setOnClickPendingIntent(R.id.button_comfy, getPendingFeedbackIntent(context, ComfyTag));
        views.setOnClickPendingIntent(R.id.button_little_warm, getPendingFeedbackIntent(context, LittleWarmTag));
        views.setOnClickPendingIntent(R.id.button_too_warm, getPendingFeedbackIntent(context, TooWarmTag));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

        }
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

    //Helper function for creating a pendingIntent
    private static PendingIntent getPendingFeedbackIntent(Context context, String feedbackTag) {
        Intent intent = new Intent(context, AiFeedbackService.class);
        intent.setAction(AiFeedbackService.ACTION_GIVE_FEEDBACK);

        //Give the pendingIntent a category
        //If pendingIntents only vary by their "extra" contents, they will be seen as the same and get overwritten.
        intent.addCategory(feedbackTag);
        intent.putExtra(AiFeedbackService.EXTRA_FEEDBACK_TAG, feedbackTag);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

