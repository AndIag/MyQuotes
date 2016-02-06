package es.coru.andiag.myquotes.entities;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by iagoc on 06/02/2016.
 */
public class Quote implements Serializable {

    private long quoteId;
    private String quote;
    private QuoteType type;
    private LanguageType language;
    private String author;
    private Calendar creationDate;
    private boolean isLocal;

    public Quote() {
    }

    public Quote(String quote, QuoteType type, String author) {
        this.quote = quote;
        this.type = type;
        this.author = author;
        this.creationDate = Calendar.getInstance();
    }

    public Quote(long id, String quote, QuoteType type, String author) {
        this.quoteId = id;
        this.quote = quote;
        this.type = type;
        this.author = author;
        this.creationDate = Calendar.getInstance();
    }

    public long getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(long quoteId) {
        this.quoteId = quoteId;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public QuoteType getType() {
        return type;
    }

    public void setType(QuoteType type) {
        this.type = type;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Calendar getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    public LanguageType getLanguage() {
        return language;
    }

    public void setLanguage(LanguageType language) {
        this.language = language;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setIsLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quote quote = (Quote) o;

        return quoteId == quote.quoteId && isLocal == quote.isLocal && type == quote.type;

    }

    @Override
    public int hashCode() {
        int result = (int) (quoteId ^ (quoteId >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (isLocal ? 1 : 0);
        return result;
    }
}
