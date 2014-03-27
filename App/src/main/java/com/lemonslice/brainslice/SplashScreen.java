package com.lemonslice.brainslice;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.lemonslice.brainslice.event.Tutorial;

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
    static MainActivity.MyRenderer renderer;
    public static void setContext(Context c)
    {
        context = c;
    }
    public static void setFrameLayout(FrameLayout l)
    {
        frameLayout = l;
    }
    public static void setRenderer(MainActivity.MyRenderer r) {renderer = r; }

    public static void inflateView()
    {
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout loadingFrame = (FrameLayout)inflater.inflate(R.layout.loading_screen, null);
        frameLayout.removeAllViews();
        if (loadingFrame != null) frameLayout.addView(loadingFrame);
    }

    public static void finished()
    {
        frameLayout.removeAllViews();
        Tutorial.show();
    }

    public static void show()
    {
        //inflate and show the xml loading screen
        inflateView();

        //timeout thing (will be removed)
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                frameLayout.post(new Runnable() {
                    @Override
                    public void run() {

                        if ((renderer.isLoaded()))
                        {
                            SplashScreen.finished();
                            cancel();
                        }

                    }
                });
            }
        }, 0, 3000);
    }

}
