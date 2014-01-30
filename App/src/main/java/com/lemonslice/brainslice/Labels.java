package com.lemonslice.brainslice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by James on 29/01/14.
 */
public class Labels
{
    private static BrainInfo brain = new BrainInfo();

    private static View createLabel(LayoutInflater inflater, BrainSegment brainSegment)
    {
        String title = brainSegment.getTitle();
        String description = brainSegment.getDescription();

        LinearLayout label = (LinearLayout)inflater.inflate(R.layout.labels, null);

        TextView titleView = (TextView)label.findViewById(R.id.segment_title);
        titleView.setText(title);

        TextView descView = (TextView)label.findViewById(R.id.segment_description);
        descView.setText(description);

        return label;
    }

    public static View getLabel(LayoutInflater inflater, String brainSegment)
    {
        BrainSegment seg = brain.getSegement(brainSegment);

        if(seg != null)
            return createLabel(inflater, seg);

        return null;
    }
}
