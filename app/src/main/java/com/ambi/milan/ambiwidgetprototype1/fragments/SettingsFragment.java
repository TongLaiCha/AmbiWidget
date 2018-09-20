package com.ambi.milan.ambiwidgetprototype1.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import com.ambi.milan.ambiwidgetprototype1.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_visualizer);
    }
}
