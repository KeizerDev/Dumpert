package io.jari.dumpert.activities;

import android.os.Bundle;

import io.jari.dumpert.fragments.PreferencesFragment;

/**
 * Created by cytodev on 4-1-16.
 */
public class PreferencesActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferencesFragment())
                .commit();

    }
}