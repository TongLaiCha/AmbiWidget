package brandonmilan.tonglaicha.ambiwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
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
    private String prefTempScale;

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
        prefTempScale = WidgetUtils.getTempScalePreference(context);
        Log.d(TAG, "updateView: PrefTempScale = " + prefTempScale);


        //Use default device if no preferred device is selected.
        if (preferredDeviceObject == null){
            deviceObject = defaultDeviceObject;
        } else {
            deviceObject = preferredDeviceObject;
        }

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
                //TODO: needs to be 2 decimals always.
                Double temperature = WidgetUtils.roundOneDecimal(Double.parseDouble(result.value));

                Log.d(TAG, "fillView: " + prefTempScale);
//                String tempScalePref = WidgetUtils.getTempScalePreference();
                if(prefTempScale.equals(String.valueOf(R.string.pref_tempScale_value_celsius))){
                    view.setTextViewText(R.id.temperature, temperature + "C");
                    Log.d(TAG, "fillView: Filling with " + temperature + "C " + view);
                    appWidgetManager.updateAppWidget(appWidgetId, view);
                } else {
                    double tempFahrenheit = WidgetUtils.convertToFahrenheit(temperature);
                    view.setTextViewText(R.id.temperature, temperature + "F");
                    Log.d(TAG, "fillView: Filling with " + temperature + "F " + view);
                    appWidgetManager.updateAppWidget(appWidgetId, view);
                }
                break;
            case "HUMID":
                Double humidity = WidgetUtils.roundOneDecimal(Double.parseDouble(result.value));
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
