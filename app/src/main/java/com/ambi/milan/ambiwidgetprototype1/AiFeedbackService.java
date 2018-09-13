package com.ambi.milan.ambiwidgetprototype1;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AiFeedbackService extends IntentService {
    public static final String ACTION_GIVE_FEEDBACK =
            "com.ambi.milan.ambiwidgetprototype1.action.give_feedback";
    public static final String EXTRA_FEEDBACK_TAG =
            "com.ambi.milan.ambiwidgetprototype1.extra.FEEDBACK_TAG";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public AiFeedbackService() {
        super("AiFeedbackService");
    }

    public static void startActionGiveFeedback(Context context, String feedbackTag) {
        Intent intent = new Intent(context, AiFeedbackService.class);
        intent.setAction(ACTION_GIVE_FEEDBACK);
        intent.putExtra(EXTRA_FEEDBACK_TAG, feedbackTag);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GIVE_FEEDBACK.equals(action)) {
                final String feedbackTag = intent.getStringExtra(EXTRA_FEEDBACK_TAG);
                handleActionGiveFeedback(feedbackTag);
            }
        }
    }

    private void handleActionGiveFeedback(String feedbackTag) {
        //Call class for API handling and giving feedback to the Ai.
        Log.d("FEEDBACK", "handleActionGiveFeedback: Giving feedback: It is " + feedbackTag + " to the Ai.");
    }
}
