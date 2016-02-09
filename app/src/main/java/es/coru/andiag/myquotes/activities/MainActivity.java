package es.coru.andiag.myquotes.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.client.Firebase;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.entities.Quote;
import es.coru.andiag.myquotes.entities.QuoteType;
import es.coru.andiag.myquotes.fragments.QuoteListFragment;
import es.coru.andiag.myquotes.fragments.SettingsFragment;
import es.coru.andiag.myquotes.utils.db.DBHelper;
import es.coru.andiag.myquotes.utils.db.QuoteDAO;
import es.coru.andiag.myquotes.utils.db.QuoteListListener;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String FRAGMENT_TAG = "QuotesFragment";

    private DBHelper dbHelper;
    private HashSet<Quote> firebaseQuotes = new HashSet<>();
    private HashSet<Quote> localQuotes = new HashSet<>();
    private ArrayList<QuoteListListener> quotesListeners = new ArrayList<>();

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
    //region This code handle quotes we have loaded in our app
    public void notifyDatabaseChange() {
        SQLiteDatabase database = getDbHelper().getReadableDatabase();
        localQuotes.clear();
        localQuotes = QuoteDAO.getQuotes(database);
        database.close();
        notifyListeners();
    }

    public HashSet<Quote> getSearchedQuotes(String query, QuoteType type) {
        HashSet<Quote> quotes = new HashSet<>();
        for (Quote q : firebaseQuotes) {
            if ((q.getAuthor().toLowerCase().contains(query) || q.getQuote().toLowerCase().contains(query)) && (q.getType() == type)) {
                quotes.add(q);
            }
        }
        for (Quote q : localQuotes) {
            if ((q.getAuthor().toLowerCase().contains(query) || q.getQuote().toLowerCase().contains(query)) && (q.getType() == type)) {
                quotes.add(q);
            }
        }
        return quotes;
    }

    private HashSet<Quote> filterQuotes(HashSet<Quote> quotes, QuoteType type) {
        HashSet<Quote> q = new HashSet<>();
        for (Quote quote : quotes) {
            if (quote.getType() == type) {
                q.add(quote);
            }
        }
        return q;
    }

    public HashSet<Quote> getQuotesByType(QuoteType type) {
        HashSet<Quote> quotes = new HashSet<>();
        if (type == QuoteType.DEFAULT) {
            quotes.addAll(firebaseQuotes);
            quotes.addAll(localQuotes);
        } else {
            quotes.addAll(filterQuotes(firebaseQuotes, type));
            quotes.addAll(filterQuotes(localQuotes, type));
        }
        return quotes;
    }

    public void addQuote(Quote q) { //Method used to add a removed quote
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

    public void cleanFirebaseQuotes() {
        firebaseQuotes = new HashSet<>();
    }

    //endregion
    //region Solution to get id in case we are in the admin mode
    private long getSmallestId() {
        long smallest = Long.MAX_VALUE;
        for (Quote q : firebaseQuotes) {
            if (q.getQuoteId() < smallest) {
                smallest = q.getQuoteId();
            }
            if (smallest == 0) break;
        }
        if (smallest <= 0) return -1;
        return smallest - 1;
    }

    private long getBiggesttId() {
        long biggest = -1;
        for (Quote q : firebaseQuotes) {
            if (q.getQuoteId() > biggest) {
                biggest = q.getQuoteId();
            }
        }
        if (biggest <= 0) return 0;
        return biggest + 1;
    }

    /**
     * @return the next valid id to add a quote as admin
     */
    public long getNextFirebaseId() {
        if (firebaseQuotes == null) return -1;
        long id = getSmallestId();
        if (id > 0) return id;
        return getBiggesttId();
    }
    //endregion

    public void changeBarsColors(QuoteType type, View rootView) {
        final ColorDrawable actionBarBackground = new ColorDrawable();
        final ColorDrawable toolbarBarBackground = new ColorDrawable();
        ActionBar actionBar = getSupportActionBar();

        FloatingActionMenu floatingMenu = (FloatingActionMenu) rootView.findViewById(R.id.add_menu);

        switch (type) {
            case BOOK:
                actionBarBackground.setColor(getResources().getColor(R.color.book));
                toolbarBarBackground.setColor(getResources().getColor(R.color.book_bar));
                floatingMenu.setMenuButtonColorNormal(getResources().getColor(R.color.book));
                floatingMenu.setMenuButtonColorPressed(getResources().getColor(R.color.book_bar));
                break;
            case MOVIE:
                actionBarBackground.setColor(getResources().getColor(R.color.movie));
                toolbarBarBackground.setColor(getResources().getColor(R.color.movie_bar));
                floatingMenu.setMenuButtonColorNormal(getResources().getColor(R.color.movie));
                floatingMenu.setMenuButtonColorPressed(getResources().getColor(R.color.movie_bar));
                break;
            case MUSIC:
                actionBarBackground.setColor(getResources().getColor(R.color.music));
                toolbarBarBackground.setColor(getResources().getColor(R.color.music_bar));
                floatingMenu.setMenuButtonColorNormal(getResources().getColor(R.color.music));
                floatingMenu.setMenuButtonColorPressed(getResources().getColor(R.color.music_bar));
                break;
            case PERSONAL:
                actionBarBackground.setColor(getResources().getColor(R.color.personal));
                toolbarBarBackground.setColor(getResources().getColor(R.color.personal_bar));
                floatingMenu.setMenuButtonColorNormal(getResources().getColor(R.color.personal));
                floatingMenu.setMenuButtonColorPressed(getResources().getColor(R.color.personal_bar));
                break;
            case DEFAULT:
                TypedValue typedValue = new TypedValue();
                TypedValue typedValueDark = new TypedValue();
                this.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
                this.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
                int color = typedValue.data;
                int colorDark = typedValueDark.data;
                actionBarBackground.setColor(color);
                toolbarBarBackground.setColor(colorDark);
                break;
        }
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(actionBarBackground);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(toolbarBarBackground.getColor());
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialice firebase
        Firebase.setAndroidContext(this);
        QuoteDAO.loadFirebaseData(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState==null) onNavigationItemSelected(navigationView.getMenu().getItem(0));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            QuoteListFragment myFragment = (QuoteListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            if (myFragment != null && myFragment.getType() == QuoteType.DEFAULT) {
                super.onBackPressed();
                return;
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, QuoteListFragment.newInstance(0, QuoteType.DEFAULT), FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
            search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    QuoteListFragment myFragment = (QuoteListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
                    if (myFragment != null && myFragment.isVisible()) {
                        if (query.length() <= 0) {
                            myFragment.notifyDataSetChanged();
                        } else {
                            myFragment.notifySearch(getSearchedQuotes(query.toLowerCase(), myFragment.getType()));
                        }
                    }
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                getSupportFragmentManager().beginTransaction()
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
        Fragment f = QuoteListFragment.newInstance(0, QuoteType.DEFAULT);

        switch (id) {
            default:
                f = QuoteListFragment.newInstance(0, QuoteType.DEFAULT);
                break;
            case R.id.nav_music:
                f = QuoteListFragment.newInstance(1, QuoteType.MUSIC);
                break;
            case R.id.nav_book:
                f = QuoteListFragment.newInstance(2, QuoteType.BOOK);
                break;
            case R.id.nav_movies:
                f = QuoteListFragment.newInstance(3, QuoteType.MOVIE);
                break;
            case R.id.nav_personal:
                f = QuoteListFragment.newInstance(4, QuoteType.PERSONAL);
                break;
            case R.id.nav_share:
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                String sAux = "Let me recommend you this application\n";
                sAux = sAux + "My Quotes : https://play.google.com/store/apps/details?id=" + appPackageName;
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "Share with: "));
                break;
            case R.id.nav_settings:
                f = new SettingsFragment();
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, f, FRAGMENT_TAG)
                .commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
