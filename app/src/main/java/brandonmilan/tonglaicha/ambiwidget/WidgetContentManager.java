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
    private RemoteViews view;
    private static int appWidgetId;
    private String prefTempScale;

    private WidgetContentManager(){
    }

    public static WidgetContentManager getInstance(AppWidgetManager appWidgetManager, int appWidgetId) {
        if(INSTANCE == null) {
            INSTANCE = new WidgetContentManager();
        }
        WidgetContentManager.appWidgetManager = appWidgetManager;
        WidgetContentManager.appWidgetId = appWidgetId;

        return INSTANCE;
    }

    public void setView(RemoteViews view) {
        this.view = view;
    }

    public void updateView(final Context context) {
        DeviceObject deviceObject;
        final DeviceObject defaultDeviceObject = WidgetUtils.getDefaultDevice(context);
        final DeviceObject preferredDeviceObject = WidgetUtils.getPreferredDevice(context);
        this.prefTempScale = WidgetUtils.getTempScalePreference(context);
        final String value_celsius = context.getString(R.string.pref_tempScale_value_celsius);
        final String value_fahrenheit = context.getString(R.string.pref_tempScale_value_fahrenheit);
        Log.d(TAG, "updateView: PrefTempScale = " + prefTempScale);


        //Use default device if no preferred device is selected.
        if (preferredDeviceObject == null){
            if (defaultDeviceObject == null){
                Log.d(TAG, "updateView: DevicObject is null, update cancelled.");
                return;
            }
            deviceObject = defaultDeviceObject;
        } else {
            deviceObject = preferredDeviceObject;
        }

        new DataManager.GetTemperatureTask(deviceObject, context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                fillView(result, "TEMP", value_celsius, value_fahrenheit);
            }

            @Override
            public void onFailure(ReturnObject result) {
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }
        }).execute();

        new DataManager.GetHumidityTask(context, new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                fillView(result, "HUMID", null, null);
            }

            @Override
            public void onFailure(ReturnObject result) {
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }
        }, deviceObject).execute();

        //Get the current device.
        final DeviceObject preferredDevice = WidgetUtils.getPreferredDevice(context);

        //Get the current mode of the device.
        new DataManager.GetModeTask(context, new OnProcessFinish<ReturnObject>() {
            @Override
            public void onSuccess(ReturnObject result) {
                String confirmToast = "Current Mode: result.value = " + result.value;
                Log.d(TAG, confirmToast);
                fillView(result, "MODE", null, null);

            }
            @Override
            public void onFailure(ReturnObject result) {
//                Toast.makeText(context, "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }
        }, preferredDevice).execute();

        fillView(new ReturnObject(deviceObject), "ROOM", null, null);
        fillView(new ReturnObject(deviceObject), "LOCATION", null, null);
    }

    public void fillView(ReturnObject result, String TAG, String value_celsius, String value_fahrenheit) {
        switch (TAG){
            case "TEMP":
                Double temperature = WidgetUtils.roundOneDecimal(Double.parseDouble(result.value));

                if(prefTempScale.equals(value_celsius)){
                    view.setTextViewText(R.id.temperature_text, temperature + "\u00B0");
                }
                else if (prefTempScale.equals(value_fahrenheit)){
                    Double tempFahrenheit = WidgetUtils.roundOneDecimal(WidgetUtils.convertToFahrenheit(temperature));
                    view.setTextViewText(R.id.temperature_text, tempFahrenheit + "\u00B0");
                }
                break;
            case "HUMID":
                Double humidity = WidgetUtils.roundOneDecimal(Double.parseDouble(result.value));
                view.setTextViewText(R.id.humidity, humidity + "%");
                break;
            case "MODE":
                String mode = result.value;
                if (mode.equals("Manual")) {
                    mode = "Off";
                    view.setTextViewText(R.id.mode_text, mode);
                    view.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_off);
                } else {
                    view.setTextViewText(R.id.mode_text, mode);
                    view.setImageViewResource(R.id.mode_svg, R.drawable.ic_icn_dashboard_mode_comfort);
                }

                break;

            case "ROOM":
                String roomName = result.deviceObject.roomName();
                view.setTextViewText(R.id.roomName, roomName);
                break;
            case "LOCATION":
                String location = result.deviceObject.locationName();
                view.setTextViewText(R.id.location_text, location);
                break;
        }
        appWidgetManager.updateAppWidget(appWidgetId, view);
    }
}
