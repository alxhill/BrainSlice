package com.lemonslice.brainslice;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import java.util.zip.Inflater;

/**
 * Created by James on 06/03/14.
 */
public class OverlayScreen
{
    static Context context;
    static FrameLayout frameLayout;
    static LayoutInflater inflater;
    public static void setContext(Context c)
    {
        context = c;
    }
    public static void setFrameLayout(FrameLayout l)
    {
        frameLayout = l;
    }

    public static void showScreen()
    {
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout calibrateScreen = (FrameLayout)inflater.inflate(R.layout.calibrate_screen, null);
        frameLayout.removeAllViews();
        if (calibrateScreen != null) frameLayout.addView(calibrateScreen);
    }
}
