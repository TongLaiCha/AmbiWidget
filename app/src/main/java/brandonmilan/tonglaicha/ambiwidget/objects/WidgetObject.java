package brandonmilan.tonglaicha.ambiwidget.objects;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.io.Serializable;
import java.util.ArrayList;

import brandonmilan.tonglaicha.ambiwidget.R;
import brandonmilan.tonglaicha.ambiwidget.WidgetStorageManager;
import brandonmilan.tonglaicha.ambiwidget.activities.SettingsActivity;
import brandonmilan.tonglaicha.ambiwidget.utils.Utils;
import brandonmilan.tonglaicha.ambiwidget.utils.WidgetUtils;

public class WidgetObject implements Serializable {

	private static final String TAG = WidgetObject.class.getSimpleName();
	public int widgetId;
	public DeviceObject device;
	public int deviceIndex = 0;
	public DeviceStatusObject deviceStatus;
	private Boolean refreshBtnIsLoading = false;
	private Boolean tooWarmBtnIsLoading = false;
	private Boolean bitWarmBtnIsLoading = false;
	private Boolean comfortableBtnIsLoading = false;
	private Boolean bitColdBtnIsLoading = false;
	private Boolean tooColdBtnIsLoading = false;
	private Boolean powerBtnIsLoading = false;
	private Boolean showModeSelectionOverlay = false;

	public void showModeSelectionOverlay(Context context, Boolean state) {
		Log.d(TAG, "showModeSelectionOverlay: " + state);
		this.showModeSelectionOverlay = state;
	}

	public WidgetObject(int widgetId, DeviceObject deviceObject, DeviceStatusObject deviceStatusObject) {
		this.widgetId = widgetId;
		this.device = deviceObject;
		this.deviceStatus = deviceStatusObject;
	}

	public void saveToFile(Context context) {
		WidgetStorageManager.setWidgetObjectByWidgetId(context, widgetId, this);

	}

	public void saveAndUpdate(Context context) {
		this.saveToFile(context);

		//Partially update the widget.
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(widgetId, this.getRemoteViews(context));
	}

	public RemoteViews getRemoteViews(Context context) {
		// Set loading overlay as default layout
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_loading_overlay);

		// If this widgetObject does not contain a device and / or devicestatus object yet, return loading overlay remoteview.
		if (this.device == null || this.deviceStatus == null) {
			return remoteViews;
		}

		String modeName = deviceStatus.getMode().getModeName();

		// Create new remoteViews from widget layout
		if (this.showModeSelectionOverlay || WidgetUtils.checkIsModeOff(this.deviceStatus)) {
			remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_mode_selection);
		} else if (modeName.equals("comfort")) {
			remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_comfort_mode);
		} else if (modeName.equals("temperature")) {
			remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_temperature_mode);
		}

		// Add listeners to buttons
		setButtonClickHandlers(context, remoteViews);

		// Check & update refresh animation of refresh button
		// Check & update feedback button loading animations
		updateButtons(context, remoteViews);

		// Device Name
		String deviceName = device.roomName();
		if (deviceName.length() > 20) {
			deviceName = deviceName.substring(0, 20) + "\u2026";
		}
		remoteViews.setTextViewText(R.id.device_name, deviceName);

		// Temperature
		String prefTempScale = WidgetUtils.getTempScalePreference(context);
		Double temperature = Utils.roundOneDecimal(deviceStatus.getSensorData().getTemperature());
		if(prefTempScale.equals(context.getString(R.string.pref_tempScale_value_celsius))){
			remoteViews.setTextViewText(R.id.temperature_text, temperature + "\u00B0");
		}
		else if (prefTempScale.equals(context.getString(R.string.pref_tempScale_value_fahrenheit))){
			Double tempFahrenheit = Utils.roundOneDecimal(Utils.convertToFahrenheit(temperature));
			remoteViews.setTextViewText(R.id.temperature_text, tempFahrenheit + "\u00B0");
		}

		// Humidity
		Double humidity = Utils.roundOneDecimal(deviceStatus.getSensorData().getHumidity());
		remoteViews.setTextViewText(R.id.humidity, humidity + "%");

		// Mode
		updateModeIcon(modeName, deviceStatus, remoteViews);

		return remoteViews;
	}

	public void setPowerBtnIsLoading(Boolean enabled) {
		this.powerBtnIsLoading = enabled;
	}

	public void setRefreshBtnIsLoading(Boolean enabled) {
		this.refreshBtnIsLoading = enabled;
	}

	public void setFeedbackBtnLoadingState(String feedbackTag, Boolean enabled) {
		switch(feedbackTag) {
			case "too_warm":
				this.tooWarmBtnIsLoading = enabled;
				break;
			case "bit_warm":
				this.bitWarmBtnIsLoading = enabled;
				break;
			case "comfortable":
				this.comfortableBtnIsLoading = enabled;
				break;
			case "bit_cold":
				this.bitColdBtnIsLoading = enabled;
				break;
			case "too_cold":
				this.tooColdBtnIsLoading = enabled;
				break;
			case "ALL":
				this.tooWarmBtnIsLoading = enabled;
				this.bitWarmBtnIsLoading = enabled;
				this.comfortableBtnIsLoading = enabled;
				this.bitColdBtnIsLoading = enabled;
				this.tooColdBtnIsLoading = enabled;
				break;
		}
	}

	/**
	 * Set all click handlers for the widgets buttons.
	 */
	private void setButtonClickHandlers(Context context, RemoteViews remoteViews) {
		if (this.showModeSelectionOverlay || WidgetUtils.checkIsModeOff(this.deviceStatus)) {
			// Set onClickPendingIntent for on/off button.
			remoteViews.setOnClickPendingIntent(R.id.button_comfort_mode, WidgetUtils.getSwitchModePendingIntent(context, widgetId, "comfort"));
			remoteViews.setOnClickPendingIntent(R.id.button_temperature_mode, WidgetUtils.getSwitchModePendingIntent(context, widgetId, "temperature"));
			remoteViews.setOnClickPendingIntent(R.id.button_manual_mode, WidgetUtils.getSwitchModePendingIntent(context, widgetId, "manual"));
			remoteViews.setOnClickPendingIntent(R.id.button_on_off, WidgetUtils.getSwitchPowerPendingIntent(context, widgetId));
		} else {
			// Set onClickPendingIntents for all the feedback buttons.
			remoteViews.setOnClickPendingIntent(R.id.button_too_cold, WidgetUtils.getGiveFeedbackPendingIntent(context, widgetId, context.getString(R.string.too_cold_tag)));
			remoteViews.setOnClickPendingIntent(R.id.button_bit_cold, WidgetUtils.getGiveFeedbackPendingIntent(context, widgetId, context.getString(R.string.bit_cold_tag)));
			remoteViews.setOnClickPendingIntent(R.id.button_comfy, WidgetUtils.getGiveFeedbackPendingIntent(context, widgetId, context.getString(R.string.comfy_tag)));
			remoteViews.setOnClickPendingIntent(R.id.button_bit_warm, WidgetUtils.getGiveFeedbackPendingIntent(context, widgetId, context.getString(R.string.bit_warm_tag)));
			remoteViews.setOnClickPendingIntent(R.id.button_too_warm, WidgetUtils.getGiveFeedbackPendingIntent(context, widgetId, context.getString(R.string.too_warm_tag)));
		}

		//Set onClickPendingIntent for mode button.
		remoteViews.setOnClickPendingIntent(R.id.button_mode, WidgetUtils.getSwitchModePendingIntent(context, widgetId, "modeSelection"));

		// Set onClickPendingIntent for prev/next device buttons.
		remoteViews.setOnClickPendingIntent(R.id.device_previous, WidgetUtils.getSwitchDevicePendingIntent(context, widgetId, context.getString(R.string.btn_previous_tag)));
		remoteViews.setOnClickPendingIntent(R.id.device_next, WidgetUtils.getSwitchDevicePendingIntent(context, widgetId, context.getString(R.string.btn_next_tag)));

		// Set onClickPendingIntent for the settings button.
		Intent configIntent = new Intent(context, SettingsActivity.class);

		// WARNING: Include the widget ID with the pendingIntent, or the configuration activity will not be opened.
		configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		PendingIntent configPendingIntent = PendingIntent.getActivity(context, widgetId, configIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.button_settings, configPendingIntent);

		// Set onClickPendingIntent for the refresh button.
		remoteViews.setOnClickPendingIntent(R.id.button_refresh, WidgetUtils.getUpdatePendingIntent(context, widgetId));
	}

	/**
	 * Update animation states for all buttons on the widget.
	 */
	private void updateButtons(Context context, RemoteViews remoteViews) {
		// Update refresh button animation.
		if (refreshBtnIsLoading) {
			remoteViews.setViewVisibility(R.id.button_refresh, View.INVISIBLE);
			remoteViews.setViewVisibility(R.id.progressBar, View.VISIBLE);
		} else {
			remoteViews.setViewVisibility(R.id.button_refresh, View.VISIBLE);
			remoteViews.setViewVisibility(R.id.progressBar, View.INVISIBLE);
		}

		//TODO: This should be in mode selection screen
		// Update power button animation.
		if(powerBtnIsLoading) {
			remoteViews.setViewVisibility(R.id.button_on_off, View.GONE);
			remoteViews.setViewVisibility(R.id.progress_on_off, View.VISIBLE);
		} else {
			remoteViews.setViewVisibility(R.id.button_on_off, View.VISIBLE);
			remoteViews.setViewVisibility(R.id.progress_on_off, View.GONE);
		}

		// Get currently selected / predicted comfort feedback
		String predictedComfortTag = deviceStatus.getComfortPrediction().levelAsTag(context);

		// Update comfort feedback buttons
		ArrayList<String> buttonsTags =  new ArrayList<>();
		buttonsTags.add(context.getString(R.string.too_warm_tag));
		buttonsTags.add(context.getString(R.string.bit_warm_tag));
		buttonsTags.add(context.getString(R.string.comfy_tag));
		buttonsTags.add(context.getString(R.string.bit_cold_tag));
		buttonsTags.add(context.getString(R.string.too_cold_tag));

		for(String buttonTag : buttonsTags){
			updateFeedbackButtons(remoteViews, buttonTag, predictedComfortTag);
		}
	}

	/**
	 * Display loading animation and border when the user presses a feedback button.
	 * @param comfortTag
	 */
	private void updateFeedbackButtons(RemoteViews remoteViews, String comfortTag, String predictedComfortTag) {

		//TODO: Only update the comfort prediction border when device is in comfort mode

		if(comfortTag != null){
			switch (comfortTag){
				case "too_warm":
					if (this.tooWarmBtnIsLoading) {
						remoteViews.setViewVisibility(R.id.button_too_warm, View.GONE);
						remoteViews.setViewVisibility(R.id.progress_too_warm, View.VISIBLE);
					} else {
						remoteViews.setViewVisibility(R.id.button_too_warm, View.VISIBLE);
						remoteViews.setViewVisibility(R.id.progress_too_warm, View.GONE);
					}

					if (comfortTag.equals(predictedComfortTag)) {
						remoteViews.setInt(R.id.container_btn_too_warm, "setBackgroundResource", R.drawable.button_too_warm_border);
					} else {
						remoteViews.setInt(R.id.container_btn_too_warm, "setBackgroundResource", R.drawable.button_selector_too_warm);
					}
					break;
				case "bit_warm":
					if (this.bitWarmBtnIsLoading) {
						remoteViews.setViewVisibility(R.id.button_bit_warm, View.GONE);
						remoteViews.setViewVisibility(R.id.progress_bit_warm, View.VISIBLE);
					} else {
						remoteViews.setViewVisibility(R.id.button_bit_warm, View.VISIBLE);
						remoteViews.setViewVisibility(R.id.progress_bit_warm, View.GONE);
					}

					if (comfortTag.equals(predictedComfortTag)) {
						remoteViews.setInt(R.id.container_btn_bit_warm, "setBackgroundResource", R.drawable.button_bit_warm_border);
					} else {
						remoteViews.setInt(R.id.container_btn_bit_warm, "setBackgroundResource", R.drawable.button_selector_bit_warm);
					}
					break;
				case "comfortable":
					if (this.comfortableBtnIsLoading) {
						remoteViews.setViewVisibility(R.id.button_comfy, View.GONE);
						remoteViews.setViewVisibility(R.id.progress_comfy, View.VISIBLE);
					} else {
						remoteViews.setViewVisibility(R.id.button_comfy, View.VISIBLE);
						remoteViews.setViewVisibility(R.id.progress_comfy, View.GONE);
					}

					if (comfortTag.equals(predictedComfortTag)) {
						remoteViews.setInt(R.id.container_btn_comfy, "setBackgroundResource", R.drawable.button_comfy_border);
					} else {
						remoteViews.setInt(R.id.container_btn_comfy, "setBackgroundResource", R.drawable.button_selector_comfy);
					}
					break;
				case "bit_cold":
					if (this.bitColdBtnIsLoading) {
						remoteViews.setViewVisibility(R.id.button_bit_cold, View.GONE);
						remoteViews.setViewVisibility(R.id.progress_bit_cold, View.VISIBLE);
					} else {
						remoteViews.setViewVisibility(R.id.button_bit_cold, View.VISIBLE);
						remoteViews.setViewVisibility(R.id.progress_bit_cold, View.GONE);
					}

					if (comfortTag.equals(predictedComfortTag)) {
						remoteViews.setInt(R.id.container_btn_bit_cold, "setBackgroundResource", R.drawable.button_bit_cold_border);
					} else {
						remoteViews.setInt(R.id.container_btn_bit_cold, "setBackgroundResource", R.drawable.button_selector_bit_cold);
					}
					break;
				case "too_cold":
					if (this.tooColdBtnIsLoading) {
						remoteViews.setViewVisibility(R.id.button_too_cold, View.GONE);
						remoteViews.setViewVisibility(R.id.progress_too_cold, View.VISIBLE);
					} else {
						remoteViews.setViewVisibility(R.id.button_too_cold, View.VISIBLE);
						remoteViews.setViewVisibility(R.id.progress_too_cold, View.GONE);
					}

					if (comfortTag.equals(predictedComfortTag)) {
						remoteViews.setInt(R.id.container_btn_too_cold, "setBackgroundResource", R.drawable.button_too_cold_border);
					} else {
						remoteViews.setInt(R.id.container_btn_too_cold, "setBackgroundResource", R.drawable.button_selector_too_cold);
					}
					break;
			}
		} else {
			Log.e(TAG, "ERROR: in displayFeedbackButtonConfirmation, feedbackgiven = null", new Exception("ERROR_FEEDBACKGIVEN_IS_NULL"));
		}
	}

	private static void updateModeIcon(String mode, DeviceStatusObject deviceStatusObject, RemoteViews remoteViews) {
		switch (mode) {
			case "manual":
				// Check if AC is ON/OFF
				if (deviceStatusObject.getApplianceState().getPower().equals("off")) {
					// Show OFF Icon
					remoteViews.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_off);
				} else {
					// Show MANUAL icon
					remoteViews.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_manual);
					// TODO: Change button layout to MANUAL MODE
					// TODO: Read result.applianceStateObject.getTemperature to check current MANUAL TEMPERATURE TARGET
				}
				break;

			case "comfort":
				// Show COMFORT Icon
				remoteViews.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_comfort);
				// TODO: Check current comfort mode AI predicted feedback and update comfort button states.
				break;

			case "off":
				// Show OFF Icon
				remoteViews.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_off);
				break;

			case "temperature":
				// Show TEMPERATURE Icon
				remoteViews.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_temperature);
				break;

			case "away_Humidity_Upper":
				// Show AWAY Icon
				remoteViews.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_away);
				// TODO: Read result.modeObject.value and update corresponding textview
				break;

			case "away_Temperature_Lower":
				// Show AWAY Icon
				remoteViews.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_away);
				// TODO: Read result.modeObject.value and update corresponding textview
				break;

			case "away_Temperature_Upper":
				// Show AWAY Icon
				remoteViews.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_away);
				// TODO: Read result.modeObject.value and update corresponding textview
				break;
		}
	}
}
