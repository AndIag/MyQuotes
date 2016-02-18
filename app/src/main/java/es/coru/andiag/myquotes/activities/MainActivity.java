package es.coru.andiag.myquotes.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import es.coru.andiag.myquotes.fragments.AboutFragment;
import es.coru.andiag.myquotes.fragments.QuoteListFragment;
import es.coru.andiag.myquotes.fragments.SettingsFragment;
import es.coru.andiag.myquotes.utils.GlobalPreferences;
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
        boolean isDefault = type == QuoteType.DEFAULT;
        for (Quote q : firebaseQuotes) {
            if ((q.getAuthor().toLowerCase().contains(query) || q.getQuote().toLowerCase().contains(query)) //Query match author or quote
                    && ((q.getType() == type) || (isDefault))) { //Type = quoteType or we are in default fragment
                quotes.add(q);
            }
        }
        for (Quote q : localQuotes) {
            if ((q.getAuthor().toLowerCase().contains(query) || q.getQuote().toLowerCase().contains(query)) //Query match author or quote
                    && ((q.getType() == type) || (isDefault))) { //Type = quoteType or we are in default fragment
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

    public void addQuotes(List<Quote> quoteList) {
        firebaseQuotes.addAll(quoteList);
        notifyListeners();
        QuoteDAO.cleanListener(); //Remove firebase listener to save battery
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

    /**
     * @return the next valid id to add a quote as admin
     */
    public long getNextFirebaseId() {
        if (firebaseQuotes == null) return -1;
        long biggest = -1;
        for (Quote q : firebaseQuotes) {
            if (q.getQuoteId() > biggest) {
                biggest = q.getQuoteId();
            }
        }
        if (biggest <= 0) return 0;
        return biggest + 1;
    }
    //endregion

    public void changeBarsColors(QuoteType type, View rootView) {
        final ColorDrawable actionBarBackground = new ColorDrawable();
        final ColorDrawable toolbarBarBackground = new ColorDrawable();
        ActionBar actionBar = getSupportActionBar();

        FloatingActionMenu floatingMenu = (FloatingActionMenu) rootView.findViewById(R.id.add_menu);
        TypedValue color = new TypedValue();
        TypedValue colorDark = new TypedValue();

        switch (type) {
            case BOOK:
                getTheme().resolveAttribute(R.attr.book, color, true);
                getTheme().resolveAttribute(R.attr.book_bar, colorDark, true);
                break;
            case MOVIE:
                getTheme().resolveAttribute(R.attr.movie, color, true);
                getTheme().resolveAttribute(R.attr.movie_bar, colorDark, true);
                break;
            case MUSIC:
                getTheme().resolveAttribute(R.attr.music, color, true);
                getTheme().resolveAttribute(R.attr.music_bar, colorDark, true);
                break;
            case PERSONAL:
                getTheme().resolveAttribute(R.attr.personal, color, true);
                getTheme().resolveAttribute(R.attr.personal_bar, colorDark, true);
                break;
            case DEFAULT:
                this.getTheme().resolveAttribute(R.attr.colorPrimary, color, true);
                this.getTheme().resolveAttribute(R.attr.colorPrimaryDark, colorDark, true);
                break;
        }
        actionBarBackground.setColor(getResources().getColor(color.resourceId));
        toolbarBarBackground.setColor(getResources().getColor(colorDark.resourceId));
        floatingMenu.setMenuButtonColorNormal(getResources().getColor(color.resourceId));
        floatingMenu.setMenuButtonColorPressed(getResources().getColor(colorDark.resourceId));
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(actionBarBackground);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(toolbarBarBackground.getColor());
        }
    }

    public void changeBarsColors(String type) {
        final ColorDrawable actionBarBackground = new ColorDrawable();
        final ColorDrawable toolbarBarBackground = new ColorDrawable();
        ActionBar actionBar = getSupportActionBar();

        TypedValue color = new TypedValue();
        TypedValue colorDark = new TypedValue();

        switch (type) {
            case GlobalPreferences.FRAGMENT_TYPE_SETTINGS:
                getTheme().resolveAttribute(R.attr.settings, color, true);
                getTheme().resolveAttribute(R.attr.settings_bar, colorDark, true);
                break;
            case GlobalPreferences.FRAGMENT_TYPE_ABOUT:
                getTheme().resolveAttribute(R.attr.about, color, true);
                getTheme().resolveAttribute(R.attr.about_bar, colorDark, true);
                break;
            default: //Default Values
                this.getTheme().resolveAttribute(R.attr.colorPrimary, color, true);
                this.getTheme().resolveAttribute(R.attr.colorPrimaryDark, colorDark, true);
                break;
        }

        actionBarBackground.setColor(getResources().getColor(color.resourceId));
        toolbarBarBackground.setColor(getResources().getColor(colorDark.resourceId));
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(actionBarBackground);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(toolbarBarBackground.getColor());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("firebaseQuotes", firebaseQuotes);
        outState.putSerializable("localQuotes", localQuotes);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            //Initialice firebase
            Firebase.setAndroidContext(this);
            QuoteDAO.loadFirebaseData(this);
            SQLiteDatabase db = getDbHelper().getReadableDatabase();
            localQuotes = QuoteDAO.getQuotes(db);
            db.close();
        } else {
            //Reload data
            firebaseQuotes = (HashSet<Quote>) savedInstanceState.getSerializable("firebaseQuotes");
            localQuotes = (HashSet<Quote>) savedInstanceState.getSerializable("localQuotes");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setPadding(0, getStatusBarHeight(), 0, 0);

        if (savedInstanceState==null) onNavigationItemSelected(navigationView.getMenu().getItem(0));
    }

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
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
            case R.id.action_about:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, new AboutFragment())
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
        Drawable image = getResources().getDrawable(R.drawable.nav_default);
        switch (id) {
            default:
                f = QuoteListFragment.newInstance(0, QuoteType.DEFAULT);
                break;
            case R.id.nav_music:
                f = QuoteListFragment.newInstance(1, QuoteType.MUSIC);
                image = getResources().getDrawable(R.drawable.nav_music);
                break;
            case R.id.nav_book:
                f = QuoteListFragment.newInstance(2, QuoteType.BOOK);
                image = getResources().getDrawable(R.drawable.nav_book);
                break;
            case R.id.nav_movies:
                f = QuoteListFragment.newInstance(3, QuoteType.MOVIE);
                image = getResources().getDrawable(R.drawable.nav_movie);
                break;
            case R.id.nav_personal:
                f = QuoteListFragment.newInstance(4, QuoteType.PERSONAL);
                image = getResources().getDrawable(R.drawable.nav_personal);
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
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headView = navigationView.getHeaderView(0);
        headView.setBackground(image);
        return true;
    }

    public void onGoogleButtonClick(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/112257302862182562124/posts")));
    }

}
