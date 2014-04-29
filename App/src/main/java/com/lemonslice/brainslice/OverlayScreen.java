package com.lemonslice.brainslice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;

import com.lemonslice.brainslice.event.Events;

/**
 * Created by James on 06/03/14.
 */
public class OverlayScreen
{
    private static Context context;
    private static FrameLayout frameLayout;
    private static LayoutInflater inflater;

    public static void setContext(Context c)
    {
        context = c;
    }
    public static void setFrameLayout(FrameLayout l)
    {
        frameLayout = l;
    }

    public static void showScreen(int layoutID)
    {
        if (inflater == null)
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout calibrateScreen = (FrameLayout)inflater.inflate(layoutID, null);

        assert calibrateScreen != null;

        Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        fadeIn.setDuration(250);
        calibrateScreen.startAnimation(fadeIn);

        Button calibrateBtn = (Button)calibrateScreen.findViewById(R.id.calibrateOverlayBtn);
        calibrateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Events.trigger("tap:calibrate");
                hideScreen();
            }
        });

        frameLayout.removeAllViews();
        frameLayout.addView(calibrateScreen);
    }

    public static void hideScreen()
    {
        Animation fadeOut = AnimationUtils.loadAnimation(context,R.anim.abc_fade_out);
        fadeOut.setDuration(250);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                frameLayout.removeAllViews();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        frameLayout.startAnimation(fadeOut);
    }
}
