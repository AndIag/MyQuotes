package es.coru.andiag.myquotes.utils.db;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.activities.BaseActivity;
import es.coru.andiag.myquotes.activities.MainActivity;
import es.coru.andiag.myquotes.entities.LanguageType;
import es.coru.andiag.myquotes.entities.Quote;
import es.coru.andiag.myquotes.entities.QuoteType;
import es.coru.andiag.myquotes.utils.GlobalPreferences;

/**
 * Created by iagoc on 06/02/2016.
 */
public abstract class QuoteDAO {

    private final static String TAG = "QuoteDAO";
    private static final Firebase myFirebaseRef;
    private static final Firebase myFirebaseRefShare;
    private static ValueEventListener listener;

    static {
        myFirebaseRef = new Firebase("https://myquotesandroid.firebaseio.com/");
        myFirebaseRefShare = new Firebase("https://myquotesandroidshare.firebaseio.com/");
    }

    //region LocalDB Methods
    private static URL getURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashSet<Quote> getQuotes(SQLiteDatabase db) {
        HashSet<Quote> arrayList = new HashSet<>();
        Quote quote;

        //Search local quotes in db
        String execute = "SELECT * FROM " + DBHelper.QUOTES_TABLE + " ORDER BY " + DBHelper.CREATION_DATE + " DESC";
        Cursor cursor = db.rawQuery(execute, null);

        while (cursor != null && cursor.moveToNext()) {
            quote = new Quote();
            quote.setQuoteId(cursor.getLong(cursor.getColumnIndex(DBHelper.QUOTE_ID)));
            quote.setQuote(cursor.getString(cursor.getColumnIndex(DBHelper.QUOTE)));
            quote.setAuthor(cursor.getString(cursor.getColumnIndex(DBHelper.AUTHOR)));
            quote.setType(QuoteType.valueOf(cursor.getString(cursor.getColumnIndex(DBHelper.TYPE))));
            quote.setIsLocal(true);
            quote.setCreationDate(cursor.getLong(cursor.getColumnIndex(DBHelper.CREATION_DATE)) * 1000);
            quote.setUrl(getURL(cursor.getString(cursor.getColumnIndex(DBHelper.URL))));
            arrayList.add(quote);
        }
        if (cursor != null) cursor.close();

        return arrayList;
    }

    public static long addQuote(SQLiteDatabase db, Quote quote) {
        if (quote == null) return -1;
        ContentValues c = new ContentValues();
        c.put(DBHelper.QUOTE, quote.getQuote());
        c.put(DBHelper.AUTHOR, quote.getAuthor());
        c.put(DBHelper.CREATION_DATE, quote.getCreationDate() / 1000);
        c.put(DBHelper.TYPE, quote.getType().toString());
        c.put(DBHelper.URL, "");
        return db.insert(DBHelper.QUOTES_TABLE, null, c);
    }

    public static boolean removeQuote(SQLiteDatabase db, long id) {
        return db.delete(DBHelper.QUOTES_TABLE, DBHelper.QUOTE_ID + "=" + id, null) > 0;
    }

    //endregion
    //region Firebase Methods
    public static void cleanListener(){
        if(listener!=null){
            myFirebaseRef.removeEventListener(listener);
        }
    }
    public static void loadFirebaseData(final MainActivity activity) {
        if (GlobalPreferences.mustSync(activity)) {
            listener = myFirebaseRef.addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<Quote> quotes = new ArrayList<>();
                            Set<Integer> languages = GlobalPreferences.getSyncLanguages(activity);
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Quote quote = snapshot.getValue(Quote.class);
                                quote.setIsLocal(false);
                                if (languages.contains(quote.getLanguage().ordinal())) {
                                    quotes.add(quote);
                                }
                            }
                            activity.addQuotes(quotes);
                        }
        
                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });

        }
    }

    public static void removeFirebaseQuote(final MainActivity activity, Quote q) {
        if (GlobalPreferences.isAdmin()) {
            myFirebaseRef.child(String.valueOf(q.getQuoteId())).removeValue();
            activity.removeQuote(q);
            activity.notifyListeners();
        }
    }

    public static void shareQuoteToUs(Context context, View view, Quote q) {
        if (q.isLocal()) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String newId = telephonyManager.getDeviceId() + String.valueOf(q.getQuoteId());
                q.setQuoteId(Long.parseLong(newId));
                myFirebaseRefShare.child(String.valueOf(q.getQuoteId())).setValue(new QuoteDTO(q));
                Toast.makeText(context, context.getResources().getString(R.string.shared), Toast.LENGTH_SHORT).show();
            } else {
                ((BaseActivity) context).requestFeature(Manifest.permission.READ_PHONE_STATE, view, q);
            }
        }
    }

    public static void addFirebaseQuote(Quote q) {
        if (GlobalPreferences.isAdmin()) {
            myFirebaseRef.child(String.valueOf(q.getQuoteId())).setValue(new QuoteDTO(q));
        }
    }
    //endregion

    private static class QuoteDTO implements Serializable {

        private long quoteId;
        private String author;
        private long creationDate;
        private int language;
        private String quote;
        private int type;
        private String url;

        public QuoteDTO(Quote q) {
            this.quoteId = q.getQuoteId();
            this.author = q.getAuthor();
            this.creationDate = q.getCreationDate();
            this.quote = q.getQuote();
            this.type = q.getType().ordinal();
            this.url = q.getUrl().toString();
            this.language = LanguageType.UNSET.ordinal();
            if (q.getLanguage() != null) {
                this.language = q.getLanguage().ordinal();
            }
        }

        public long getQuoteId() {
            return quoteId;
        }

        public void setQuoteId(long quoteId) {
            this.quoteId = quoteId;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public long getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(long creationDate) {
            this.creationDate = creationDate;
        }

        public int getLanguage() {
            return language;
        }

        public void setLanguage(int language) {
            this.language = language;
        }

        public String getQuote() {
            return quote;
        }

        public void setQuote(String quote) {
            this.quote = quote;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

}
