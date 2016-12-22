package es.coru.andiag.myquotes.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.entities.Quote;
import es.coru.andiag.myquotes.utils.GlobalPreferences;

public class BaseActivity extends AppCompatActivity {

    public static final String TAG = "BaseActivity";

    private Quote quote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateTheme();
    }

    private void updateTheme() { //Change theme to all activities that extends BaseActivity
        if (GlobalPreferences.getTheme(getApplicationContext()) == GlobalPreferences.THEME_DARK) {
            setTheme(R.style.AppTheme_Dark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(getResources().getColor(R.color.darkPrimaryDark));
            }
            return;
        }
        if (GlobalPreferences.getTheme(getApplicationContext()) == GlobalPreferences.THEME_LIGHT) {
            setTheme(R.style.AppTheme);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(getResources().getColor(R.color.lightPrimaryDark));
            }
            return;
        }
        setTheme(R.style.AppTheme);
    }

}
