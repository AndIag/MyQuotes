package es.coru.andiag.myquotes.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.entities.Quote;
import es.coru.andiag.myquotes.fragments.SettingsFragment;
import es.coru.andiag.myquotes.utils.QuoteListListener;
import es.coru.andiag.myquotes.utils.db.DBHelper;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DBHelper dbHelper;
    private HashSet<Quote> firebaseQuotes = new HashSet<>();
    private ArrayList<QuoteListListener> quotesListeners;

    public DBHelper getDbHelper() {
        if (dbHelper == null) dbHelper = new DBHelper(this);
        return dbHelper;
    }

    //region This code handle the listener we use to dinamically update all fragments
    public void registerListener(QuoteListListener listener) {
        quotesListeners.add(listener);
        listener.notifyDataSetChanged(); //In case we have loaded the quotes yet
    }

    public void unregisterListener(QuoteListListener listener) {
        quotesListeners.remove(listener);
    }

    public void notifyListeners() {
        for (QuoteListListener l : quotesListeners) {
            l.notifyDataSetChanged();
        }
    }

    //endregion
    //region This code handle the quotes we have loaded in our app
    public void addQuotes(Quote q) {
        firebaseQuotes.add(q);
        notifyListeners();
    }

    public void addQuotes(List<Quote> quoteList) {
        firebaseQuotes.addAll(quoteList);
        notifyListeners();
    }

    public void removeQuote(Quote q) {
        firebaseQuotes.remove(q);
        notifyListeners();
    }

    public HashSet<Quote> getFirebaseQuotes() {
        return firebaseQuotes;
    }
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new SettingsFragment())
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_settings:
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_container, new SettingsFragment())
                        .commit();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
