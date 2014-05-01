package com.lemonslice.brainslice;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by James on 06/03/14.
 */
public class SplashScreen
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

    public static View inflateView()
    {
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout loadingFrame = (FrameLayout)inflater.inflate(R.layout.splash_screen, null);

        frameLayout.removeAllViews();
        if (loadingFrame != null) frameLayout.addView(loadingFrame);

        return loadingFrame;
    }

    public static void finished_splash()
    {
        frameLayout.removeAllViews();
        HomeScreen.show();
        Tutorial.show(1,true);
    }

    public static void show()
    {
        BrainModel.showBrain = false;
        BrainModel.disableDoubleTap = true;
        //inflate and show the xml loading screen
        FrameLayout splashScreen = (FrameLayout)inflateView();
        //final ImageView lemon = (ImageView)splashScreen.findViewById(R.id.lemon);
        //final TextView teamName = (TextView)splashScreen.findViewById(R.id.team_lemon_slice);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                frameLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        finished_splash();
                    }
                });
                cancel();
            }
        }, 3000);

        Log.d("JAMES", "End of function???");
    }
}

class MyAnimationListener implements Animation.AnimationListener {
    View view;
    public MyAnimationListener(View view)
    {
        this.view = view;
    }
    public void onAnimationEnd(Animation animation) {
    }
    public void onAnimationRepeat(Animation animation) {
    }
    public void onAnimationStart(Animation animation) {
    }
}
