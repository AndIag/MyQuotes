package es.coru.andiag.myquotes.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.utils.GlobalPreferences;

public class BaseActivity extends AppCompatActivity {

    public static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateTheme();
    }

    private void updateTheme() { //Change theme to all activities that extends BaseActivity
        if (GlobalPreferences.getTheme(getApplicationContext()) == GlobalPreferences.THEME_DARK) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
            return;
        }
        if (GlobalPreferences.getTheme(getApplicationContext()) == GlobalPreferences.THEME_LIGHT) {
            setTheme(R.style.AppTheme_Light_NoActionBar);
            return;
        }
        setTheme(R.style.AppTheme_Light_NoActionBar);
    }

}
