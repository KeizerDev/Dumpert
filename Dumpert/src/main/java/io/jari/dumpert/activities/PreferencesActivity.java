package io.jari.dumpert.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import io.jari.dumpert.R;
import io.jari.dumpert.fragments.PreferencesFragment;

/**
 * Created by cytodev on 4-1-16.
 */
public class PreferencesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if(getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.rootView, PreferencesFragment.newInstance(R.xml.prefs,
                        R.string.nav_settings))
                .addToBackStack("Preferences")
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if(!getFragmentManager().popBackStackImmediate()) {
                super.onBackPressed();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}