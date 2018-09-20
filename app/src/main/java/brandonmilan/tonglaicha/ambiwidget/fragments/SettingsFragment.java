package brandonmilan.tonglaicha.ambiwidget.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import brandonmilan.tonglaicha.ambiwidget.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_visualizer);
    }
}
