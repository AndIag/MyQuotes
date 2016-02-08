package es.coru.andiag.myquotes.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.activities.MainActivity;
import es.coru.andiag.myquotes.utils.GlobalPreferences;
import es.coru.andiag.myquotes.utils.db.QuoteDAO;

/**
 * Created by Canalejas on 03/02/2016.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        if (GlobalPreferences.isLite()) {
            Preference pref = findPreference(GlobalPreferences.PREF_MUST_SYNC);
            pref.setEnabled(false);
            pref = findPreference(GlobalPreferences.PREF_SYNC_LANGUAGES);
            pref.setEnabled(false);
        }
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        switch (key) {
            case GlobalPreferences.PREF_THEME_KEY: //Change the theme
                getActivity().recreate();
                break;
            case GlobalPreferences.PREF_MUST_SYNC:
                //Clean the array and if sync is activate reload data
                ((MainActivity) getActivity()).cleanFirebaseQuotes();
                if (prefs.getBoolean(GlobalPreferences.PREF_MUST_SYNC, true)) {
                    QuoteDAO.loadFirebaseData((MainActivity) getActivity());
                }
                break;
            case GlobalPreferences.PREF_SYNC_LANGUAGES:
                ((MainActivity) getActivity()).cleanFirebaseQuotes();
                QuoteDAO.loadFirebaseData((MainActivity) getActivity());
                break;
        }
    }
}
