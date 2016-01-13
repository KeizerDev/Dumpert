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
            switch(preferences.getString("theme", "green")) {
                case "blue":        super.setTheme(R.style.Theme_Dumpert_Blue);        break;
                case "red":         super.setTheme(R.style.Theme_Dumpert_Red);         break;
                case "pink":        super.setTheme(R.style.Theme_Dumpert_Pink);        break;
                case "orange":      super.setTheme(R.style.Theme_Dumpert_Orange);      break;
                case "bluegray":    super.setTheme(R.style.Theme_Dumpert_BlueGray);    break;
                case "webartisans": super.setTheme(R.style.Theme_Dumpert_WebArtisans); break;
                default:            super.setTheme(R.style.Theme_Dumpert);             break;
            }
        } catch(Exception e) {
            Log.e(TAG, "Could not apply theme", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(!dontApplyTheme) this.setTheme();
        super.onCreate(savedInstanceState);
        if(!dontApplyTheme) this.setTheme();
    }

}
