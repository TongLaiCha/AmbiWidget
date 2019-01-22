package brandonmilan.tonglaicha.ambiwidget.services;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.R;
import brandonmilan.tonglaicha.ambiwidget.WidgetContentManager;
import brandonmilan.tonglaicha.ambiwidget.WidgetProvider;
import brandonmilan.tonglaicha.ambiwidget.WidgetStorageManager;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.objects.WidgetObject;
import brandonmilan.tonglaicha.ambiwidget.utils.Utils;
import brandonmilan.tonglaicha.ambiwidget.utils.WidgetUtils;

/**
 * Class for handling tasks in a background thread.
 */
public class WidgetService extends JobIntentService {
	private static final String TAG = "WidgetService";

	public static final String ACTION_GIVE_FEEDBACK =
			"brandonmilan.tonglaicha.ambiwidget.action.give_feedback";
	public static final String EXTRA_FEEDBACK_TAG =
			"brandonmilan.tonglaicha.ambiwidget.extra.ACTION_TAG";
	public static final String ACTION_UPDATE_WIDGET =
			"brandonmilan.tonglaicha.ambiwidget.action.update_widget";
	public static final String ACTION_SUPER_UPDATE_WIDGET =
			"brandonmilan.tonglaicha.ambiwidget.action.super_update_widget";
	public static final String EXTRA_UPDATE_BY_USER =
			"brandonmilan.tonglaicha.ambiwidget.action.super_update_widget";
	public static final String ACTION_SWITCH_OFF =
			"brandonmilan.tonglaicha.ambiwidget.action.switch_off";
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
	public static final String ACTION_ADJUST_TEMPERATURE =
			"brandonmilan.tonglaicha.ambiwidget.action.adjust_temperature";
	public static final String EXTRA_ADJUST_TYPE =
			"brandonmilan.tonglaicha.ambiwidget.extra.adjust_type";

	public static Boolean busy = false;
	private static int timeRemaining = 0;
	Timer timer = new Timer();
	private static Boolean timerHasStarted = false;
	private static double finalNewTemp;

	/**
	 * Starts this service to perform UpdateWidget action with the given parameters.
	 * If the service is already performing a task, this action will be queued.
	 */
	public static void startActionUpdateWidget(Context context, int appWidgetId, Boolean updateByUser) {
		PendingIntent pendingIntent = WidgetUtils.getUpdatePendingIntent(context, appWidgetId, updateByUser);
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
			if (action.equals(ACTION_GIVE_FEEDBACK) ||
					action.equals(ACTION_SWITCH_OFF) ||
					action.equals(ACTION_SWITCH_DEVICE) ||
					action.equals(ACTION_SWITCH_MODE) ||
					action.equals(ACTION_UPDATE_WIDGET) ||
					action.equals(ACTION_ADJUST_TEMPERATURE)) {
				if (WidgetService.busy || (timerHasStarted && !action.equals(ACTION_ADJUST_TEMPERATURE))) {
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
			else if(WidgetService.ACTION_SWITCH_OFF.equals(action)) {
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
				boolean updateByUser = intent.getBooleanExtra(EXTRA_UPDATE_BY_USER, false);
				handleActionUpdateWidget(appWidgetId, updateByUser);
			} else if (ACTION_SUPER_UPDATE_WIDGET.equals(action)) {
				handleActionSuperUpdateWidget();
			} else if(ACTION_SWITCH_OFF.equals(action)) {
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
			} else if (ACTION_ADJUST_TEMPERATURE.equals(action)) {
				final int appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
				final String adjustType = intent.getStringExtra(EXTRA_ADJUST_TYPE);
				handleActionAdjustTemperature(appWidgetId, adjustType);

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

				// Get the widget object from file storage
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

				// Show the disconnected icon if the device is disconnected.
				if (WidgetUtils.checkDeviceIsOnline(result)){
					// Update the mode in the widgetObject to Comfort mode
					widgetObject.getDeviceStatus().getMode().setModeName("comfort");
				} else {
					widgetObject.getDeviceStatus().getMode().setModeName("disconnected");
					confirmToast = "The device is currently offline.";
				}

				Toast.makeText(getApplicationContext(), confirmToast, Toast.LENGTH_LONG).show();

				// Update the prediction object in the widgetObject to Comfort level (for border update)
				widgetObject.getDeviceStatus().getComfortPrediction().setLevelByTag(feedbackTag);

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
	private void handleActionUpdateWidget(int appWidgetId, boolean updateByUser) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		WidgetProvider.updateWidget(this, appWidgetManager, appWidgetId, updateByUser);
	}

	/**
	 * Handle action superUpdateWidget in the provided background threat.
	 * Updates the device list and also update all widgets.
	 * * NOTE: This should only be used on first time setup when there is no deviceList yet.
	 */
	private void handleActionSuperUpdateWidget() {
		WidgetContentManager.updateDeviceListAndAllWidgets(getApplicationContext());
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

		// Show mode selection screen by default after switching device
		widgetObject.setShowModeSelectionOverlay(false);

		// Save and update the widgetObject
		widgetObject.saveToFile(getApplicationContext());

		WidgetContentManager.updateWidgetContent(getApplicationContext(), appWidgetId, true);
	}

	/**
	 * Handle action SwitchOff in provided background threat.
	 */
	private void handleActionSwitchOff(final int appWidgetId) {
		//Get the widget object.
		WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

		if (widgetObject.getDeviceStatus() == null) {
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

				// Show the disconnected icon if the device is disconnected.
				if (WidgetUtils.checkDeviceIsOnline(result)){
					// Update the mode in the widgetObject to Off mode
					widgetObject.getDeviceStatus().getMode().setModeName("off");
				} else {
					widgetObject.getDeviceStatus().getMode().setModeName("disconnected");
					confirmToast = "The device is currently offline.";
				}

				widgetObject.saveAndUpdate(context);

				Toast.makeText(context, confirmToast, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onFailure(ReturnObject result) {
                Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
				Log.d(TAG, result.errorMessage + ": " + result.exception);
				if (result.exception.getMessage().equals("ERROR_SERVICE_UNAVAILABLE")) {

					// Change mode icon
					WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);
					widgetObject.getDeviceStatus().getMode().setModeName("disconnected");
					String confirmToast = "The device is currently offline.";

					widgetObject.saveAndUpdate(context);

					Toast.makeText(context, confirmToast, Toast.LENGTH_LONG).show();
				}
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
			widgetObject.setShowModeSelectionOverlay(true);
			widgetObject.saveAndUpdate(getApplicationContext());
			WidgetService.busy = false;

		} else if (newMode.equals("comfort") || newMode.equals("temperature")){
			this.updateDeviceMode(getApplicationContext(), appWidgetId, widgetObject.device, newMode);
		}
	}

	/**
	 * Handle action AdjustTemperature in the provided background threat.
	 */
	private void handleActionAdjustTemperature(final int appWidgetId, String adjustType) {
		// Get widget object.
		final WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

		// Get the preferred tempScale
//		String prefTempScale = WidgetUtils.getTempScalePreference(getApplicationContext());

		// Get current preferred device temp
		double preferredTemperature = widgetObject.getPreferredTemperature();
		double newTemp = preferredTemperature;

		double maxTemp = 32;
		double minTemp = 18;

		if (adjustType.equals("add")) {
			// Check if temp exceeds maximum
			if (preferredTemperature + 0.5 > maxTemp) {
				WidgetService.busy = false;
				return;
			} else {
				// Add temp
				newTemp = preferredTemperature + 0.5;
			}
		} else if (adjustType.equals("decrease")) {
			// Check if temp exceeds minimum
			if (preferredTemperature - 0.5 < minTemp) {
				WidgetService.busy = false;
				return;
			} else {
				// Decrease temp
				newTemp = preferredTemperature - 0.5;
			}
		}

		// Update preferred device temp
		widgetObject.setPreferredTemperature(newTemp);
		widgetObject.saveAndUpdate(getApplicationContext());
		WidgetService.busy = false;

		finalNewTemp = newTemp;

		// Reset the timer
		WidgetService.timeRemaining = 1500;

		if (!timerHasStarted) {
			startTimerAdjustTemperature(appWidgetId, widgetObject);
		}
	}

	/**
	 * Timer to prevent unnecessary API calls to adjust the temperature.
	 * The API call to adjust the preferred temperature will only be executed if
	 * the user has not clicked the buttons for adjusting the preferred temperature in the last 2 seconds.
	 */
	private void startTimerAdjustTemperature(final int appWidgetId, final WidgetObject widgetObject) {
		WidgetService.timerHasStarted = true;

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				timeRemaining -= 100;
				if (timeRemaining <= 0) {
					updatePreferredTemperature(getApplicationContext(), appWidgetId, widgetObject.device, WidgetService.finalNewTemp);
					WidgetService.timerHasStarted = false;
				} else {
					startTimerAdjustTemperature(appWidgetId, widgetObject);
				}
			}
		}, 100);

	}

	/**
	 * Update the device mode.
	 * @param preferredDevice
	 */
	private void updateDeviceMode(final Context context, final int appWidgetId, final DeviceObject preferredDevice, final String mode) {

		// preferredTemperature is 20 when it's set to temperature mode for the first time,
		final double preferredTemperature = WidgetObject.defaultTemperatureForTemperatureMode;

		WidgetService.busy = true;

		new DataManager.UpdateModeTask(context, new OnProcessFinish<ReturnObject>() {

			@Override
			public void onSuccess(ReturnObject result) {
				String confirmToast = "Device is now in " + mode + " mode.";

				// Get the widget object from file storage
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(getApplicationContext(), appWidgetId);

				// Set the mode icon.
				if (WidgetUtils.checkDeviceIsOnline(result)){
					// Update the mode in the widgetObject to Comfort mode
					widgetObject.getDeviceStatus().getMode().setModeName(mode);
				} else {
					widgetObject.getDeviceStatus().getMode().setModeName("disconnected");
					confirmToast = "The device is currently offline.";
				}

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
				widgetObject.setShowModeSelectionOverlay(false);
				widgetObject.setDesiredTemperatureIsLoading(false);
				widgetObject.setPreferredTemperature(preferredTemperature);
				widgetObject.setModeButtonIsLoading(false, mode);
				widgetObject.saveAndUpdate(context);

				WidgetService.busy = false;
			}

		}, preferredDevice, mode, preferredTemperature, false).execute();

	}
	/**
	 * Update the device mode.
	 * @param preferredDevice
	 */
	private void updatePreferredTemperature(final Context context, final int appWidgetId,
											final DeviceObject preferredDevice, final double preferredTemperature) {

		WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);
		// Show loading animation around desired temperature number when updating temperature
		widgetObject.setDesiredTemperatureIsLoading(true);
		widgetObject.saveAndUpdate(context);
		WidgetService.busy = true;

		new DataManager.UpdateModeTask(context, new OnProcessFinish<ReturnObject>() {

			@Override
			public void onSuccess(ReturnObject result) {
				// Change mode icon
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);
				widgetObject.setPreferredTemperature(preferredTemperature);
				widgetObject.saveAndUpdate(context);

				String confirmToast = "Desired temperature set to " + preferredTemperature;

				// Get the preferred tempScale
				String prefTempScale = WidgetUtils.getTempScalePreference(context);

				// Set the confirmToast to display temperature in fahrenheit if this is the preferred tempScale
				if(prefTempScale.equals(context.getString(R.string.pref_tempScale_value_fahrenheit))) {
					Double tempFahrenheit = Utils.roundOneDecimal(Utils.convertToFahrenheit(preferredTemperature));
					confirmToast = "Desired temperature set to " + tempFahrenheit;
				}

				Log.d(TAG, confirmToast);
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
				widgetObject.setShowModeSelectionOverlay(false);
				widgetObject.setDesiredTemperatureIsLoading(false);
				widgetObject.saveAndUpdate(context);

				WidgetService.busy = false;
			}

		}, preferredDevice, "temperature", preferredTemperature, false).execute();

	}

}
