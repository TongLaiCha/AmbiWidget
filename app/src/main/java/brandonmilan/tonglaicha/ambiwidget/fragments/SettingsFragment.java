package brandonmilan.tonglaicha.ambiwidget.fragments;

import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.R;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.utils.WidgetUtils;

public class SettingsFragment extends PreferenceFragmentCompat {
    private final static String TAG = "SettingsFragment";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        //WARINING: This must be the first line or settings activity will crash.
        addPreferencesFromResource(R.xml.pref_visualizer);

        final PreferenceScreen screen = this.getPreferenceScreen();

        //TODO: Add divider between preferences.
//        setDivider();

        new DataManager.GetDeviceListTask(false, screen.getContext(), new OnProcessFinish<ReturnObject>() {

            @Override
            public void onSuccess(ReturnObject result) {
                List<DeviceObject> deviceList = result.deviceList;
                List<String> deviceRoomNames = new ArrayList<String>();
                List<String> deviceListGson = new ArrayList<>();

                DeviceObject preferredDevice = WidgetUtils.getPreferredDevice(screen.getContext());

                for (DeviceObject deviceObject: deviceList) {
                    deviceRoomNames.add(deviceObject.roomName());

                    Gson gson = new Gson();
                    String deviceObjectGson = gson.toJson(deviceObject);
                    deviceListGson.add(deviceObjectGson);
                }

                ListPreference listPreference = new ListPreference(screen.getContext());
                listPreference.setKey(String.valueOf(R.string.pref_preferredDevice_key));
                listPreference.setTitle(R.string.pref_preferredDevice_label);
                listPreference.setEntries(deviceRoomNames.toArray(new CharSequence[deviceRoomNames.size()]));
                listPreference.setEntryValues(deviceListGson.toArray(new CharSequence[deviceListGson.size()]));

                //TODO: Needs to be the same as default device saved in shared pref.
                listPreference.setDefaultValue(deviceListGson.get(0));

                if(preferredDevice == null) {
                    listPreference.setSummary("Default");
                } else {
                    listPreference.setSummary(preferredDevice.roomName());
                }

                screen.addPreference(listPreference);
            }

            @Override
            public void onFailure(ReturnObject result) {
//                Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, result.errorMessage + ": " + result.exception);
            }
        }).execute();
    }
}
