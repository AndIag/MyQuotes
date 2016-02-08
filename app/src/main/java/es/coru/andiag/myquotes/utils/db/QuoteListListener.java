package es.coru.andiag.myquotes.utils.db;

import java.util.Set;

import es.coru.andiag.myquotes.entities.Quote;

/**
 * Created by Canalejas on 11/10/2015.
 */
public interface QuoteListListener {
    void notifyDataSetChanged();

    void notifySearch(Set<Quote> quotes);
}
