package com.ambi.milan.ambiwidgetprototype1.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ambi.milan.ambiwidgetprototype1.services.AiFeedbackService;
import com.ambi.milan.ambiwidgetprototype1.FullWidgetProvider;

public final class WidgetUtils {
    private static final String TAG = "WidgetUtils";

    /**
     * Helper function for creating a pendingIntent.
     *
     * @return PendingIntent
     */
    public static PendingIntent getPendingIntent(Context context, String action, String tag) {
        Intent intent = new Intent(context, FullWidgetProvider.class);
        intent.setAction(action);

        //Give the pendingIntent a category
        //If pendingIntents only vary by their "extra" contents, they will be seen as the same and get overwritten.
        if(tag != null){
            intent.addCategory(tag);
            intent.putExtra(AiFeedbackService.EXTRA_FEEDBACK_TAG, tag);
        }
        Log.d(TAG, "getPendingIntent: Creating pendingIntent");
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
