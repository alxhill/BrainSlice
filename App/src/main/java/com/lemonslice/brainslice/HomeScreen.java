package com.lemonslice.brainslice;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.threed.jpct.SimpleVector;


/**
 * Created by James on 24/04/2014.
 */
public class HomeScreen {
    static Context context;
    static FrameLayout frameLayout;
    static LayoutInflater inflater;
    public static void setContext(Context c)
    {
        context = c;
    }
    public static void setFrameLayout(FrameLayout l){frameLayout = l; }
    private static Typeface fontAwesome;
    private GestureDetector gestureDetector = null;

    private  static int btncount = 0;

    public static void show() {
        btncount = 0;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout homeScreen = (FrameLayout)inflater.inflate(R.layout.home_screen, null);

        assert homeScreen != null;

        frameLayout.removeAllViews();
        frameLayout.addView(homeScreen);

        Animation fadeIn = AnimationUtils.loadAnimation(context,R.anim.fade_in);
        fadeIn.setDuration(250);
        homeScreen.startAnimation(fadeIn);

        LinearLayout btn_holder = (LinearLayout)homeScreen.findViewById(R.id.hs_button_holder);
        LinearLayout btn1 = (LinearLayout)btn_holder.findViewById(R.id.hs_learnbtn);
        LinearLayout btn2 = (LinearLayout)btn_holder.findViewById(R.id.hs_visualizebtn);
        LinearLayout btn3 = (LinearLayout)btn_holder.findViewById(R.id.hs_quizbtn);

        MainActivity.addButtonListener(btn1, "learn");
        MainActivity.addButtonListener(btn2, "visualise");
        MainActivity.addButtonListener(btn3, "quiz");

        LinearLayout icon_holder = (LinearLayout)homeScreen.findViewById(R.id.hs_icon_holder);


        iconifyView(R.id.hs_help_button_icon, 25);
        iconifyView(R.id.hs_settings_button_icon, 25);
        iconifyView(R.id.hs_volume_button, 25);
        iconifyView(R.id.hs_about_button,25);

        /*
        btn1.setVisibility(View.INVISIBLE);
        btn2.setVisibility(View.INVISIBLE);
        btn3.setVisibility(View.INVISIBLE);
        icon_holder.setVisibility(View.INVISIBLE);

        Animation slideIn1 = AnimationUtils.loadAnimation(context, R.anim.slide_in);
        Animation slideIn2 = AnimationUtils.loadAnimation(context, R.anim.slide_in);
        Animation slideIn3 = AnimationUtils.loadAnimation(context, R.anim.slide_in);
        slideIn1.setFillAfter(true);
        slideIn2.setFillAfter(true);
        slideIn3.setFillAfter(true);
        slideIn1.setInterpolator(new AccelerateInterpolator());
        slideIn2.setInterpolator(new AccelerateInterpolator());
        slideIn3.setInterpolator(new AccelerateInterpolator());

        slideIn1.setStartOffset(500);
        btn1.startAnimation(slideIn1);

        slideIn2.setStartOffset(600);
        btn2.startAnimation(slideIn2);

        slideIn3.setStartOffset(700);
        btn3.startAnimation(slideIn3);

        Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        slideDown.setFillAfter(true);
        slideDown.setInterpolator(new DecelerateInterpolator());
        slideDown.setStartOffset(600);
        icon_holder.startAnimation(slideDown);*/

        BrainModel.showBrain = true;
        //BrainModel.smoothMoveToGeneric(BrainModel.homePosition, 120, 400);
        //BrainModel.smoothRotateToFront();
        //BrainModel.smoothZoom(0.23f, 1200);
        BrainModel.setLabelsToDisplay(false);
        //BrainModel.onlyRotateY = true;
        //BrainModel.disableDoubleTap = true;
        MainActivity.setZOnTop();
    }

    public static void hide()
    {
        Animation fadeOut = AnimationUtils.loadAnimation(context,R.anim.abc_fade_out);
        fadeOut.setDuration(500);
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

    private static TextView iconifyView(int resId, int size)
    {
        if (fontAwesome == null)
            fontAwesome = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");

        TextView view = (TextView) frameLayout.findViewById(resId);
        view.setTypeface(fontAwesome);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        return view;
    }
}
