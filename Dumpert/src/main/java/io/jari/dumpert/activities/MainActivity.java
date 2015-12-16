package io.jari.dumpert.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import io.jari.dumpert.R;
import io.jari.dumpert.fragments.*;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;


public class MainActivity extends MaterialNavigationDrawer {
    public SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        this.setTheme();
        super.onCreate(savedInstanceState);
        this.setTheme(); // if we don't call it again here theme doesn't get properly acquired #justandroidthings
    }

    @Override
    public void init(Bundle bundle) {
        this.disableLearningPattern(); //wtflibrary

        this.addSection(this.newSection(getResources().getString(R.string.nav_new), R.drawable.ic_new_releases, new NewFragment()));
        this.addSection(this.newSection(getResources().getString(R.string.nav_top), R.drawable.ic_whatshot, new TopFragment()));
        this.addSection(this.newSection(getResources().getString(R.string.nav_images), R.drawable.ic_photo2, new ImageFragment()));
        this.addSection(this.newSection(getResources().getString(R.string.nav_videos), R.drawable.ic_play_circle_fill2, new VideoFragment()));
        this.addSection(this.newSection(getResources().getString(R.string.nav_audio), R.drawable.ic_audiotrack, new AudioFragment()));
        this.addBottomSection(this.newSection(getResources().getString(R.string.nav_about), R.drawable.ic_info, new Intent(MainActivity.this, AboutActivity.class)));
        this.addBottomSection(this.newSection(getResources().getString(R.string.nav_login), R.drawable.ic_info, new Intent(MainActivity.this, LoginActivity.class)));
        this.addBottomSection(this.newSection(getResources().getString(R.string.nav_settings), R.drawable.ic_settings, new PreferencesFragment()));

        this.setBackPattern(MaterialNavigationDrawer.BACKPATTERN_BACK_ANYWHERE);
    }

    void setTheme() {
        String theme = preferences.getString("theme", "green");

        if(theme.equals("green")) {
            //default theme, do nothing
        } else if(theme.equals("blue")) {
            super.setTheme(R.style.Theme_Dumpert_Blue_Drawer);
        } else if(theme.equals("red")) {
            super.setTheme(R.style.Theme_Dumpert_Red_Drawer);
        } else if(theme.equals("pink")) {
            super.setTheme(R.style.Theme_Dumpert_Pink_Drawer);
        } else if(theme.equals("orange")) {
            super.setTheme(R.style.Theme_Dumpert_Orange_Drawer);
        } else if(theme.equals("bluegray")) {
            super.setTheme(R.style.Theme_Dumpert_BlueGray_Drawer);
        } else if(theme.equals("webartisans")) {
            super.setTheme(R.style.Theme_Dumpert_WebArtisans_Drawer);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setIconified(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(MainActivity.this, SearchResultsActivity.class);
                intent.setAction(Intent.ACTION_SEARCH);
                intent.putExtra(SearchManager.QUERY, query);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

}
