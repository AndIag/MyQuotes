package es.coru.andiag.myquotes.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.activities.MainActivity;
import es.coru.andiag.myquotes.entities.Quote;
import es.coru.andiag.myquotes.fragments.QuoteListFragment;
import es.coru.andiag.myquotes.utils.GlobalPreferences;
import es.coru.andiag.myquotes.utils.db.QuoteDAO;

/**
 * Created by iagoc on 06/02/2016.
 */
public class AdapterQuotes extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = "AdapterQuotes";
    private Context context;
    private QuoteListFragment quoteListFragment;
    private DateFormat dateF;
    private List<Quote> quoteList = new ArrayList<>();

    public AdapterQuotes(Context context, QuoteListFragment quoteListFragment) {
        this.context = context;
        this.quoteListFragment = quoteListFragment;
        dateF = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, context.getResources().getConfiguration().locale);
    }

    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }

    public List<Quote> getQuoteList() {
        return quoteList;
    }

    public void updateQuotes(Set<Quote> cL) {
        quoteList.clear();
        quoteList.addAll(asSortedList(cL));
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.item_quote, viewGroup, false);
        return new VHQuote(itemView);
    }

    @Override
    public long getItemId(int position) {
        if (quoteList == null) return -1;
        return quoteList.get(position).getQuoteId();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        Quote q = quoteList.get(i);
        VHQuote holder = (VHQuote) viewHolder;

        TypedValue tcolor = new TypedValue();
        switch (q.getType()) {
            case MUSIC:
                context.getTheme().resolveAttribute(R.attr.music, tcolor, true);
                break;
            case MOVIE:
                context.getTheme().resolveAttribute(R.attr.movie, tcolor, true);
                break;
            case PERSONAL:
                context.getTheme().resolveAttribute(R.attr.personal, tcolor, true);
                break;
            case BOOK:
                context.getTheme().resolveAttribute(R.attr.book, tcolor, true);
                break;
            default:
                break;
        }

        int color = context.getResources().getColor(tcolor.resourceId);

        holder.textQuote.setTextColor(color);
        holder.right.setColorFilter(color);
        holder.left.setColorFilter(color);
        holder.textAuthor.setText(q.getAuthor());
        holder.textQuote.setText(q.getQuote());
        context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, tcolor, true);
        holder.buttonShare.setColorFilter(context.getResources().getColor(tcolor.resourceId));
        if (q.isLocal()) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(q.getCreationDate());
            String date = dateF.format(c.getTime());
            holder.textCreationDate.setText(date);
        } else {
            holder.textCreationDate.setText("");
        }
        holder.isLocal = q.isLocal();
    }

    @Override
    public int getItemCount() {
        return quoteList.size();
    }


    public class VHQuote extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView textQuote, textAuthor, textCreationDate;
        CardView cardView;
        ImageView buttonShare,right,left;
        boolean isLocal;

        View v;

        public VHQuote(View itemView) {
            super(itemView);
            this.v = itemView;
            this.cardView = (CardView) v;

            textQuote = (TextView) v.findViewById(R.id.textQuote);
            textAuthor = (TextView) v.findViewById(R.id.textAuthor);
            textCreationDate = (TextView) v.findViewById(R.id.textCreationDate);
            buttonShare = (ImageView) v.findViewById(R.id.buttonShare);
            right = (ImageView) v.findViewById(R.id.imageRight);
            left = (ImageView) v.findViewById(R.id.imageLeft);
            buttonShare.setOnClickListener(this);
            cardView.setOnLongClickListener(this);

        }

        public boolean isLocal() {
            return isLocal;
        }

        @Override
        public void onClick(View v) {
            Quote item = quoteList.get(getAdapterPosition());
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, item.getAuthor() + " : \"" + item.getQuote() + "\"");
            context.startActivity(Intent.createChooser(intent, "Share with"));
        }

        private CharSequence[] getActions(boolean isLocal) {
            if (GlobalPreferences.isAdmin()) {
                if (isLocal) {
                    return new CharSequence[]{
                            context.getString(R.string.admin_action_copy),
                            context.getString(R.string.admin_action_modify),
                            context.getString(R.string.admin_action_remove),
                            context.getString(R.string.admin_action_share_with_us)
                    };
                } else {
                    return new CharSequence[]{
                            context.getString(R.string.admin_action_copy),
                            context.getString(R.string.admin_action_modify),
                            context.getString(R.string.admin_action_remove),
                    };
                }
            } else {
                if (isLocal) {
                    return new CharSequence[]{
                            context.getString(R.string.admin_action_copy),
                            context.getString(R.string.admin_action_share_with_us)
                    };
                } else {
                    return new CharSequence[]{
                            context.getString(R.string.admin_action_copy),
                    };
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            final Quote qitem = quoteList.get(getAdapterPosition());
            final CharSequence[] actions = getActions(qitem.isLocal());
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setItems(actions, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (actions[item].equals(context.getString(R.string.admin_action_modify))) {
                        quoteListFragment.showModifyDialog(qitem);
                    }
                    if (actions[item].equals(context.getString(R.string.admin_action_remove))) {
                        QuoteDAO.removeFirebaseQuote((MainActivity) context, qitem);
                    }
                    if (actions[item].equals(context.getString(R.string.admin_action_copy))) {
                        copyToClipboard();
                    }
                    if (actions[item].equals(context.getString(R.string.admin_action_share_with_us))) {
                        QuoteDAO.shareQuoteToUs(context, quoteListFragment.getView(), qitem);
                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }

        private void copyToClipboard() {
            Quote item = quoteList.get(getAdapterPosition());
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Quote", item.getQuote());
            clipboard.setPrimaryClip(clipData);
            Toast.makeText(context, R.string.clipboard, Toast.LENGTH_SHORT).show();
        }
    }
}