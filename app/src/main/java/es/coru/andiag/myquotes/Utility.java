package es.coru.andiag.myquotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Canalejas on 03/02/2016.
 */
public class Utility {

    public static int getTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.valueOf(prefs.getString(SettingsFragment.THEME_KEY, "-1"));
    }

}
