package es.coru.andiag.myquotes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    public static final String TAG = "BaseActivity";

    private final static int THEME_DARK = 0;
    private final static int THEME_LIGHT = 1;

    public boolean isPro() {
        return (BuildConfig.FLAVOR.equals("pro"));
    }

    public boolean isAdmin() {
        return (BuildConfig.FLAVOR.equals("admin"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateTheme();
    }

    private void updateTheme() {
        if (Utility.getTheme(getApplicationContext()) <= THEME_DARK) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }
    }

}
