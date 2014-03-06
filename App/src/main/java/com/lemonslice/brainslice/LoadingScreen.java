package com.lemonslice.brainslice;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by James on 06/03/14.
 */
public class LoadingScreen
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
        FrameLayout loadingFrame = (FrameLayout)inflater.inflate(R.layout.loadingscreen, null);
        frameLayout.removeAllViews();
        if (loadingFrame != null) frameLayout.addView(loadingFrame);
    }

    public static void removeView()
    {

        frameLayout.removeAllViews();
    }

    public static void showLoadingScreen()
    {
        inflateView();
        final ProgressBar progressBar = (ProgressBar)frameLayout.findViewById(R.id.progressBarMain);
        progressBar.setProgress(0);
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                frameLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progressBar.getProgress()+1);
                        if ((progressBar.getProgress() > 99) && (renderer.isLoaded()))
                        {
                            LoadingScreen.removeView();
                            cancel();
                        }

                    }
                });
            }
        }, 0, 40);
    }

}
