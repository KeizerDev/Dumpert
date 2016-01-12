package io.jari.dumpert.fragments;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.io.FileNotFoundException;
import java.io.InputStream;

import io.jari.dumpert.R;
import io.jari.dumpert.activities.PreferencesActivity;

/**
 * JARI.IO
 * Date: 25-12-14
 * Time: 2:44
 */
public class PreferencesFragment extends PreferenceFragment {
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private Context context;
    private static int subTitle = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(getArguments().getInt("KEY"));
        PreferencesFragment.subTitle = getArguments().getInt("NAME");

        if(((PreferencesActivity) getActivity()).getSupportActionBar() != null) {
            if(getArguments().getInt("NAME") != R.string.nav_settings) {
                ((PreferencesActivity) getActivity()).getSupportActionBar()
                        .setSubtitle(subTitle);
            }
        }

        Preference.OnPreferenceClickListener nestedListener = new Preference
                .OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int instance = -1;

                switch(preference.getKey()) {
                    case "about": instance = R.xml.prefs_about; break;
                    case "thirdparty": instance = R.xml.prefs_about_thirdparty; break;
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

        Preference.OnPreferenceClickListener clickListener = new Preference
                .OnPreferenceClickListener() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
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
                    case "disclaimer":
                    case "license":
                    case "apache2":
                    case "mit":
                    case "gifdrawable":
                    case "jsoup":
                    case "photoview":
                    case "picasso":
                    case "robototextview":
                        AlertDialog.Builder licenseDialog = new AlertDialog.Builder(context);
                        licenseDialog.setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                dialog.dismiss();
                            }
                        });
                        try {
                            Resources res = getResources();
                            InputStream ins;

                            switch(pref.getKey()) {
                                case "disclaimer":
                                    licenseDialog.setTitle(R.string.pref_disclaimer);
                                    ins = res.openRawResource(R.raw.disclaimer);
                                    break;
                                case "license":
                                    licenseDialog.setTitle(R.string.pref_license);
                                    ins = res.openRawResource(R.raw.dumpert);
                                    break;
                                case "apache2":
                                    licenseDialog.setTitle(R.string.pref_license_apache2);
                                    ins = res.openRawResource(R.raw.apache2);
                                    break;
                                case "mit":
                                    licenseDialog.setTitle(R.string.pref_license_mit);
                                    ins = res.openRawResource(R.raw.mit);
                                    break;
                                case "gifdrawable":
                                    licenseDialog.setTitle(R.string.pref_license_gifdrawable);
                                    ins = res.openRawResource(R.raw.gifdrawable);
                                    break;
                                case "jsoup":
                                    licenseDialog.setTitle(R.string.pref_license_jsoup);
                                    ins = res.openRawResource(R.raw.jsoup);
                                    break;
                                case "photoview":
                                    licenseDialog.setTitle(R.string.pref_license_photoview);
                                    ins = res.openRawResource(R.raw.photoview);
                                    break;
                                case "picasso":
                                    licenseDialog.setTitle(R.string.pref_license_picasso);
                                    ins = res.openRawResource(R.raw.picasso);
                                    break;
                                case "robototextview":
                                    licenseDialog.setTitle(R.string.pref_license_robototextview);
                                    ins = res.openRawResource(R.raw.robototextview);
                                    break;
                                default:
                                    throw new FileNotFoundException();
                            }

                            byte[] b = new byte[ins.available()];
                            ins.read(b);
                            licenseDialog.setMessage(new String(b));
                        } catch(Exception e) {
                            e.printStackTrace();
                            licenseDialog.setMessage(e.getLocalizedMessage());
                        } finally {
                            licenseDialog.show();
                        }
                        break;
                    default:
                        break;
                }

                return true;
            }
        };

        attachClickListener("about", nestedListener);
        attachClickListener("disclaimer", clickListener);
        attachClickListener("license", clickListener);
        attachClickListener("thirdparty", nestedListener);
        attachClickListener("apache2", clickListener);
        attachClickListener("mit", clickListener);
        attachClickListener("gifdrawable", clickListener);
        attachClickListener("jsoup", clickListener);
        attachClickListener("photoview", clickListener);
        attachClickListener("picasso", clickListener);
        attachClickListener("robototextview", clickListener);
        attachClickListener("content", nestedListener);
        attachClickListener("data", nestedListener);
        attachClickListener("datausage", clickListener);
        attachClickListener("visual", nestedListener);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }

    public static PreferencesFragment newInstance(int key, int name) {
        PreferencesFragment fragment = new PreferencesFragment();
        Bundle args = new Bundle();

        args.putInt("KEY", key);
        args.putInt("NAME", name);
        fragment.setArguments(args);

        return fragment;
    }

    private void attachClickListener(String key, Preference.OnPreferenceClickListener listener) {
        if(findPreference(key) == null)
            return;

        findPreference(key).setOnPreferenceClickListener(listener);
    }

    public static int getSubTitle() {
        return PreferencesFragment.subTitle;
    }

    public static void setSubTitle(int subTitle) {
        PreferencesFragment.subTitle = subTitle;
    }

}
