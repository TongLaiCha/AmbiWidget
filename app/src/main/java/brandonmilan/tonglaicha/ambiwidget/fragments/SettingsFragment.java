package brandonmilan.tonglaicha.ambiwidget.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
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

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final static String TAG = "SettingsFragment";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_visualizer);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        final PreferenceScreen prefscreen = this.getPreferenceScreen();
        int count = prefscreen.getPreferenceCount();

        for(int i = 0; i < count; i++) {
            Preference p = prefscreen.getPreference(i);
            if (!(p instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
        //TODO: Add divider between preferences.

        createDeviceListPreference(prefscreen);
    }

    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)){
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }

//        WidgetUtils.remoteUpdateWidget(this.getContext(), null);
        //TODO: Update the right widget.
        WidgetUtils.remoteUpdateAllWidgets(this.getContext());
    }

    private void createDeviceListPreference(final PreferenceScreen screen) {
        new DataManager.GetDeviceListTask(screen.getContext(), new OnProcessFinish<ReturnObject>() {

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
                listPreference.setIcon(R.drawable.ic_icn_nav_device_resize);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
