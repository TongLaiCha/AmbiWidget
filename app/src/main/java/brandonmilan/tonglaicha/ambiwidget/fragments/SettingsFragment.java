package brandonmilan.tonglaicha.ambiwidget.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.R;
import brandonmilan.tonglaicha.ambiwidget.WidgetProvider;
import brandonmilan.tonglaicha.ambiwidget.WidgetStorageManager;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;

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

        WidgetProvider.updateAllWidgets(this.getContext());

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
