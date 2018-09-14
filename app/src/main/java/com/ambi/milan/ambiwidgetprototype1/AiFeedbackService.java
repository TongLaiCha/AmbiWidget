package com.ambi.milan.ambiwidgetprototype1;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class AiFeedbackService extends IntentService {
    private static final String TAG = "AiFeedbackService";

    public static final String ACTION_GIVE_FEEDBACK =
            "com.ambi.milan.ambiwidgetprototype1.action.give_feedback";
    public static final String EXTRA_FEEDBACK_TAG =
            "com.ambi.milan.ambiwidgetprototype1.extra.FEEDBACK_TAG";
    public static final String ACTION_UPDATE_WIDGET =
            "com.ambi.milan.ambiwidgetprototype1.action.update_widget";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public AiFeedbackService() {
        super("AiFeedbackService");
    }

    /**
     * Starts this service to perform GiveFeedback action with the given parameters.
     *
     * @see IntentService
     */
    public static void startActionGiveFeedback(Context context, String feedbackTag) {
        Intent intent = new Intent(context, AiFeedbackService.class);
        intent.setAction(ACTION_GIVE_FEEDBACK);
        intent.putExtra(EXTRA_FEEDBACK_TAG, feedbackTag);
        context.startService(intent);
    }

    /**
     * Starts this service to perform UpdateWidget with the given parameters.
     *
     * @see IntentService
     */
    public static void startActionUpdateWidget(Context context) {
        Intent intent = new Intent(context, AiFeedbackService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        context.startService(intent);
        Log.d(TAG, "startActionUpdateWidget: Starting intent..");
    }

    /**
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GIVE_FEEDBACK.equals(action)) {
                final String feedbackTag = intent.getStringExtra(EXTRA_FEEDBACK_TAG);
                handleActionGiveFeedback(feedbackTag);
            } else if(ACTION_UPDATE_WIDGET.equals(action)) {
                handleActionUpdateWidget();
            }
        }
    }

    /**
     * Handle action UpdateWidget in the provided background threat.
     */
    private void handleActionUpdateWidget() {
        Log.d(TAG, "handleActionUpdateWidget: Updating widget!");

        WidgetData data = new WidgetData("Interns desk", 25.6, 58.2, "Work");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, FullWidgetProvider.class));

        FullWidgetProvider.updateWidgetsData(this, appWidgetManager, data, appWidgetIds);
    }

    /**
     * Handle action GiveFeedback in the provided background threat.
     *
     * @param feedbackTag
     */
    private void handleActionGiveFeedback(String feedbackTag) {
        //Call class for API handling and giving feedback to the Ai.
        Log.d(TAG, "handleActionGiveFeedback: Giving feedback: It is " + feedbackTag + " to the Ai.");
    }
}
