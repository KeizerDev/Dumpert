package io.jari.dumpert.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import io.jari.dumpert.R;


public class BaseActivity extends AppCompatActivity {
    private final static String TAG = "Base";
    public SharedPreferences preferences;
    public boolean dontApplyTheme = false;

    void setTheme() {
        try {
            String theme = preferences.getString("theme", "green");

            if (theme.equals("green")) {
                //default theme, do nothing
            } else if (theme.equals("blue")) {
                super.setTheme(R.style.Theme_Dumpert_Blue);
            } else if (theme.equals("red")) {
                super.setTheme(R.style.Theme_Dumpert_Red);
            } else if (theme.equals("pink")) {
                super.setTheme(R.style.Theme_Dumpert_Pink);
            } else if (theme.equals("orange")) {
                super.setTheme(R.style.Theme_Dumpert_Orange);
            } else if (theme.equals("bluegray")) {
                super.setTheme(R.style.Theme_Dumpert_BlueGray);
            } else if (theme.equals("webartisans")) {
                super.setTheme(R.style.Theme_Dumpert_WebArtisans);
            }
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(!dontApplyTheme) this.setTheme();
        super.onCreate(savedInstanceState);
        if(!dontApplyTheme) this.setTheme(); // if we don't call it again here theme doesn't get properly acquired #justandroidthings
    }
}
