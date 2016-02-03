package es.coru.andiag.myquotes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    public boolean isPro() {
        return (BuildConfig.FLAVOR.equals("pro"));
    }

    public boolean isAdmin() {
        return (BuildConfig.FLAVOR.equals("admin"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
