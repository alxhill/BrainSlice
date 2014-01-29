package com.lemonslice.brainslice;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by James on 29/01/14.
 */
public class Labels
{
    private static BrainInfo brain = new BrainInfo();

    private static Toast createLabel(Context c, BrainSegment brainSegment)
    {
        String title = brainSegment.getTitle();
        String description = brainSegment.getDescription();

        return Toast.makeText(c, title+"\n"+description, 3000);
    }

    public static Toast getLabel(Context c, String brainSegment)
    {
        BrainSegment seg = brain.getSegement(brainSegment);
        if(seg != null)
            return createLabel(c, seg);

        return null;
    }
}
