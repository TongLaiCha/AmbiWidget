package brandonmilan.tonglaicha.ambiwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.utils.WidgetUtils;

public class WidgetContentManager {
    private static final String TAG = "WidgetContentManager";
    private static WidgetContentManager INSTANCE;
    private static AppWidgetManager appWidgetManager;
    private static RemoteViews view;
    private static int appWidgetId;

    private WidgetContentManager(){
    }

    public static WidgetContentManager getInstance(AppWidgetManager appWidgetManager, RemoteViews view, int appWidgetId) {
        if(INSTANCE == null) {
            INSTANCE = new WidgetContentManager();
        }
        WidgetContentManager.appWidgetManager = appWidgetManager;
        WidgetContentManager.view = view;
        WidgetContentManager.appWidgetId = appWidgetId;

        return INSTANCE;
    }

    public void updateView(Context context) {
        DeviceObject deviceObject;
        final DeviceObject defaultDeviceObject = WidgetUtils.getDefaultDevice(context);
        final DeviceObject preferredDeviceObject = WidgetUtils.getPreferredDevice(context);

        //Use default device if no preferred device is selected.
        if (preferredDeviceObject == null){
            deviceObject = defaultDeviceObject;
        } else {
            deviceObject = preferredDeviceObject;
        }

//        Log.d(TAG, "updateView: Preferred device = " + deviceObject.roomName());


        new DataManager.GetTemperatureTask(deviceObject, false, context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                fillView(result, "TEMP");
                Log.d(TAG, "onSuccess: Content filled");
            }

            @Override
            public void onFailure(ReturnObject result) {
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }
        }).execute();

        new DataManager.GetHumidityTask(deviceObject, false, context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                fillView(result, "HUMID");
                Log.d(TAG, "onSuccess: Content filled");
            }

            @Override
            public void onFailure(ReturnObject result) {
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }
        }).execute();

        fillView(new ReturnObject(deviceObject), "ROOM");
        fillView(new ReturnObject(deviceObject), "LOCATION");
    }

    private void fillView(ReturnObject result, String TAG) {
        switch (TAG){
            case "TEMP":
                String temperature = result.value;
                view.setTextViewText(R.id.temperature, temperature);
                Log.d(TAG, "fillView: Filling with " + temperature + view);
                appWidgetManager.updateAppWidget(appWidgetId, view);
                break;
            case "HUMID":
                String humidity = result.value;
                WidgetContentManager.view.setTextViewText(R.id.humidity, humidity + "%");
                appWidgetManager.updateAppWidget(appWidgetId, view);
                break;
            case "ROOM":
                String roomName = result.deviceObject.roomName();
                WidgetContentManager.view.setTextViewText(R.id.roomName, roomName);
                appWidgetManager.updateAppWidget(appWidgetId, view);
                break;
            case "LOCATION":
                String location = result.deviceObject.locationName();
                WidgetContentManager.view.setTextViewText(R.id.location, location);
                appWidgetManager.updateAppWidget(appWidgetId, view);
                break;
        }

    }
}
