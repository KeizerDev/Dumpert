package io.jari.dumpert.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.jari.dumpert.R;
import io.jari.dumpert.api.Login;
import io.jari.dumpert.dialogs.LoginDialog;
import io.jari.dumpert.fragments.*;

// username set to dummy to ensure the login functionality is not used.

public class MainActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG                   = "DMA";
    private static final long   DRAWER_CLOSE_DELAY_MS = 250;

    public SharedPreferences preferences;

    private        FragmentManager   manager;
    private        DrawerLayout      drawer;
    private static SharedPreferences credentials;
    private static NavigationView    navigationView;
    private static TextView          loginAction;
    private        int               navItemID = R.id.nav_new;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        navigate(navItemID);
    }

    @Override
    protected void onPause() {
        drawer = null;
        credentials = null;
        navigationView = null;
        loginAction = null;

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initUI();
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        item.setChecked(true);
        navItemID = item.getItemId();

        drawer.closeDrawer(GravityCompat.START);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(item.getItemId());
            }
        }, DRAWER_CLOSE_DELAY_MS);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.nav_search).getActionView();

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.nav_settings) {
            Intent settings = new Intent(MainActivity.this, PreferencesActivity.class);
            this.startActivity(settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initUI() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        manager = getFragmentManager();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        credentials = getSharedPreferences("dumpert", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if(getSupportActionBar() == null) setSupportActionBar(toolbar);

        navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(navItemID).setChecked(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.open_drawer, R.string.close_drawer);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        loginAction = (TextView) navigationView.getHeaderView(0).findViewById(R.id.login_action);
        loginAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag().equals("Login")) {
                    new LoginDialog().show(manager, "Login");
                    notifyAccountChanged();
                } else {
                    if(Login.logout(MainActivity.this)) {
                        notifyAccountChanged();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.error_could_not_logout,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        notifyAccountChanged();
    }

    private void navigate(int itemID) {
        FragmentTransaction transaction = manager.beginTransaction();
        int title = R.string.app_name;

        switch(itemID) {
            case R.id.nav_new:
                title = R.string.nav_new;
                transaction.replace(R.id.rootView, new NewFragment());
                break;
            case R.id.nav_top:
                title = R.string.nav_top;
                transaction.replace(R.id.rootView, new TopFragment());
                break;
            case R.id.nav_images:
                title = R.string.nav_images;
                transaction.replace(R.id.rootView, new ImageFragment());
                break;
            case R.id.nav_videos:
                title = R.string.nav_videos;
                transaction.replace(R.id.rootView, new VideoFragment());
                break;
            case R.id.nav_audio:
                title = R.string.nav_audio;
                transaction.replace(R.id.rootView, new AudioFragment());
                break;
            default:
                Log.w(TAG, "nothing to navigate to");
        }

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        transaction.commit();
    }

    public static void notifyAccountChanged() {
        Log.v(TAG, "checking if login status has changed");

        String username = credentials.getString("username", "");
        ImageView loginImage = (ImageView) navigationView.getHeaderView(0)
                .findViewById(R.id.login_image);
        TextView  loginName  = (TextView) navigationView.getHeaderView(0)
                .findViewById(R.id.login_username);

        if(username.equals("")) {
            loginImage.setVisibility(View.GONE);
            loginName.setText("");
            loginName.setVisibility(View.GONE);
            loginAction.setText(R.string.nav_login);
            loginAction.setTag("Login");
        } else {
            loginImage.setVisibility(View.VISIBLE);
            loginName.setVisibility(View.VISIBLE);
            loginName.setText(username);
            loginAction.setText(R.string.nav_logout);
            loginAction.setTag("Logout");
        }
    }

}
