package es.coru.andiag.myquotes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import es.coru.andiag.myquotes.entities.Quote;
import es.coru.andiag.myquotes.utils.Global;

/**
 * Created by iagoc on 06/02/2016.
 */
public class AdapterQuotes extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = "AdapterQuotes";

    private Context context;
    private List<Quote> quoteList = new ArrayList<>();
    private DateFormat dateF;


    public AdapterQuotes(Context context) {
        this.context = context;
        dateF = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, context.getResources().getConfiguration().locale);
    }

    public List<Quote> getQuoteList() {
        return quoteList;
    }

    public void updateQuotes(List<Quote> cL) {
        this.quoteList = cL;
        notifyDataSetChanged();

    }

    //Add quotes to the adapter removing duplicate instances.
    private void addAll(HashSet<Quote> quotes) {
        for (Quote q : quotes) {
            if (quoteList.contains(q)) {
                quoteList.remove(q);
            }
            quoteList.add(q);
        }
    }

    //Sort the array by date.
    private void sortArray() {
        Comparator<Quote> comparator = new Comparator<Quote>() {
            public int compare(Quote c1, Quote c2) {
                if (c1.getCreationDate().before(c2.getCreationDate())) return -1;
                if (c1.getCreationDate().after(c2.getCreationDate())) return 1;
                return 0;
            }
        };
        Collections.sort(quoteList, comparator);
    }

    public void addQuotes(HashSet<Quote> quotes) {
        if (quotes == null) return;
        addAll(quotes);
        sortArray();
        notifyDataSetChanged();
    }

    public void addQuotes(Quote q) {
        quoteList.add(0, q);
        notifyItemInserted(0);
    }

    public void addQuotes(Quote q, int position) {
        quoteList.add(position, q);
        notifyItemInserted(position);
    }

    public void removeQuote(int position) {
        quoteList.remove(position);
        notifyItemRemoved(position);
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

        int color = context.getResources().getColor(R.color.settings);
        switch (q.getType()) {
            case MUSIC:
                color = context.getResources().getColor(R.color.music);
                break;
            case MOVIE:
                color = context.getResources().getColor(R.color.movie);
                break;
            case PERSONAL:
                color = context.getResources().getColor(R.color.personal);
                break;
            case BOOK:
                color = context.getResources().getColor(R.color.book);
                break;
            default:
                break;
        }
        //holder.cardView.setCardBackgroundColor(color);
        holder.textQuote.setTextColor(color);
        holder.right.setColorFilter(color);
        holder.left.setColorFilter(color);
        holder.textAuthor.setText(q.getAuthor());
        holder.textQuote.setText(q.getQuote());
        String date = dateF.format(q.getCreationDate().getTime());
        holder.textCreationDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return quoteList.size();
    }


    class VHQuote extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView textQuote, textAuthor, textCreationDate;
        CardView cardView;
        ImageView buttonShare,right,left;

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

        //NO SE SI HAY MAS DIFERENCIAS ENTRE EL DE ADMINISTRACION Y EL NORMAL ECHALE UN OJO
        @Override
        public void onClick(View v) {
            if (Global.isAdmin()) {
                //AQUI TIENE QUE HACER LO DE ADMIN + AÑADIR UNA OPCION QUE HAGA LO DE ABAJO
            } else {//AQUI HACE LO DE LITE Y PRO NO SE SI QUIERES HACER QUE PRO PUEDA HACER ALGO MAS
                Quote item = quoteList.get(getAdapterPosition());
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, item.getAuthor() + " : \"" + item.getQuote() + "\"");
                context.startActivity(Intent.createChooser(intent, "Share with"));
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (Global.isAdmin()) {
                //AQUI TIENE QUE HACER LO DE ADMIN + AÑADIR UNA OPCION QUE HAGA LO DE ABAJO
            } else {//AQUI HACE LO DE LITE Y PRO NO SE SI QUIERES HACER QUE PRO PUEDA HACER ALGO MAS
                Quote item = quoteList.get(getAdapterPosition());
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Quote", item.getQuote());
                clipboard.setPrimaryClip(clipData);
                Toast.makeText(context, R.string.clipboard, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }
}