package brandonmilan.tonglaicha.ambiwidget.services;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.WidgetProvider;
import brandonmilan.tonglaicha.ambiwidget.WidgetStorageManager;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.objects.WidgetObject;
import brandonmilan.tonglaicha.ambiwidget.utils.WidgetUtils;

/**
 * Class for handling tasks in a background thread.
 * @author Milan Sosef
 */
public class WidgetService extends JobIntentService {
	private static final String TAG = "WidgetService";

	public static final String ACTION_GIVE_FEEDBACK =
			"brandonmilan.tonglaicha.ambiwidget.action.give_feedback";
	public static final String EXTRA_FEEDBACK_TAG =
			"brandonmilan.tonglaicha.ambiwidget.extra.ACTION_TAG";
	public static final String ACTION_UPDATE_WIDGET =
			"brandonmilan.tonglaicha.ambiwidget.action.update_widget";
	public static final String EXTRA_UPDATE_BY_USER =
			"brandonmilan.tonglaicha.ambiwidget.extra.update_by_user";
	public static final String EXTRA_FEEDBACK_GIVEN =
			"brandonmilan.tonglaicha.ambiwidget.extra.feedback_given";
	public static final String ACTION_SWITCH_ON_OFF =
			"brandonmilan.tonglaicha.ambiwidget.action.switch_on_off";
	public static final String EXTRA_WIDGET_ID =
			"brandonmilan.tonglaicha.ambiwidget.extra.widget_id";
	public static final String ACTION_SWITCH_DEVICE =
			"brandonmilan.tonglaicha.ambiwidget.action.switch_device";
	public static final String EXTRA_DEVICE_SWITCH_DIRECTION =
			"brandonmilan.tonglaicha.ambiwidget.extra.device_switch_direction";

	public static Boolean busy = false;

	public static void preEnqueueWork(Context context, int JOB_ID, Intent intent) {
		String action = intent.getAction();

		if (action != null) {
			// Prevent button spam
			if (action.equals(ACTION_GIVE_FEEDBACK) || action.equals(ACTION_SWITCH_ON_OFF))  {
				if (WidgetService.busy) {
					return;
				} else {
					WidgetService.busy = true;
				}
			}

			// Display loading animation on feedback buttons.
			if(WidgetService.ACTION_GIVE_FEEDBACK.equals(action)){
				String feedbackGiven = intent.getStringExtra(WidgetService.EXTRA_FEEDBACK_TAG);
				Integer appWidgetId = intent.getIntExtra(WidgetService.EXTRA_WIDGET_ID, 0);

				// Get widget object
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);

				// Update loading animation state of clicked feedback button
				widgetObject.setFeedbackBtnLoadingState(feedbackGiven, true);
				widgetObject.saveAndUpdate(context);

			} // Display loading animation on power button.
			else if(WidgetService.ACTION_SWITCH_ON_OFF.equals(action)) {
				Integer appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);

				// Get widget object.
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);

				//Update loading animation state of power button.
				widgetObject.setPowerBtnIsLoading(true);
				widgetObject.saveAndUpdate(context);
			}

			WidgetService.enqueueWork(context, WidgetService.class, JOB_ID, intent);
		} else {
			Log.e(TAG, "preEnqueueWork: Unable to enqueue work, action is null.", new Exception("ERROR_ACTION_IS_NULL"));
		}
	}

	/**
	 * Handle the incoming job intent in a background thread.
	 */
	@Override
	protected void onHandleWork(Intent intent) {
			final String action = intent.getAction();
			if (ACTION_GIVE_FEEDBACK.equals(action)) {
				final String feedbackTag = intent.getStringExtra(EXTRA_FEEDBACK_TAG);
				final int appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
				handleActionGiveFeedback(appWidgetId, feedbackTag);
			} else if(ACTION_UPDATE_WIDGET.equals(action)) {
				final int appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
				final Boolean UpdateByUser = intent.getBooleanExtra(EXTRA_UPDATE_BY_USER, false);
				handleActionUpdateWidget(appWidgetId, UpdateByUser);
			} else if(ACTION_SWITCH_ON_OFF.equals(action)) {
				final int appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
				handleActionSwitchOnOff(appWidgetId);
			} else if (ACTION_SWITCH_DEVICE.equals(action)) {
				final int appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
				final String switchDirection = intent.getStringExtra(EXTRA_DEVICE_SWITCH_DIRECTION);
				handleActionSwitchDevice(appWidgetId, switchDirection);
			}
	}

	/**
	 * Handle action GiveFeedback in the provided background threat.
	 * @param feedbackTag
	 */
	private void handleActionGiveFeedback(final int appWidgetId, final String feedbackTag) {
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

		// Send comfort feedback to API
		new DataManager.UpdateComfortTask(getApplicationContext(), new OnProcessFinish<ReturnObject>() {

			@Override
			public void onSuccess(ReturnObject result) {
				String feedbackMsg = feedbackTag.replace("_", " ");
				String confirmToast = "Feedback given: " + feedbackMsg + ".";
				Toast.makeText(getApplicationContext(), confirmToast, Toast.LENGTH_LONG).show();

				// Get the widget object from file storage
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

				// Update the mode in the widgetObject to Comfort mode
				widgetObject.deviceStatus.getMode().setModeName("Comfort");

				// Update the prediction object in the widgetObject to Comfort level (for border update)
				widgetObject.deviceStatus.getComfortPrediction().setLevelByTag(feedbackTag);

				// Disable loading animation of all comfort buttons
				widgetObject.setFeedbackBtnLoadingState("ALL", false);
				widgetObject.saveAndUpdate(getApplicationContext());

				WidgetService.busy = false;
			}

			@Override
			public void onFailure(ReturnObject result) {
				Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
				Log.d(TAG, result.errorMessage + ": " + result.exception);

				// Get widget object.
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

				// Disable loading animation of all comfort buttons
				widgetObject.setFeedbackBtnLoadingState("ALL", false);
				widgetObject.saveAndUpdate(getApplicationContext());
        
				WidgetService.busy = false;
			}
		}, deviceObject, feedbackTag).execute();
	}


	/**
	 * Handle action UpdateWidget in the provided background threat.
	 */
	private void handleActionUpdateWidget(int appWidgetId, Boolean updateByUser) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		WidgetProvider.updateWidget(this, appWidgetManager, appWidgetId, updateByUser);
	}

	/**
	 * Handle action SwitchDevice in the provided background threat.
	 */
	private void handleActionSwitchDevice(int appWidgetId, String switchDirection) {
		Log.d(TAG, "handleActionSwitchDevice: switching to the " + switchDirection + " device.");
		//TODO: Write functionality to iterate over the device list and switch device.
	}

	/**
	 * Handle action SwitchOnOff in provided background threat.
	 */
	// TODO: Do ON/OFF feedback based on what the USER SEES (local data from last update) and NOT doing a new update.
	private void handleActionSwitchOnOff(final int appWidgetId) {
		//Get the widget object.
		WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

		if (widgetObject.deviceStatus == null) {
			Log.e(TAG, "handleActionSwitchOnOff: widgetObject.deviceStatus == null");
			WidgetService.busy = false;
			return;
		}

		String modeName = widgetObject.deviceStatus.getMode().getModeName();
		String power = widgetObject.deviceStatus.getApplianceState().getPower();
		Log.d(TAG, "modeName: "+modeName);
		Log.d(TAG, "power: "+power);

		// Enable loading animation of power button
		widgetObject.setPowerBtnIsLoading(true);
		widgetObject.saveAndUpdate(getApplicationContext());

		// If AC is off
		if (modeName.equals("Off") || (modeName.equals("Manual")) && power.equals("Off")) {
			//Set the the device to Comfort mode.
			setDeviceToComfort(getApplicationContext(), appWidgetId, widgetObject.device);
			Log.d(TAG, "setDeviceToComfort: ");
		} else {
			//Turn off the AC.
			turnDeviceOff(getApplicationContext(), appWidgetId, widgetObject.device);
		}
	}

	/**
	 * Set a device in "Off" mode.
	 * @param preferredDevice
	 */
	private void turnDeviceOff(final Context context, final int appWidgetId, DeviceObject preferredDevice) {

		new DataManager.PowerOffTask(context, new OnProcessFinish<ReturnObject>() {

			@Override
			public void onSuccess(ReturnObject result) {
				String confirmToast = "Device is now in off mode.";
				Log.d(TAG, confirmToast);

				// Change mode icon
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);
				widgetObject.deviceStatus.getMode().setModeName("Off");

				//Disable loading animation of power button.
				widgetObject.setPowerBtnIsLoading(false);

				widgetObject.saveAndUpdate(context);

				Toast.makeText(context, confirmToast, Toast.LENGTH_LONG).show();

				WidgetService.busy = false;
			}

			@Override
			public void onFailure(ReturnObject result) {
                Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
				Log.d(TAG, result.errorMessage + ": " + result.exception);

				//Update loading animation state of power button.
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);
				widgetObject.setPowerBtnIsLoading(false);

				widgetObject.saveAndUpdate(context);

				WidgetService.busy = false;
			}
		}, preferredDevice).execute();

	}

	/**
	 * Set a device in "Comfort" mode.
	 * @param preferredDevice
	 */
	private void setDeviceToComfort(final Context context, final int appWidgetId, DeviceObject preferredDevice) {

		new DataManager.UpdateModeTask(context, new OnProcessFinish<ReturnObject>() {

			@Override
			public void onSuccess(ReturnObject result) {
				String confirmToast = "Device is now in comfort mode.";
				Log.d(TAG, confirmToast);

				// Change mode icon
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);
				widgetObject.deviceStatus.getMode().setModeName("Comfort");

				//Disable loading animation of power button.
				widgetObject.setPowerBtnIsLoading(false);

				widgetObject.saveAndUpdate(context);

				Toast.makeText(context, confirmToast, Toast.LENGTH_LONG).show();

				WidgetService.busy = false;
			}

			@Override
			public void onFailure(ReturnObject result) {
                Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
				Log.d(TAG, result.errorMessage + ": " + result.exception);

				//Disable loading animation of power button.
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);
				widgetObject.setPowerBtnIsLoading(false);
				widgetObject.saveAndUpdate(context);
        
				WidgetService.busy = false;
			}
		}, preferredDevice, "comfort", null, false).execute();

	}
}
