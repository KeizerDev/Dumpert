package io.jari.dumpert.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import io.jari.dumpert.R;
import io.jari.dumpert.fragments.SearchFragment;

/**
 * JARI.IO
 * Date: 19-1-15
 * Time: 17:56
 */
public class SearchResultsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresults);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if(getSupportActionBar() == null) setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.nav_search);
        }

        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            SearchFragment searchFragment = new SearchFragment();
            searchFragment.query = getIntent().getStringExtra(SearchManager.QUERY);

            if(getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(searchFragment.query);
            }

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.searchresults, searchFragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }

        return false;
    }

}
