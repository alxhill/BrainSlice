package com.lemonslice.brainslice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.Switch;

/**
 * Created by andy on 19/04/14.
 */
public class AboutDialog {

    private final AlertDialog aboutDialog;

    public AboutDialog(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme));

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.about_dialog, null);

        builder.setTitle("About")
               .setView(linearLayout);

        aboutDialog = builder.create();
    }

    public void show()
    {
        aboutDialog.show();
    }
}
