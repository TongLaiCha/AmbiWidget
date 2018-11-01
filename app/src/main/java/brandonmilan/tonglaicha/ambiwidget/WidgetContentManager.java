package brandonmilan.tonglaicha.ambiwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.objects.WidgetObject;
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

		// Get the device status data
		new DataManager.GetDeviceStatusTask(context, new OnProcessFinish<ReturnObject>() {

			@Override
			public void onSuccess(ReturnObject result) {
				Log.d(TAG, "GetDeviceStatusTask success: "+result.deviceStatusObject);
				// Get widget object
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);

				// DeviceObject
				widgetObject.device = deviceObject;

				// Device status
				widgetObject.deviceStatus = result.deviceStatusObject;

				// Disable refresh button loading
				widgetObject.setRefreshBtnIsLoading(false);

				// Save new widgetObject
				widgetObject.saveAndUpdate(context);
			}

			@Override
			public void onFailure(ReturnObject result) {
				Log.d(TAG, result.errorMessage + ": " + result.exception);

				// Get widget object
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);

				// Disable refresh button loading
				widgetObject.setRefreshBtnIsLoading(false);

				// Save new widgetObject
				widgetObject.saveAndUpdate(context);
			}
		}, deviceObject).execute();

		// Get the device list.
		new DataManager.GetDeviceListTask(context, new OnProcessFinish<ReturnObject>() {

			@Override
			public void onSuccess(ReturnObject result) {
				List<DeviceObject> deviceList = result.deviceList;

				// Get widget object
				WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);

				// TODO: Save device to file.

//				DeviceObject preferredDevice = WidgetUtils.getPreferredDevice(context);
			}

			@Override
			public void onFailure(ReturnObject result) {
                Toast.makeText(context, "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
				Log.d(TAG, result.errorMessage + ": " + result.exception);
			}
		}).execute();
	}
}
