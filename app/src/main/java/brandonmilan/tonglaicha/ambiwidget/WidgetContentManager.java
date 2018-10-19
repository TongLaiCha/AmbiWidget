package brandonmilan.tonglaicha.ambiwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Date;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceStatusObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.utils.WidgetUtils;

/**
 * (CONTROLLER)
 * Manager for all content in the widget.
 */
public class WidgetContentManager {
	private static final String TAG = "WidgetContentManager";
	private static String prefTempScale;

	private WidgetContentManager(){
	}

	/**
	 * Updates the the widget remoteView object with NEW data.
	 */
	public static void updateWidgetContent(final Context context, final int appWidgetId) {
		final DeviceObject deviceObject;
		final DeviceObject defaultDeviceObject = WidgetUtils.getDefaultDevice(context);
		final DeviceObject preferredDeviceObject = WidgetUtils.getPreferredDevice(context);
		WidgetContentManager.prefTempScale = WidgetUtils.getTempScalePreference(context);

		//Use default device if no preferred device is selected.
		if (preferredDeviceObject == null){
			if (defaultDeviceObject == null){
				return;
			}
			deviceObject = defaultDeviceObject;
		} else {
			deviceObject = preferredDeviceObject;
		}

		// Get removeViews object
		final RemoteViews views = WidgetUtils.getRemoteViewsByWidgetId(appWidgetId);

		new DataManager.GetDeviceStatusTask(context, new OnProcessFinish<ReturnObject>() {

			@Override
			public void onSuccess(ReturnObject result) {
				Log.d(TAG, "GetDeviceStatusTask success: "+result.deviceStatusObject);

				// Add device object to result
				result.deviceObject = deviceObject;

				// Fill views with the new data
				fillViews(result, context, appWidgetId);

				// Update the widget to display the new data
				WidgetUtils.updateRefreshAnimation(false, views);
				AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
			}

			@Override
			public void onFailure(ReturnObject result) {
				Log.d(TAG, result.errorMessage + ": " + result.exception);
			}
		}, deviceObject).execute();
	}

	/**
	 * Fills a view of a remoteViews object with content.
	 */
	private static void fillViews(ReturnObject data, Context context, int appWidgetId) {
		// Get removeViews object
		RemoteViews views = WidgetUtils.getRemoteViewsByWidgetId(appWidgetId);

		DeviceObject deviceObject = data.deviceObject;
		DeviceStatusObject deviceStatusObject = data.deviceStatusObject;

		// Device Name
		String roomName = deviceObject.roomName();
		views.setTextViewText(R.id.roomName, roomName);

		// Sensor data
		Double temperature = WidgetUtils.roundOneDecimal(deviceStatusObject.getSensorDataObject().getTemperature());
		if(prefTempScale.equals(context.getString(R.string.pref_tempScale_value_celsius))){
			views.setTextViewText(R.id.temperature_text, temperature + "\u00B0");
		}
		else if (prefTempScale.equals(context.getString(R.string.pref_tempScale_value_fahrenheit))){
			Double tempFahrenheit = WidgetUtils.roundOneDecimal(WidgetUtils.convertToFahrenheit(temperature));
			views.setTextViewText(R.id.temperature_text, tempFahrenheit + "\u00B0");
		}

		// Humidity
		Double humidity = WidgetUtils.roundOneDecimal(deviceStatusObject.getSensorDataObject().getHumdiity());
		views.setTextViewText(R.id.humidity, humidity + "%");

		// Mode
		String mode = deviceStatusObject.getModeObject().mode();

		updateModeIcon(appWidgetId, mode, deviceStatusObject);
	}

	public static void updateModeIcon(int appWidgetId, String mode, DeviceStatusObject deviceStatusObject) {
		// Get removeViews object
		RemoteViews views = WidgetUtils.getRemoteViewsByWidgetId(appWidgetId);

		switch (mode) {

			case "Manual":
				// Check if AC is ON/OFF
				if (deviceStatusObject.getApplianceStateObject().power().equals("Off")) {
					// Show OFF Icon
					views.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_off);
				} else {
					// Show MANUAL icon
					views.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_manual);
					// TODO: Change button layout to MANUAL MODE
					// TODO: Read result.applianceStateObject.temperature to check current MANUAL TEMPERATURE TARGET
				}
				break;

			case "Comfort":
				// Show COMFORT Icon
				views.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_comfort);
				// TODO: Check current comfort mode AI predicted feedback and update comfort button states.
				break;

			case "Off":
				// Show OFF Icon
				views.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_off);
				break;

			case "Temperature":
				// Show TEMPERATURE Icon
				views.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_temperature);
				break;

			case "Away_Humidity_Upper":
				// Show AWAY Icon
				views.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_away);
				// TODO: Read result.modeObject.value and update corresponding textview
				break;

			case "Away_Temperature_Lower":
				// Show AWAY Icon
				views.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_away);
				// TODO: Read result.modeObject.value and update corresponding textview
				break;

			case "Away_Temperature_Upper":
				// Show AWAY Icon
				views.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_away);
				// TODO: Read result.modeObject.value and update corresponding textview
				break;
		}
	}
}
