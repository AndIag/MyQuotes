package es.coru.andiag.myquotes.fragments;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;

import java.util.ArrayList;
import java.util.HashSet;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.activities.MainActivity;
import es.coru.andiag.myquotes.adapters.AdapterQuotes;
import es.coru.andiag.myquotes.entities.Quote;
import es.coru.andiag.myquotes.entities.QuoteType;
import es.coru.andiag.myquotes.utils.QuoteListListener;
import es.coru.andiag.myquotes.utils.db.DBHelper;
import es.coru.andiag.myquotes.utils.db.QuoteDAO;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import jp.wasabeef.recyclerview.animators.adapters.SlideInLeftAnimationAdapter;

/**
 * Created by iagoc on 06/02/2016.
 */
public class QuoteListFragment extends Fragment implements QuoteListListener {

    private final static String TAG = "QuoteListFragment";

    private static final String ARG_TYPE = "type";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final QuoteType MAIN_SECTION = QuoteType.DEFAULT;
    private static MainActivity activityMain;
    private QuoteType type;

    private AdapterQuotes adapter;
    private SlideInLeftAnimationAdapter slideAdapter;

    private FloatingActionsMenu menu;
    private FloatingActionButton music, book, personal, movie;

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private ArrayList<Quote> quotes;
    private Quote removedQuote;
    private int remoteQuotePosition;


    //region Fragment Initialice Methods
    public QuoteListFragment() {
        // Required empty public constructor
    }

    public static QuoteListFragment newInstance(int sectionNumber, QuoteType type) {
        QuoteListFragment fragment = new QuoteListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TYPE, type);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activityMain = (MainActivity) activity;
        dbHelper = activityMain.getDbHelper();
    }

    @Override
    public void onStart() {
        super.onStart();
        //Register this fragment as listener in case our db change
        activityMain.registerListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        //UnRegister this fragment as listener
        activityMain.unregisterListener(this);
    }
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = (QuoteType) getArguments().getSerializable(ARG_TYPE);
        }
        adapter = new AdapterQuotes(activityMain);
        slideAdapter = new SlideInLeftAnimationAdapter(adapter);
        slideAdapter.setFirstOnly(false);
        database = dbHelper.getReadableDatabase();
        if (type == MAIN_SECTION) { //If we are in the main section we load all our inner data and our db data to.
            quotes = QuoteDAO.getQuotes(database);
        } else { //Else we just load one type of data.
            quotes = QuoteDAO.findQuotesByType(database, type);
        }
        database.close();
    }

    protected void showInputDialog(final QuoteType t) {
        int color, icon;
        switch (t) {
            case MOVIE:
                color = R.color.movie;
                icon = R.drawable.movie;
                break;
            case MUSIC:
                color = R.color.music;
                icon = R.drawable.music;
                break;
            case PERSONAL:
                icon = R.drawable.personal;
                color = R.color.personal;
                break;
            case BOOK:
                icon = R.drawable.book;
                color = R.color.book;
                break;
            default:
                color = R.color.settings;
                icon = R.drawable.settings;
        }

        final MaterialDialog dialog = new MaterialDialog.Builder(activityMain)
                .title(R.string.dialog_input)
                .titleColorRes(R.color.white)
                .customView(R.layout.dialog_input, true)
                .iconRes(icon)
                .limitIconToDefaultSize()
                .backgroundColorRes(color)
                .positiveText(R.string.button_ok)
                .positiveColorRes(R.color.white)
                .negativeText(R.string.button_cancel)
                .negativeColorRes(R.color.white)
                .show();

        View view = dialog.getCustomView();

        final EditText textAuthor = (EditText) view.findViewById(R.id.editTextAuthor);
        final EditText textQuote = (EditText) view.findViewById(R.id.editTextQuote);

        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean a = !textAuthor.getText().toString().matches("");
                boolean q = !textQuote.getText().toString().matches("");
                if (a && q) {
                    Toast.makeText(activityMain, textAuthor.getText() + " : " + textQuote.getText(), Toast.LENGTH_SHORT).show();
                    database = dbHelper.getWritableDatabase();
                    Quote quote = new Quote(textQuote.getText().toString(), t, textAuthor.getText().toString());
                    long id = QuoteDAO.addQuote(database, quote);
                    quote.setQuoteId(id);
                    dialog.dismiss();
                    adapter.addQuotes(quote);
                    slideAdapter.notifyItemInserted(0);
                    database.close();
                } else {
                    if (!a) textAuthor.setError(activityMain.getString(R.string.error_edit));
                    if (!q) textQuote.setError(activityMain.getString(R.string.error_edit));
                }
            }
        });
    }

    private void setOnClickListenersToMenu() {
        movie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.collapse();
                showInputDialog(QuoteType.MOVIE);
            }
        });
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.collapse();
                showInputDialog(QuoteType.MUSIC);
            }
        });
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.collapse();
                showInputDialog(QuoteType.BOOK);
            }
        });
        personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.collapse();
                showInputDialog(QuoteType.PERSONAL);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activityMain.changeBarsColors(type);
        View rootView = inflater.inflate(R.layout.fragment_quote_list, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        menu = (FloatingActionsMenu) rootView.findViewById(R.id.add_menu);
        movie = (FloatingActionButton) rootView.findViewById(R.id.sub_movie);
        book = (FloatingActionButton) rootView.findViewById(R.id.sub_book);
        music = (FloatingActionButton) rootView.findViewById(R.id.sub_music);
        personal = (FloatingActionButton) rootView.findViewById(R.id.sub_personal);

        setOnClickListenersToMenu();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                removedQuote = adapter.getQuoteList().get(viewHolder.getPosition());
                if (removedQuote.isLocal()) {
                    database = dbHelper.getReadableDatabase();
                    QuoteDAO.removeQuote(database, removedQuote.getQuoteId());
                    database.close();
                } else {
                    activityMain.removeQuote(removedQuote);
                }

                remoteQuotePosition = viewHolder.getPosition();
                adapter.removeQuote(viewHolder.getPosition());
                slideAdapter.notifyItemRemoved(viewHolder.getPosition());

                SuperActivityToast superActivityToast = new SuperActivityToast(activityMain, SuperToast.Type.BUTTON);
                superActivityToast.setDuration(SuperToast.Duration.EXTRA_LONG);
                superActivityToast.setText(getString(R.string.removed));
                superActivityToast.setButtonIcon(SuperToast.Icon.Dark.UNDO, getString(R.string.undo));
                superActivityToast.setOnClickWrapper(new OnClickWrapper("superactivitytoast", new SuperToast.OnClickListener() {

                    @Override
                    public void onClick(View view, Parcelable token) {
                        if (removedQuote != null) {
                            if (removedQuote.isLocal()) {
                                database = dbHelper.getWritableDatabase();
                                QuoteDAO.addQuote(database, removedQuote);
                                database.close();
                            } else {
                                activityMain.addQuotes(removedQuote);
                            }
                            adapter.addQuotes(removedQuote, remoteQuotePosition);
                            slideAdapter.notifyItemInserted(remoteQuotePosition);
                            removedQuote = null;
                        }
                    }

                }));
                superActivityToast.show();

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activityMain);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setItemAnimator(new ScaleInAnimator());
        recyclerView.setAdapter(slideAdapter);
        if (quotes == null) quotes = new ArrayList<>();
        adapter.updateQuotes(quotes);
        slideAdapter.notifyDataSetChanged();

        return rootView;
    }

    //In this method we create an array with only our type quotes
    private HashSet<Quote> getMyQuotes(HashSet<Quote> quotes) {
        HashSet<Quote> q = new HashSet<>();
        for (Quote quote : quotes) {
            if (quote.getType() == type) {
                q.add(quote);
            }
        }
        return q;
    }

    @Override //Callback for the listener
    public void notifyDataSetChanged() {
        if (adapter != null) {
            if (type == MAIN_SECTION) {
                //Get all quotes
                adapter.addQuotes(activityMain.getFirebaseQuotes());
            } else {
                //Get quotes by type
                adapter.addQuotes(getMyQuotes(activityMain.getFirebaseQuotes()));
            }
            slideAdapter.notifyDataSetChanged();
        }
    }

}