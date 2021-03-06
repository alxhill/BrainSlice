package com.lemonslice.brainslice;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by James on 29/01/14.
 * Takes in inflator (for inflating labels xml) and string (the brain segment)
 * Returns View (a linear layout) of the corresponding brain seg label
 */
public class Labels
{
    private static FrameLayout frameLayout;
    private static Context context;
    private static FrameLayout parent;
    private static TextView labelOverlay;

    private static View createLabel(LayoutInflater inflater, BrainSegment brainSegment)
    {
        //get title and description of segment
        String title = brainSegment.getTitle();
        String description = brainSegment.getDescription();

        //inflate layout from labels.xml into a view (a linear layout)
        LinearLayout label = (LinearLayout)inflater.inflate(R.layout.labels, null);

        //Set the text of the title of the Segment (in small Caps)
        assert label != null;
        TextView titleView = (TextView)label.findViewById(R.id.segment_title);
        titleView.setText(title);

        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);

//        titleView = Utils.getSmallCaps(titleView);

        //Set the text of the description text view
        TextView descView = (TextView)label.findViewById(R.id.segment_description);
        descView.setText(description);

        //return view (linear layout)
        return label;
    }

    private static View getLabel(LayoutInflater inflater, String brainSegment)
    {
        BrainSegment seg = BrainInfo.getSegment(brainSegment);

        //check if segment exists
        if(seg != null)
            return createLabel(inflater, seg);
        else
            Log.e("Brain Slice", "No slice found");

        return null;
    }

    public static void displayLabel(String brainSegment)
    {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        frameLayout.removeAllViews();

        if(Labels.getLabel(inflater, brainSegment) != null)
        {
            frameLayout.addView(Labels.getLabel(inflater, brainSegment));

            if(labelOverlay != null)
                labelOverlay.setVisibility(View.INVISIBLE);
        }
    }

    public static void removeLabels()
    {
        frameLayout.removeAllViews();

        assert labelOverlay != null;
        labelOverlay.setVisibility(View.VISIBLE);

    }

    public static void setFrameLayout(FrameLayout l)
    {
        frameLayout = l;
        parent = (FrameLayout)frameLayout.getParent();
        assert parent != null;
        labelOverlay = (TextView)parent.findViewById(R.id.label_overlay);
    }

    public static void setContext(Context c)
    {
        context = c;
    }
}
