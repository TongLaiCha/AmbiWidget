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
        this.prefTempScale = WidgetUtils.getTempScalePreference(context);
        final String value_celsius = context.getString(R.string.pref_tempScale_value_celsius);
        final String value_fahrenheit = context.getString(R.string.pref_tempScale_value_fahrenheit);
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
                fillView(result, "TEMP", value_celsius, value_fahrenheit);
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
                fillView(result, "HUMID", value_celsius, value_fahrenheit);
                Log.d(TAG, "onSuccess: Content filled");
            }

            @Override
            public void onFailure(ReturnObject result) {
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }
        }).execute();

        fillView(new ReturnObject(deviceObject), "ROOM", null, null);
        fillView(new ReturnObject(deviceObject), "LOCATION", null, null);
    }

    private void fillView(ReturnObject result, String TAG,
                          String value_celsius, String value_fahrenheit) {
        switch (TAG){
            case "TEMP":
                //TODO: needs to be 2 decimals always.
                Double temperature = WidgetUtils.roundOneDecimal(Double.parseDouble(result.value));

                Log.d(TAG, "fillView: " + prefTempScale);
//                String tempScalePref = WidgetUtils.getTempScalePreference();
                if(prefTempScale.equals(value_celsius)){
                    view.setTextViewText(R.id.temperature, temperature + "C");
                    Log.d(TAG, "fillView: Filling with " + temperature + "C " + view);
                    appWidgetManager.updateAppWidget(appWidgetId, view);
                } else if (prefTempScale.equals(value_fahrenheit)){
                    double tempFahrenheit = WidgetUtils.convertToFahrenheit(temperature);
                    view.setTextViewText(R.id.temperature, tempFahrenheit + "F");
                    Log.d(TAG, "fillView: Filling with " + tempFahrenheit + "F " + view);
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
