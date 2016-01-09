package io.jari.dumpert.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
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
    private String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        this.caller = getIntent().getStringExtra("activity");

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
        if(getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            getSupportActionBar().setSubtitle(null);
        } else {
            Intent back = null;

            switch(caller) {
                case "main":
                    back = new Intent(PreferencesActivity.this, MainActivity.class);
                    break;
                case "viewItem":
                    back = new Intent(PreferencesActivity.this, ViewItemActivity.class);
                    back.putExtra("item", getIntent().getSerializableExtra("item"));
                    break;
                default:
                    break;
            }

            this.startActivity(back);
            this.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            if(getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
                getSupportActionBar().setSubtitle(null);
                return true;
            } else {
                super.onBackPressed();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}