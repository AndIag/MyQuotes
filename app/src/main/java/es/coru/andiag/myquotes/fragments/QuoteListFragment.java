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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;

import java.util.Set;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.activities.MainActivity;
import es.coru.andiag.myquotes.adapters.AdapterQuotes;
import es.coru.andiag.myquotes.entities.LanguageType;
import es.coru.andiag.myquotes.entities.Quote;
import es.coru.andiag.myquotes.entities.QuoteType;
import es.coru.andiag.myquotes.utils.DialogHelper;
import es.coru.andiag.myquotes.utils.GlobalPreferences;
import es.coru.andiag.myquotes.utils.db.DBHelper;
import es.coru.andiag.myquotes.utils.db.QuoteDAO;
import es.coru.andiag.myquotes.utils.db.QuoteListListener;
import jp.wasabeef.recyclerview.animators.adapters.SlideInLeftAnimationAdapter;

/**
 * Created by iagoc on 06/02/2016.
 */
public class QuoteListFragment extends Fragment implements QuoteListListener {

    private final static String TAG = "QuoteListFragment";

    private static final String ARG_TYPE = "type";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static MainActivity activityMain;
    private QuoteType type;

    private AdapterQuotes adapter;
    private SlideInLeftAnimationAdapter slideAdapter;

    private FloatingActionMenu menu;
    private FloatingActionButton music, book, personal, movie;

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private Quote removedQuote;

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

    public QuoteType getType() {
        return type;
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
        adapter = new AdapterQuotes(activityMain, this);
        slideAdapter = new SlideInLeftAnimationAdapter(adapter);
        slideAdapter.setFirstOnly(false);

    }

    private void createFirebaseQuote(EditText textAuthor, EditText textQuote, QuoteType t) {
        Toast.makeText(activityMain, textAuthor.getText() + " : " + textQuote.getText(), Toast.LENGTH_SHORT).show();
        Quote quote = new Quote(textQuote.getText().toString(), t, textAuthor.getText().toString());
        long id = activityMain.getNextFirebaseId();
        quote.setQuoteId(id);
        quote.setLanguage(LanguageType.UNSET);
        QuoteDAO.addFirebaseQuote(quote);
    }

    private void createQuote(EditText textAuthor, EditText textQuote, QuoteType t) {
        Toast.makeText(activityMain, textAuthor.getText() + " : " + textQuote.getText(), Toast.LENGTH_SHORT).show();
        database = dbHelper.getWritableDatabase();
        Quote quote = new Quote(textQuote.getText().toString(), t, textAuthor.getText().toString());
        long id = QuoteDAO.addQuote(database, quote);
        quote.setQuoteId(id);
        database.close();
        activityMain.notifyDatabaseChange();
    }

    //region Handle the material dialog to add or modify a Quote
    public void showInputDialog(final QuoteType t) {
        final MaterialDialog dialog = DialogHelper.getDialog(activityMain, R.layout.dialog_input, DialogHelper.getIconByType(t), DialogHelper.getColorByType(t));
        View view = dialog.getCustomView();

        if (view == null) {
            throw new NullPointerException();
        }

        final EditText textAuthor = (EditText) view.findViewById(R.id.editTextAuthor);
        final EditText textQuote = (EditText) view.findViewById(R.id.editTextQuote);
        final Spinner s = (Spinner) view.findViewById(R.id.typeSpinner);
        s.setVisibility(View.GONE);

        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean a = !textAuthor.getText().toString().matches("");
                boolean q = !textQuote.getText().toString().matches("");
                if (a && q) {
                    if (GlobalPreferences.isAdmin()) {
                        createFirebaseQuote(textAuthor, textQuote, t);
                    } else {
                        createQuote(textAuthor, textQuote, t);
                    }
                    dialog.dismiss();
                } else {
                    if (!a) textAuthor.setError(activityMain.getString(R.string.error_edit));
                    if (!q) textQuote.setError(activityMain.getString(R.string.error_edit));
                }
            }
        });
    }

    public void showModifyDialog(final Quote oldQuote) {
        if (!GlobalPreferences.isAdmin()) return;

        final QuoteType t = oldQuote.getType();
        final MaterialDialog dialog = DialogHelper.getDialog(activityMain, R.layout.dialog_input, DialogHelper.getIconByType(t), DialogHelper.getColorByType(t));
        View view = dialog.getCustomView();

        if (view == null) {
            throw new NullPointerException();
        }

        final EditText textAuthor = (EditText) view.findViewById(R.id.editTextAuthor);
        textAuthor.setText(oldQuote.getAuthor());

        final EditText textQuote = (EditText) view.findViewById(R.id.editTextQuote);
        textQuote.setText(oldQuote.getQuote());

        final Spinner s = (Spinner) view.findViewById(R.id.typeSpinner);
        ArrayAdapter<QuoteType> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, QuoteType.values());
        s.setAdapter(adapter);
        s.setSelection(adapter.getPosition(t));

        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean a = !textAuthor.getText().toString().matches("");
                boolean q = !textQuote.getText().toString().matches("");
                if (a && q) {
                    //Add to firebase
                    Toast.makeText(activityMain, textAuthor.getText() + " : " + textQuote.getText(), Toast.LENGTH_SHORT).show();
                    Quote quote = new Quote(textQuote.getText().toString(), t, textAuthor.getText().toString());
                    quote.setQuoteId(quote.getQuoteId());
                    quote.setLanguage(LanguageType.UNSET);
                    quote.setType((QuoteType) s.getItemAtPosition(s.getSelectedItemPosition()));
                    QuoteDAO.removeFirebaseQuote(activityMain, oldQuote);
                    QuoteDAO.addFirebaseQuote(quote);
                    dialog.dismiss();
                } else {
                    if (!a) textAuthor.setError(activityMain.getString(R.string.error_edit));
                    if (!q) textQuote.setError(activityMain.getString(R.string.error_edit));
                }
            }
        });
        dialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityMain.notifyListeners();
                dialog.dismiss();
            }
        });
    }
    //endregion

    private void setOnClickListenersToMenu() {
        movie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.close(true);
                showInputDialog(QuoteType.MOVIE);
            }
        });
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.close(true);
                showInputDialog(QuoteType.MUSIC);
            }
        });
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.close(true);
                showInputDialog(QuoteType.BOOK);
            }
        });
        personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.close(true);
                showInputDialog(QuoteType.PERSONAL);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_quote_list, container, false);
        activityMain.changeBarsColors(type, rootView);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        menu = (FloatingActionMenu) rootView.findViewById(R.id.add_menu);
        movie = (FloatingActionButton) rootView.findViewById(R.id.sub_movie);

        book = (FloatingActionButton) rootView.findViewById(R.id.sub_book);
        music = (FloatingActionButton) rootView.findViewById(R.id.sub_music);
        personal = (FloatingActionButton) rootView.findViewById(R.id.sub_personal);

        setOnClickListenersToMenu();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if ((viewHolder instanceof AdapterQuotes.VHQuote) && !(((AdapterQuotes.VHQuote) viewHolder).isLocal())) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

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
                } else return;

                activityMain.notifyDatabaseChange();

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
                                activityMain.notifyDatabaseChange();
                            }
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
        recyclerView.setAdapter(slideAdapter);
        adapter.updateQuotes(activityMain.getQuotesByType(type));
        slideAdapter.notifyDataSetChanged();

        return rootView;
    }

    @Override //Callback for the listener
    public void notifyDataSetChanged() {
        if (adapter != null) {
            adapter.updateQuotes(activityMain.getQuotesByType(type));
            slideAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifySearch(Set<Quote> quotes) {
        if (adapter != null && quotes != null) {
            adapter.updateQuotes(quotes);
            slideAdapter.notifyDataSetChanged();
        }
    }
}