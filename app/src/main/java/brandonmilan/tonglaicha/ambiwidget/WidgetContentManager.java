package brandonmilan.tonglaicha.ambiwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceStatusObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.objects.WidgetObject;
import brandonmilan.tonglaicha.ambiwidget.utils.Utils;
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
				widgetObject.refreshBtnIsLoading = false;

				// Save new widgetObject
				widgetObject.saveToFile(context);
        
				// Tell android to update the widget
				AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, widgetObject.getRemoteViews(context));
			}

			@Override
			public void onFailure(ReturnObject result) {
				Log.d(TAG, result.errorMessage + ": " + result.exception);
			}
		}, deviceObject).execute();
	}
}
