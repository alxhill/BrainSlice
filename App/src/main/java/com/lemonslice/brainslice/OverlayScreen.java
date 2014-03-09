package com.lemonslice.brainslice;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.zip.Inflater;

/**
 * Created by James on 06/03/14.
 */
public class OverlayScreen
{
    static Context context;
    static FrameLayout frameLayout;
    static LayoutInflater inflater;
    static VisualiseController visualiseController;
    public static void setContext(Context c)
    {
        context = c;
    }
    public static void setFrameLayout(FrameLayout l)
    {
        frameLayout = l;
    }
    public static void setVisualiseController(VisualiseController vc) { visualiseController = vc; }

    public static void showScreen(int layoutID)
    {
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout calibrateScreen = (FrameLayout)inflater.inflate(layoutID, null);

        Button calibrateBtn =(Button)calibrateScreen.findViewById(R.id.calibrateOverlayBtn);
        calibrateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visualiseController.loadView();
                hideScreen();
            }
        });


        frameLayout.removeAllViews();
        if (calibrateScreen != null) frameLayout.addView(calibrateScreen);
    }

    public static void hideScreen()
    {
        frameLayout.removeAllViews();
    }
}
