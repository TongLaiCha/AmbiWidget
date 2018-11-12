package brandonmilan.tonglaicha.ambiwidget.services;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.R;
import brandonmilan.tonglaicha.ambiwidget.WidgetContentManager;
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
	public static final String ACTION_SWITCH_ON_OFF =
			"brandonmilan.tonglaicha.ambiwidget.action.switch_on_off";
	public static final String EXTRA_WIDGET_ID =
			"brandonmilan.tonglaicha.ambiwidget.extra.widget_id";
	public static final String ACTION_SWITCH_DEVICE =
			"brandonmilan.tonglaicha.ambiwidget.action.switch_device";
	public static final String EXTRA_DEVICE_SWITCH_DIRECTION =
			"brandonmilan.tonglaicha.ambiwidget.extra.device_switch_direction";
	public static final String ACTION_SWITCH_MODE =
			"brandonmilan.tonglaicha.ambiwidget.action.switch_mode";
	public static final String EXTRA_NEW_MODE =
			"brandonmilan.tonglaicha.ambiwidget.extra.new_mode";

	public static Boolean busy = false;

	/**
	 * Starts this service to perform UpdateWidget action with the given parameters.
	 * If the service is already performing a task, this action will be queued.
	 */
	public static void startActionUpdateWidget(Context context, int appWidgetId) {
		PendingIntent pendingIntent = WidgetUtils.getUpdatePendingIntent(context, appWidgetId);
		try {
			pendingIntent.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
	}

	public static void preEnqueueWork(Context context, int JOB_ID, Intent intent) {
		String action = intent.getAction();

		if (action != null) {
			// Prevent button spam
			if (action.equals(ACTION_GIVE_FEEDBACK) || action.equals(ACTION_SWITCH_ON_OFF) || action.equals(ACTION_SWITCH_DEVICE)) {
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

			}

			// Display loading animation on power button.
			else if(WidgetService.ACTION_SWITCH_ON_OFF.equals(action)) {
				Integer appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);

				// Get widget object.
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);

				// Update loading animation state of power button.
				widgetObject.setPowerBtnIsLoading(true);
				widgetObject.saveAndUpdate(context);
			}

			// Display loading animation on mode button.
			else if(WidgetService.ACTION_SWITCH_MODE.equals(action)) {
				Integer appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
				String newMode = intent.getStringExtra(EXTRA_NEW_MODE);

				// Get widget object.
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);

				// Update loading animation state of pressed mode button.
				widgetObject.setModeButtonIsLoading(true, newMode);
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
				handleActionUpdateWidget(appWidgetId);
			} else if(ACTION_SWITCH_ON_OFF.equals(action)) {
				final int appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
				handleActionSwitchOff(appWidgetId);
			} else if (ACTION_SWITCH_DEVICE.equals(action)) {
				final int appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
				final String switchDirection = intent.getStringExtra(EXTRA_DEVICE_SWITCH_DIRECTION);
				handleActionSwitchDevice(appWidgetId, switchDirection);
			} else if (ACTION_SWITCH_MODE.equals(action)) {
				final int appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
				final String newMode = intent.getStringExtra(EXTRA_NEW_MODE);
				handleActionSwitchMode(appWidgetId, newMode);
			}
	}

	/**
	 * Handle action GiveFeedback in the provided background threat.
	 * @param feedbackTag
	 */
	private void handleActionGiveFeedback(final int appWidgetId, final String feedbackTag) {
		//Call class for API handling and giving feedback to the Ai.
		Log.d(TAG, "handleActionGiveFeedback: Giving feedback: It is " + feedbackTag + " to the Ai.");

		// Get widget object
		WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

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
				widgetObject.deviceStatus.getMode().setModeName("comfort");

				// Update the prediction object in the widgetObject to Comfort level (for border update)
				widgetObject.deviceStatus.getComfortPrediction().setLevelByTag(feedbackTag);

				widgetObject.saveAndUpdate(getApplicationContext());
			}

			@Override
			public void onFailure(ReturnObject result) {
				Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
				Log.d(TAG, result.errorMessage + ": " + result.exception);
			}

			@Override
			public void onFinish(ReturnObject result) {
				// Get widget object.
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

				// Disable loading animation of all comfort buttons
				widgetObject.setFeedbackBtnLoadingState("ALL", false);
				widgetObject.saveAndUpdate(getApplicationContext());

				WidgetService.busy = false;
			}

		}, widgetObject.device, feedbackTag).execute();
	}


	/**
	 * Handle action UpdateWidget in the provided background threat.
	 */
	private void handleActionUpdateWidget(int appWidgetId) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		WidgetProvider.updateWidget(this, appWidgetManager, appWidgetId);
	}

	/**
	 * Handle action SwitchDevice in the provided background threat.
	 */
	private void handleActionSwitchDevice(int appWidgetId, String switchDirection) {
		Log.d(TAG, "handleActionSwitchDevice: switching to the " + switchDirection + " device.");

		List<DeviceObject> deviceObjectsList = WidgetStorageManager.getDeviceObjectsList(getApplicationContext());
		Log.d(TAG, "SwitchDevice: deviceObjecsList size = " + deviceObjectsList.size());

		// Get widget object
		WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

		// Check if deviceObjecsList exists
		if (deviceObjectsList == null) {
			return;
		}

		// Check if deviceObjecsList is empty
		if (deviceObjectsList.size() == 0) {
			Log.e(TAG, "deviceObjecsList.size == 0: ", new Exception());
			return;
		}

		// Check if a device has been removed
		if (deviceObjectsList.size() -1 < widgetObject.deviceIndex) {
			widgetObject.deviceIndex = 0;
		}

		// Move to the next or previous device in the deviceList.
		if (switchDirection.equals(getApplicationContext().getString(R.string.btn_next_tag))) {
			// Add 1 to deviceIndex
			if (widgetObject.deviceIndex + 1 > deviceObjectsList.size() - 1){
				widgetObject.deviceIndex = 0;
			} else {
				widgetObject.deviceIndex++;
			}
		} else {
			// Add -1 to deviceIndex
			if (widgetObject.deviceIndex - 1 < 0) {
				widgetObject.deviceIndex = deviceObjectsList.size() - 1;
			} else {
				widgetObject.deviceIndex--;
			}
		}

		//TODO: Do we want this?
		// Show mode selection screen by default after switching device
		widgetObject.showModeSelectionOverlay(getApplicationContext(), false);

		// Save and update the widgetObject
		widgetObject.saveToFile(getApplicationContext());

		WidgetContentManager.updateWidgetContent(getApplicationContext(), appWidgetId);
	}

	/**
	 * Handle action SwitchOff in provided background threat.
	 */
	private void handleActionSwitchOff(final int appWidgetId) {
		//Get the widget object.
		WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

		if (widgetObject.deviceStatus == null) {
			Log.e(TAG, "handleActionSwitchOff: widgetObject.deviceStatus == null");
			WidgetService.busy = false;
			return;
		}

		//Turn off the AC.
		turnDeviceOff(getApplicationContext(), appWidgetId, widgetObject.device);
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
				widgetObject.deviceStatus.getMode().setModeName("off");
				widgetObject.saveAndUpdate(context);

				Toast.makeText(context, confirmToast, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onFailure(ReturnObject result) {
                Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
				Log.d(TAG, result.errorMessage + ": " + result.exception);
			}

			@Override
			public void onFinish(ReturnObject result) {
				// Get widget object.
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

				//Update loading animation state of power button.
				widgetObject.setPowerBtnIsLoading(false);
				widgetObject.saveAndUpdate(context);

				WidgetService.busy = false;
			}

		}, preferredDevice).execute();

	}

	/**
	 * Handle action SwitchMode in the provided background threat.
	 */
	private void handleActionSwitchMode(int appWidgetId, String newMode) {
		// Get widget object.
		WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

		if (newMode.equals("modeSelection")){
			// Show mode selection overlay.
			widgetObject.showModeSelectionOverlay(getApplicationContext(), true);
			widgetObject.saveAndUpdate(getApplicationContext());

		} else if (newMode.equals("comfort") || newMode.equals("temperature")){
			this.updateDeviceMode(getApplicationContext(), appWidgetId, widgetObject.device, newMode);
		}
	}

	/**
	 * Update the device mode.
	 * @param preferredDevice
	 */
	private void updateDeviceMode(final Context context, final int appWidgetId, DeviceObject preferredDevice, final String mode) {

		new DataManager.UpdateModeTask(context, new OnProcessFinish<ReturnObject>() {

			@Override
			public void onSuccess(ReturnObject result) {
				String confirmToast = "Device is now in " + mode + " mode.";
				Log.d(TAG, confirmToast);

				// Change mode icon
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);
				widgetObject.deviceStatus.getMode().setModeName(mode);
				widgetObject.saveAndUpdate(context);

				Toast.makeText(context, confirmToast, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onFailure(ReturnObject result) {
                Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
				Log.d(TAG, result.errorMessage + ": " + result.exception);
			}

			@Override
			public void onFinish(ReturnObject result) {
				// Get widget object.
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

				//Update loading animation state of pressed mode button
				widgetObject.showModeSelectionOverlay(getApplicationContext(), false);
				widgetObject.setModeButtonIsLoading(false, mode);
				widgetObject.saveAndUpdate(context);

				WidgetService.busy = false;
			}

		}, preferredDevice, mode, 0, false).execute();

	}
}
