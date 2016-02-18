package es.coru.andiag.myquotes.utils;

import android.app.Activity;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.entities.QuoteType;

/**
 * Created by Canalejas on 07/02/2016.
 */
public abstract class DialogHelper {

    public static int getColorByType(QuoteType t) {
        switch (t) {
            case MOVIE:
                return R.color.movie;
            case MUSIC:
                return R.color.music;
            case PERSONAL:
                return R.color.personal;
            case BOOK:
                return R.color.book;
            default:
                return R.color.settings;
        }
    }

    public static int getIconByType(QuoteType t) {
        switch (t) {
            case MOVIE:
                return R.drawable.movie;
            case MUSIC:
                return R.drawable.music;
            case PERSONAL:
                return R.drawable.personal;
            case BOOK:
                return R.drawable.book;
            default:
                return R.drawable.settings;
        }
    }

    public static com.afollestad.materialdialogs.MaterialDialog getDialog(Activity activity, int layout, int icon, int color) {
        return new com.afollestad.materialdialogs.MaterialDialog.Builder(activity)
                .title(R.string.dialog_input)
                .titleColorRes(R.color.white)
                .customView(layout, true)
                .iconRes(icon)
                .limitIconToDefaultSize()
                .backgroundColorRes(color)
                .positiveText(R.string.button_ok)
                .positiveColorRes(R.color.white)
                .negativeText(R.string.button_cancel)
                .negativeColorRes(R.color.white)
                .show();
    }

}
