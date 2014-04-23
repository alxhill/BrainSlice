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
public class SettingsMenu {

    private AlertDialog settingsDialog;

    private Switch colourSwitch;
    private Switch xRaySwitch;

    public SettingsMenu(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme));

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.dialog_settings, null);
        colourSwitch = (Switch) linearLayout.findViewById(R.id.colourSwitch);
        xRaySwitch = (Switch) linearLayout.findViewById(R.id.xRaySwitch);

        builder.setTitle("Settings")
                .setPositiveButton("Close",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //BrainModel.setXRayMode(xRaySwitch.isChecked());
                        //BrainModel.setColourMode(colourSwitch.isChecked());
                        BrainModel.setDisplayMode(xRaySwitch.isChecked(), colourSwitch.isChecked());
                        Log.d("BrainSlice","Dialog: save");
                    }
                })
                .setView(linearLayout);

        settingsDialog = builder.create();
    }

    public void show()
    {
        settingsDialog.show();
    }
}
