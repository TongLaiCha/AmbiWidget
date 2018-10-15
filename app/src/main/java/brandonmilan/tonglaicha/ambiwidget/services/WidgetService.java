package brandonmilan.tonglaicha.ambiwidget.services;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.WidgetProvider;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.utils.WidgetUtils;

/**
 * Class for handling tasks in a background thread.
 * @author Milan Sosef
 */
public class WidgetService extends JobIntentService {
    private static final String TAG = "WidgetService";

    public static final String ACTION_GIVE_FEEDBACK =
            "brandonmilan.tonglaicha.ambiwidget.action.give_feedback";
    public static final String EXTRA_ACTION_TAG =
            "brandonmilan.tonglaicha.ambiwidget.extra.ACTION_TAG";
    public static final String ACTION_UPDATE_WIDGET =
            "brandonmilan.tonglaicha.ambiwidget.action.update_widget";
    public static final String ACTION_SWITCH_ON_OFF =
            "brandonmilan.tonglaicha.ambiwidget.action.switch_on_off";

    /**
     * Handle the incoming jobIntent in a background thread.
     *
     * @param intent
     */
    @Override
    protected void onHandleWork(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GIVE_FEEDBACK.equals(action)) {
                final String feedbackTag = intent.getStringExtra(EXTRA_ACTION_TAG);
                handleActionGiveFeedback(feedbackTag);
            } else if(ACTION_UPDATE_WIDGET.equals(action)) {
                final String UpdateByUser = intent.getStringExtra(EXTRA_ACTION_TAG);
                handleActionUpdateWidget(UpdateByUser);
            } else if(ACTION_SWITCH_ON_OFF.equals(action)) {
                handleActionSwitchOnOff();
            }
        }
    }

    /**
     * Handle action GiveFeedback in the provided background threat.
     * @param feedbackTag
     */
    private void handleActionGiveFeedback(final String feedbackTag) {
        //Call class for API handling and giving feedback to the Ai.
        Log.d(TAG, "handleActionGiveFeedback: Giving feedback: It is " + feedbackTag + " to the Ai.");

       DeviceObject deviceObject;
        final DeviceObject defaultDeviceObject = WidgetUtils.getDefaultDevice(getApplicationContext());
        final DeviceObject preferredDeviceObject = WidgetUtils.getPreferredDevice(getApplicationContext());

        //Use default device if no preferred device is selected.
        if (preferredDeviceObject == null){
            deviceObject = defaultDeviceObject;
            Log.d(TAG, "handleActionGiveFeedback: No preferred device selected! Using default.");
        } else {
            deviceObject = preferredDeviceObject;
            Log.d(TAG, "handleActionGiveFeedback: Using prefered device!");
        }

        new DataManager.UpdateComfortTask(getApplicationContext(), new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                String feedbackMsg = feedbackTag.replace("_", " ");
                String confirmToast = "Feedback given: " + feedbackMsg + ".";
                Toast.makeText(getApplicationContext(), confirmToast, Toast.LENGTH_LONG).show();
                WidgetUtils.remoteUpdateWidget(getApplicationContext());
            }

            @Override
            public void onFailure(ReturnObject result) {
                Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }
        }, deviceObject, feedbackTag).execute();
    }

    /**
     * Handle action UpdateWidget in the provided background threat.
     */
    private void handleActionUpdateWidget(String TAG) {
        Log.d(TAG, "handleActionUpdateWidget: Updating widget!");
        Boolean updateFromUser = false;
        if(TAG != null && TAG.equals(WidgetProvider.UpdateByUserTag)){
            updateFromUser = true;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));

        WidgetProvider.updateAllWidgets(this, appWidgetManager, appWidgetIds, updateFromUser);
    }

    /**
     * Handle action SwitchOnOff in provided background threat.
     */
    private void handleActionSwitchOnOff() {
        Log.d(TAG, "handleActionSwitchOnOff: Switching on or off!");

        //Get the current device.
        final DeviceObject preferredDevice = WidgetUtils.getPreferredDevice(getApplicationContext());

        //Get the current mode of the device.
        new DataManager.GetModeTask(getApplicationContext(), new OnProcessFinish<ReturnObject>() {
            @Override
            public void onSuccess(ReturnObject result) {
                Log.d(TAG, "Current Mode: result.value = " + result.value);

                if (result.value.equals("Manual")){
                    //Set the the device to Comfort mode.
                    WidgetUtils.setDeviceToComfort(getApplicationContext(), preferredDevice);
                } else {
                    //Turn off the AC.
                    WidgetUtils.turnDeviceOff(getApplicationContext(), preferredDevice);
                }

            }
            @Override
            public void onFailure(ReturnObject result) {
//                Toast.makeText(context, "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }
        }, preferredDevice).execute();
    }
}
