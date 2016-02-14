package es.coru.andiag.myquotes.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import es.coru.andiag.myquotes.R;
import es.coru.andiag.myquotes.activities.MainActivity;
import es.coru.andiag.myquotes.utils.GlobalPreferences;

/**
 * Created by Canalejas on 10/02/2016.
 */
public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.about, container, false);
        ((MainActivity) getActivity()).changeBarsColors(GlobalPreferences.FRAGMENT_TYPE_ABOUT);
        return rootView;
    }
}
