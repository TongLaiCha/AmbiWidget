package brandonmilan.tonglaicha.ambiwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.objects.WidgetObject;
import brandonmilan.tonglaicha.ambiwidget.services.WidgetService;

/**
 * (CONTROLLER)
 * Manager for all content in the widget.
 */
public class WidgetContentManager {
    private static final String TAG = "WidgetContentManager";

    private WidgetContentManager() {
    }

    /**
     * Updates the the widget remoteView object with NEW data.
     */
    public static void updateWidgetContent(final Context context, final int appWidgetId) {
        List<DeviceObject> deviceObjectsList = WidgetStorageManager.getDeviceObjectsList(context);

        // Get widget object
        WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);

        // Check if deviceObjecsList exists
        if (deviceObjectsList == null) {
            Log.e(TAG, "Device list does not exist, can't update widget content.");
            updateDeviceList(context);
            return;
        }

        // Check if deviceObjecsList is empty
        if (deviceObjectsList.size() == 0) {
            Log.e(TAG, "Device list is empty, can't update widget content.");
            updateDeviceList(context);
            return;
        }

        // Check if a device has been removed
        if (deviceObjectsList.size() - 1 < widgetObject.deviceIndex) {
            widgetObject.deviceIndex = 0;
        }

        widgetObject.device = deviceObjectsList.get(widgetObject.deviceIndex);

        widgetObject.setRefreshBtnIsLoading(true);
        widgetObject.setShowModeSelectionOverlay(false);

        // Save and update to display loading animation
        widgetObject.saveAndUpdate(context);

        final DeviceObject deviceObject = deviceObjectsList.get(widgetObject.deviceIndex);

        // Get the device status data
        new DataManager.GetDeviceStatusTask(context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                Log.d(TAG, "GetDeviceStatusTask success: " + result.deviceStatusObject);
            }

            @Override
            public void onFailure(ReturnObject result) {
                Toast.makeText(context, "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }

            @Override
            public void onFinish(ReturnObject result) {

                // Get widget object
                WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);

                // DeviceObject
                widgetObject.device = deviceObject;

                // Device status
                if (result.deviceStatusObject != null) {
                    widgetObject.deviceStatus = result.deviceStatusObject;
                }

                // Disable refresh button loading
                widgetObject.setRefreshBtnIsLoading(false);

                // Save new widgetObject
                widgetObject.saveAndUpdate(context);

                WidgetService.busy = false;
            }

        }, deviceObject).execute();
    }

    /**
     * Updates the device list and save it as file.
     * Should be called regularly to keep device list up-to-date.
     */
    public static void updateDeviceList(final Context context) {

        Log.d(TAG, "updateDeviceList: Updating device list..");

        // Get the device list.
        new DataManager.GetDeviceListTask(context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                List<DeviceObject> deviceList = result.deviceList;

                // Save the device list to a file
                WidgetStorageManager.setDeviceList(context, deviceList);
            }

            @Override
            public void onFailure(ReturnObject result) {
                Toast.makeText(context, "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }

            @Override
            public void onFinish(ReturnObject result) {
            }

        }).execute();
    }

    /**
     * Updates the device list and also update all widgets.
     * NOTE: This should only be used on first time setup when there is no deviceList yet.
     */
    public static void updateDeviceListAndAllWidgets(final Context context) {

        Log.d(TAG, "updateDeviceList: Updating device list..");
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

        // Get the device list.
        new DataManager.GetDeviceListTask(context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                List<DeviceObject> deviceList = result.deviceList;

                // Save the device list to a file
                WidgetStorageManager.setDeviceList(context, deviceList);

                // There may be multiple widgets active, so update all of them
                for (int appWidgetId : appWidgetIds) {
                    WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);
                    widgetObject.setShowNoConnectionOverlay(false);
                    widgetObject.saveAndUpdate(context);
                }

                WidgetProvider.updateAllWidgets(context);
            }

            @Override
            public void onFailure(ReturnObject result) {
                Toast.makeText(context, "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, result.errorMessage + ": " + result.exception);

                // There may be multiple widgets active, so update all of them
                for (int appWidgetId : appWidgetIds) {
                    WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);
                    widgetObject.setShowNoConnectionOverlay(true);
                    widgetObject.saveAndUpdate(context);
                }
            }

            @Override
            public void onFinish(ReturnObject result) {
            }

        }).execute();
    }
}
