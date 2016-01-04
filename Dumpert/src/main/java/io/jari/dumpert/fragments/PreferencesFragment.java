package io.jari.dumpert.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import io.jari.dumpert.R;
import io.jari.dumpert.activities.PreferencesActivity;

/**
 * JARI.IO
 * Date: 25-12-14
 * Time: 2:44
 */
public class PreferencesFragment extends PreferenceFragment {
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("theme")) {
                    PreferencesActivity prefs = (PreferencesActivity) getActivity();
//                    main.preferences.edit().putBoolean("switchtosettings", true).commit();
                    prefs.finish();
                    startActivity(prefs.getIntent());
                    prefs.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }
}
