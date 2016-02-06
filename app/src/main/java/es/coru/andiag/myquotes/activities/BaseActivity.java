package es.coru.andiag.myquotes.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.utils.Global;

public class BaseActivity extends AppCompatActivity {

    public static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateTheme();
    }

    private void updateTheme() { //Change theme to all activities that extends BaseActivity
        if (Global.getTheme(getApplicationContext()) == Global.THEME_DARK) {
            setTheme(R.style.AppTheme_Dark);
            return;
        }
        if (Global.getTheme(getApplicationContext()) == Global.THEME_LIGHT) {
            setTheme(R.style.AppTheme_Light);
            return;
        }
        setTheme(R.style.AppTheme_Light);
    }

}
