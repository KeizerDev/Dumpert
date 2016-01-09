package io.jari.dumpert.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
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

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            SearchFragment searchFragment = new SearchFragment();
            searchFragment.query = intent.getStringExtra(SearchManager.QUERY);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.nav_search);
            getSupportActionBar().setSubtitle(searchFragment.query);

            getFragmentManager().beginTransaction().replace(R.id.searchresults, searchFragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        } else return false;
    }
}
