package es.coru.andiag.myquotes;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

/**
 * Created by Canalejas on 03/02/2016.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "SettingsFragment";
    private static boolean isDialogOpen = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private void showDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isDialogOpen = false;
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Global.PREF_THEME_KEY: //Change the theme
                getActivity().recreate();
                break;
            case Global.PREF_MUST_SYNC:
                if (Global.isLite()) { //If u are a lite user u can´t disable the synchronization
                    if (!isDialogOpen) { //It avoid the app to create more than one dialog
                        isDialogOpen = true;
                        showDialog("INSERTAR STRING AQUI");
                    }
                    //Put the value again to true
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    prefs.edit().putBoolean(Global.PREF_MUST_SYNC, true).apply();
                }
                break;
            case Global.PREF_SYNC_LANGUAGES:
                if (Global.isLite()) { //If u are a lite user u can´t disable the synchronization
                    if (!isDialogOpen) { //It avoid the app to create more than one dialog
                        isDialogOpen = true;
                        showDialog("INSERTAR OTRO STRING AQUI");
                    }
                    //Put the value again to all languages
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    prefs.edit().putStringSet(Global.PREF_SYNC_LANGUAGES, Global.defaultSyncLanguages).apply();
                }
                break;
        }
    }
}
