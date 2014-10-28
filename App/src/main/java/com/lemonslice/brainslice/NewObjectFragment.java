package com.lemonslice.brainslice;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by James on 28/10/2014.
 */

public class NewObjectFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;
        Bundle args = getArguments();
        int card = args.getInt(ARG_OBJECT);
        if ((!Tutorial.showMainMenu)&&(card>=2))
            card++;
        switch(card)
        {
            case 1: rootView = inflater.inflate(R.layout.card_left, container, false); break;
            case 2: rootView = inflater.inflate(R.layout.card_first, container, false);
                Tutorial.comicNeue((TextView) rootView.findViewById(R.id.welcome_message), 50);
                break;
            case 3: rootView = inflater.inflate(R.layout.card2, container, false);
                Tutorial.comicNeue((TextView) rootView.findViewById(R.id.tutorial_learn_title), 40);
                break;
            case 4: rootView = inflater.inflate(R.layout.card3, container, false);
                Tutorial.comicNeue((TextView) rootView.findViewById(R.id.tutorial_visualize_title), 40);
                break;
            case 5: rootView = inflater.inflate(R.layout.card4, container, false);
                Tutorial.comicNeue((TextView) rootView.findViewById(R.id.tutorial_quiz_title), 40);
                break;
            case 6: rootView = inflater.inflate(R.layout.card5, container, false);
                Tutorial.comicNeue((TextView) rootView.findViewById(R.id.tutorial_settings_title), 40);
                break;
            case 7: rootView = inflater.inflate(R.layout.card_final, container, false);
                break;
            default: break;
        }

        return rootView;
    }
}
