package es.coru.andiag.myquotes.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

import es.coru.andiag.myquotes.BuildConfig;

/**
 * Created by Canalejas on 03/02/2016.
 */
public class Global {

    public static final String TAG = "Global";

    public final static String FLAVOR_LITE = "lite";
    public final static String FLAVOR_PRO = "pro";
    public final static String FLAVOR_ADMIN = "admin";
    public final static int THEME_LIGHT = 0;
    public final static int THEME_DARK = 1;
    public static final String PREF_THEME_KEY = "theme_list";
    public static final String PREF_MUST_SYNC = "must_sync";
    public static final String PREF_SYNC_LANGUAGES = "sync_languages";
    public static Set<String> defaultSyncLanguages;

    static {
        //Hay que insertar todos los lenguajes en defaultSyncLanguages aqui
    }

    //region Preferences
    public static boolean mustSync(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_MUST_SYNC, true);
    }

    public static Set<String> getSyncLanguages(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(PREF_SYNC_LANGUAGES, defaultSyncLanguages);
    }

    public static int getTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.valueOf(prefs.getString(PREF_THEME_KEY, "-1"));
    }

    //endregion
    //region Flavors
    public static boolean isLite() {
        return (BuildConfig.FLAVOR.equals(FLAVOR_LITE));
    }

    public static boolean isPro() {
        return (BuildConfig.FLAVOR.equals(FLAVOR_PRO));
    }

    public static boolean isAdmin() {
        return (BuildConfig.FLAVOR.equals(FLAVOR_ADMIN));
    }
    //endregion

}
