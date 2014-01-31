package com.lemonslice.brainslice;

import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by James on 29/01/14.
 */
public class Labels
{
    private static View createLabel(LayoutInflater inflater, BrainSegment brainSegment)
    {
        String title = brainSegment.getTitle();
        String description = brainSegment.getDescription();

        LinearLayout label = (LinearLayout)inflater.inflate(R.layout.labels, null);

        TextView titleView = (TextView)label.findViewById(R.id.segment_title);
        titleView = Utils.getSmallCaps(titleView);

        TextView descView = (TextView)label.findViewById(R.id.segment_description);
        descView.setText(description);

        return label;
    }

    public static View getLabel(LayoutInflater inflater, String brainSegment)
    {
        BrainSegment seg = BrainInfo.getSegment(brainSegment);

        if(seg != null)
            return createLabel(inflater, seg);

        return null;
    }
}
