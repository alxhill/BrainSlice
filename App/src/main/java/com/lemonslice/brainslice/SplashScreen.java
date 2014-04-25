package com.lemonslice.brainslice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
    }

    public static void show()
    {
        BrainModel.showBrain = false;
        BrainModel.disableDoubleTap = true;
        //inflate and show the xml loading screen
        FrameLayout splashScreen = (FrameLayout)inflateView();
        final ImageView lemon = (ImageView)splashScreen.findViewById(R.id.lemon);
        final TextView teamName = (TextView)splashScreen.findViewById(R.id.team_lemon_slice);

        lemon.setVisibility(View.INVISIBLE);
        teamName.setVisibility(View.INVISIBLE);

        //scale from 0 to 1.2
        final Animation scaleLemon = AnimationUtils.loadAnimation(context, R.anim.scale_up);
        final Animation scaleTeamName = AnimationUtils.loadAnimation(context, R.anim.scale_up);
        scaleLemon.setFillAfter(true);
        scaleTeamName.setFillAfter(true);
        scaleLemon.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleTeamName.setInterpolator(new AccelerateDecelerateInterpolator());

        //scale from 1.2 to 1
        final Animation scaleBack = AnimationUtils.loadAnimation(context, R.anim.scale_back);
        scaleBack.setFillAfter(true);

        final Animation scaleExitLemon = AnimationUtils.loadAnimation(context, R.anim.scale_up_small);
        final Animation scaleExitTeamName = AnimationUtils.loadAnimation(context, R.anim.scale_up_small);
        scaleExitLemon.setFillAfter(true);
        scaleExitTeamName.setFillAfter(true);

        final Animation scaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down);
        scaleDown.setFillAfter(true);
        scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleLemon.setAnimationListener(new MyAnimationListener(lemon) {
            @Override
            public void onAnimationEnd(Animation animation) {
                teamName.startAnimation(scaleTeamName);
                view.startAnimation(scaleBack);
            }
        });
        scaleTeamName.setAnimationListener(new MyAnimationListener(teamName) {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(scaleBack);
            }
        });
        scaleExitLemon.setAnimationListener(new MyAnimationListener(lemon) {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(scaleDown);
            }
        });
        scaleExitTeamName.setAnimationListener(new MyAnimationListener(teamName) {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(scaleDown);
            }
        });

        //NOBODY LOOK AT THIS CODE!!!
        //wait for screen orient ro load properly
        //REPLACE WITH START TIME OFFSETS
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                frameLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        lemon.startAnimation(scaleLemon);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                frameLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        SplashScreen.finished_splash();
                                    }
                                });
                            }
                        }, 3000 /*Time for lemon animation*/);
                        cancel();
                    }
                });
            }
        }, 1000);
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
