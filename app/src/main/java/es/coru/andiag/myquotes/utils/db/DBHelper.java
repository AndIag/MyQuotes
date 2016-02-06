package es.coru.andiag.myquotes.utils.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by iagoc on 06/02/2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String QUOTES_TABLE = "Quotes";
    public static final String QUOTE_ID = "quoteId";
    public static final String QUOTE = "quote";
    public static final String AUTHOR = "author";
    public static final String CREATION_DATE = "creationDate";
    public static final String TYPE = "type";
    private final static String TAG = "DBHelper";
    private static final int VERSION = 1;

    public DBHelper(Context context) {
        super(context, "quotes.sqlite", null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + QUOTES_TABLE + " (" + QUOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                " " + QUOTE + " TEXT, " + AUTHOR + " TEXT, " + CREATION_DATE + " INTEGER, " + TYPE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + QUOTES_TABLE);
        onCreate(sqLiteDatabase);
    }
}