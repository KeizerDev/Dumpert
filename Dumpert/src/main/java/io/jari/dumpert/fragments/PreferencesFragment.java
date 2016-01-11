package io.jari.dumpert.fragments;

import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import io.jari.dumpert.R;
import io.jari.dumpert.activities.PreferencesActivity;

/**
 * JARI.IO
 * Date: 25-12-14
 * Time: 2:44
 */
public class PreferencesFragment extends PreferenceFragment {
    Preference.OnPreferenceClickListener nestedListener;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    Preference.OnPreferenceClickListener clickListener;

    public static PreferencesFragment newInstance(int key, int name) {
        PreferencesFragment fragment = new PreferencesFragment();
        Bundle args = new Bundle();

        args.putInt("KEY", key);
        args.putInt("NAME", name);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(getArguments().getInt("KEY"));

        if(((PreferencesActivity) getActivity()).getSupportActionBar() != null) {
            if(getArguments().getInt("NAME") != R.string.nav_settings) {
                ((PreferencesActivity) getActivity()).getSupportActionBar()
                        .setSubtitle(getArguments().getInt("NAME"));
            }
        }

        nestedListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int instance = -1;

                switch(preference.getKey()) {
                    case "about": instance = R.xml.prefs_about; break;
                    case "data": instance = R.xml.prefs_data; break;
                    case "content": instance = R.xml.prefs_content; break;
                    case "visual": instance = R.xml.prefs_visual; break;
                    default:
                        break;
                }

                if(instance != -1) {
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.rootView, newInstance(instance, preference.getTitleRes()))
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack(preference.getKey())
                            .commit();
                }

                return true;
            }
        };


        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("theme")) {
                    PreferencesActivity prefs = (PreferencesActivity) getActivity();
//                    main.preferences.edit().putBoolean("switchtosettings", true).commit();
                    prefs.finish();
                    startActivity(prefs.getIntent());
                    prefs.overridePendingTransition(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                }
            }
        };

        clickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference pref) {
                switch(pref.getKey()) {
                    case "datausage":
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        // @fixme: this links to the overview, we need the package data.
                        intent.setComponent(new ComponentName("com.android.settings",
                                "com.android.settings.Settings$DataUsageSummaryActivity"));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }

                return true;
            }
        };

        attachClickListener("about", nestedListener);
        attachClickListener("data", nestedListener);
        attachClickListener("content", nestedListener);
        attachClickListener("visual", nestedListener);
        attachClickListener("datausage", clickListener);
    }

    private void attachClickListener(String key, Preference.OnPreferenceClickListener listener) {
        if(findPreference(key) == null)
            return;

        findPreference(key).setOnPreferenceClickListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(listener);
    }

}
