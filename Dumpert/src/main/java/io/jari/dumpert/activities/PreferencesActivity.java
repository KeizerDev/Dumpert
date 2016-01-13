package io.jari.dumpert.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import io.jari.dumpert.R;
import io.jari.dumpert.fragments.PreferencesFragment;

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

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.rootView, PreferencesFragment.newInstance(R.xml.prefs,
                        R.string.nav_settings))
                .commit();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            back();
        }

        return super.onOptionsItemSelected(item);
    }

    private void back() {
        if(getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();

            if(getSupportActionBar() != null) {
                if(PreferencesFragment.getSubTitle() == R.string.pref_thirdparty) {
                    getSupportActionBar().setSubtitle(R.string.pref_about);
                    PreferencesFragment.setSubTitle(R.string.pref_about);
                } else {
                    getSupportActionBar().setSubtitle(null);
                }
            }
        } else {
            Intent back = new Intent(PreferencesActivity.this, MainActivity.class);

            this.startActivity(back);
            this.finish();
        }
    }

}
