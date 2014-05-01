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
import android.widget.ImageView;
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

    private static int btncount = 0;
    private static Typeface comicNeue;
    private static Typeface germanBeauty;

    public static void show() {
        btncount = 0;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout homeScreen = (FrameLayout)inflater.inflate(R.layout.home_screen, null);

        assert homeScreen != null;

        //catch taps falling through view into activity main
        homeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                return;
            }
        });

        frameLayout.removeAllViews();
        frameLayout.addView(homeScreen);

        Animation fadeIn = AnimationUtils.loadAnimation(context,R.anim.fade_in);
        fadeIn.setDuration(250);
        homeScreen.startAnimation(fadeIn);

        LinearLayout btn_holder = (LinearLayout)homeScreen.findViewById(R.id.hs_button_holder);
        final LinearLayout btn1 = (LinearLayout)btn_holder.findViewById(R.id.hs_learnbtn);
        LinearLayout btn2 = (LinearLayout)btn_holder.findViewById(R.id.hs_visualizebtn);
        LinearLayout btn3 = (LinearLayout)btn_holder.findViewById(R.id.hs_quizbtn);

        ImageView spiral = (ImageView) homeScreen.findViewById(R.id.imageView);

        final Animation rotateSpiral = AnimationUtils.loadAnimation(context, R.anim.rotate);
        spiral.startAnimation(rotateSpiral);


        MainActivity.addButtonListener(btn1, "learn");
        MainActivity.addButtonListener(btn2, "visualise");
        MainActivity.addButtonListener(btn3, "quiz");

        comicNeue(R.id.TextViewLearn,25);
        comicNeue(R.id.TextViewQuiz,25);
        comicNeue(R.id.TextViewWindow,25);

        germanBeauty(R.id.MainMenuTitle, 80);

        iconifyView(R.id.help_button, 30);
        iconifyView(R.id.about_button,30);

        MainActivity.addButtonListener(frameLayout.findViewById(R.id.help_button), "help");
        MainActivity.addButtonListener(frameLayout.findViewById(R.id.about_button), "about");


        BrainModel.showBrain = true;
        BrainModel.setLabelsToDisplay(false);
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

    public static TextView germanBeauty(int id, int size)
    {
        TextView textView = (TextView) frameLayout.findViewById(id);
        if (germanBeauty == null)
            germanBeauty = Typeface.createFromAsset(Tutorial.context.getAssets(), "fonts/German-Beauty.ttf");

        textView.setTypeface(germanBeauty);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        return textView;
    }

    public static TextView comicNeue(int id, int size)
    {
        TextView textView = (TextView) frameLayout.findViewById(id);
        if (comicNeue == null)
            comicNeue = Typeface.createFromAsset(Tutorial.context.getAssets(), "fonts/ComicNeue-Angular-Bold.ttf");

        textView.setTypeface(comicNeue);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        return textView;
    }
}
