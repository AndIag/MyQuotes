package es.coru.andiag.myquotes.activities;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.entities.Quote;
import es.coru.andiag.myquotes.utils.GlobalPreferences;
import es.coru.andiag.myquotes.utils.db.QuoteDAO;

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

    public void requestFeature(String requiredFeature, View view, Quote quote) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, requiredFeature)) {
            Snackbar.make(view, getResources().getString(R.string.permission_explication), Snackbar.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{requiredFeature}, GlobalPreferences.APP_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        this.quote = quote;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GlobalPreferences.APP_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && quote != null) {
                    QuoteDAO.shareQuoteToUs(this, null, quote);
                    quote = null;
                } else {
                    Toast.makeText(this, getResources().getString(R.string.cant_share), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
