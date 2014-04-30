package com.lemonslice.brainslice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Switch;

/**
 * Created by andy on 19/04/14.
 */
public class AboutDialog {

    private final AlertDialog aboutDialog;
    private final Context context;

    public AboutDialog(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme));

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.about_dialog, null);

        builder.setView(linearLayout);

        aboutDialog = builder.create();
    }

    public void show()
    {
        aboutDialog.show();
    }
}
