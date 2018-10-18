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
	 * A class that can be be used to create an instance that counts finished tasks. (For multiple async tasks at the same time)
	 */
    private static class WorkCounter {
        private int runningTasks;
        private final Context context;
        private final int appWidgetId;
        private RemoteViews views;
        private Date startingTime;

        public WorkCounter(int numberOfTasks, Context context, final int appWidgetId, RemoteViews views) {
            this.runningTasks = numberOfTasks;
            this.context = context;
            this.appWidgetId = appWidgetId;
            this.views = views;
			this.startingTime = new Date();
        }
        // Only call this in onPostExecute! (or add synchronized to method declaration)
        public void taskFinished() {
            if (--runningTasks == 0) {
				WidgetUtils.updateRefreshAnimation(false, views);
				AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);

				// TODO: Check time improvement compared to new endpoint.
				long difference = new Date().getTime() - this.startingTime.getTime();
				Log.d(TAG, "Tasks DONE: Took "+difference+" miliseconds!");
            }
        }
    }

	/**
	 * Updates the the widget remoteView object with NEW data.
	 */
    public static void updateAllViews(final Context context, final RemoteViews views, final int appWidgetId) {
        DeviceObject deviceObject;
        final DeviceObject defaultDeviceObject = WidgetUtils.getDefaultDevice(context);
        final DeviceObject preferredDeviceObject = WidgetUtils.getPreferredDevice(context);
        WidgetContentManager.prefTempScale = WidgetUtils.getTempScalePreference(context);
        final String value_celsius = context.getString(R.string.pref_tempScale_value_celsius);
        final String value_fahrenheit = context.getString(R.string.pref_tempScale_value_fahrenheit);

        //Use default device if no preferred device is selected.
        if (preferredDeviceObject == null){
            if (defaultDeviceObject == null){
                return;
            }
            deviceObject = defaultDeviceObject;
        } else {
            deviceObject = preferredDeviceObject;
        }

        // Fill content (local values)
        fillView(new ReturnObject(deviceObject), "ROOM", context, views, appWidgetId, null, null);

        // Retrieve and fill data from API
        final WorkCounter workCounter = new WorkCounter(3, context, appWidgetId, views);

        new DataManager.GetTemperatureTask(context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                fillView(result, "TEMP", context, views, appWidgetId, value_celsius, value_fahrenheit);
                workCounter.taskFinished();
            }

            @Override
            public void onFailure(ReturnObject result) {
                Log.d(TAG, result.errorMessage + ": " + result.exception);
                workCounter.taskFinished();
            }
        }, deviceObject).execute();

        // Get humidity
        new DataManager.GetHumidityTask(context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                fillView(result, "HUMID", context, views, appWidgetId, null, null);
                workCounter.taskFinished();
            }

            @Override
            public void onFailure(ReturnObject result) {
                Log.d(TAG, result.errorMessage + ": " + result.exception);
                workCounter.taskFinished();
            }
        }, deviceObject).execute();

        //Get the current mode of the device.
        new DataManager.GetModeTask(context, new OnProcessFinish<ReturnObject>() {
            @Override
            public void onSuccess(ReturnObject result) {
                String confirmToast = "Current Mode: result.modeObject.mode = " + result.modeObject.mode();
                Log.d(TAG, confirmToast);
                fillView(result, "MODE", context, views, appWidgetId, null, null);
                workCounter.taskFinished();
            }
            @Override
            public void onFailure(ReturnObject result) {
//                Toast.makeText(context, "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, result.errorMessage + ": " + result.exception);
                workCounter.taskFinished();
            }
        }, deviceObject).execute();
    }

	/**
	 * Fills a view of a remoteViews object with content.
	 */
    private static void fillView(ReturnObject result, String TAG, Context context, RemoteViews views, int appWidgetId, String value_celsius, String value_fahrenheit) {

        switch (TAG){

        	// Temperature
            case "TEMP":
                Double temperature = WidgetUtils.roundOneDecimal(Double.parseDouble(result.value));
                if(prefTempScale.equals(value_celsius)){
                    views.setTextViewText(R.id.temperature_text, temperature + "\u00B0");
                }
                else if (prefTempScale.equals(value_fahrenheit)){
                    Double tempFahrenheit = WidgetUtils.roundOneDecimal(WidgetUtils.convertToFahrenheit(temperature));
                    views.setTextViewText(R.id.temperature_text, tempFahrenheit + "\u00B0");
                }
                break;

			// Humidity
            case "HUMID":
                Double humidity = WidgetUtils.roundOneDecimal(Double.parseDouble(result.value));
                views.setTextViewText(R.id.humidity, humidity + "%");
                break;

			// Current Mode
            case "MODE":
                // Check if mode is manual
				String mode = result.modeObject.mode();

				switch (mode) {

					case "Manual":
						// Check if AC is ON/OFF
						if (result.applianceStateObject.power().equals("Off")) {
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
                break;

			// Device Name
            case "ROOM":
                String roomName = result.deviceObject.roomName();
                views.setTextViewText(R.id.roomName, roomName);
                break;
        }
    }
}
