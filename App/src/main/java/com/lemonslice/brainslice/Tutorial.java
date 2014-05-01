package com.lemonslice.brainslice;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by James on 26/03/14.
 */
public class Tutorial {

    public static final int NUMBER_OF_CARDS = 7;

    static Context context;
    static FrameLayout frameLayout;
    static LayoutInflater inflater;

    static ArrayList<LinearLayout> circles;

    public static void setContext(Context c)
    {
        context = c;
    }
    public static void setFrameLayout(FrameLayout l) {frameLayout = l; }

    static ViewPager mViewPager;
    static CollectionPagerAdapter mCollectionPagerAdapter;

    public static boolean showMainMenu;
    private static Typeface comicNeue;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void show(int startPage, final boolean showMainMenu) {
        Tutorial.showMainMenu = showMainMenu;

        frameLayout.removeAllViews();
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        FrameLayout cardsScreen = (FrameLayout)inflater.inflate(R.layout.card_holder, null);
        FrameLayout holder = (FrameLayout)cardsScreen.findViewById(R.id.holder);
        LinearLayout circleHolder = (LinearLayout)holder.findViewById(R.id.page_indicator_holder);

        circles = new ArrayList<LinearLayout>(NUMBER_OF_CARDS);
        for(int i=0; i< NUMBER_OF_CARDS; i++)
        {
            circles.add((LinearLayout)circleHolder.getChildAt(i));
        }
        circles.get(startPage).setBackground(context.getResources().getDrawable(R.drawable.circle_gold));
        circles.get(0).setVisibility(View.INVISIBLE);
        circles.get(NUMBER_OF_CARDS-1).setVisibility(View.INVISIBLE);
        if (!showMainMenu)
            circles.get(NUMBER_OF_CARDS-2).setVisibility(View.INVISIBLE);

        FragmentActivity main = (FragmentActivity)context;
        mCollectionPagerAdapter =
                new CollectionPagerAdapter(
                        main.getSupportFragmentManager());

        mViewPager = (ViewPager) cardsScreen.findViewById(R.id.pager);
        mViewPager.setHorizontalFadingEdgeEnabled(true);
        mViewPager.setFadingEdgeLength(30);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mCollectionPagerAdapter);
        mViewPager.setPageTransformer(false,new ZoomOutPageTransformer());

        mViewPager.setCurrentItem(startPage, true);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                /*if (((showMainMenu) && (position+1==NUMBER_OF_CARDS))
                        || ((!showMainMenu) && (position+2==NUMBER_OF_CARDS))
                        || (position+1==1)
                        )
                    hide();*/
            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0 || position == NUMBER_OF_CARDS - 1 || (position == NUMBER_OF_CARDS - 2 && !showMainMenu))
                    hide();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_SETTLING)
                {
                    for (int i = 0; i < circles.size(); i++) {
                        LinearLayout c = circles.get(i);
                        if (mViewPager.getCurrentItem() == i)
                            c.setBackground(context.getResources().getDrawable(R.drawable.circle_gold));
                        else
                            c.setBackground(context.getResources().getDrawable(R.drawable.circle_white));
                    }

                }
            }
        });

        cardsScreen.removeView(holder);
        cardsScreen.addView(holder);

        //fade in
        holder.setVisibility(View.INVISIBLE);

        frameLayout.addView(cardsScreen);
        Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        fadeIn.setFillAfter(true);
        holder.startAnimation(fadeIn);
    }

    public static void hide()
    {
        Animation fadeOut = AnimationUtils.loadAnimation(context,R.anim.abc_fade_out);
        fadeOut.setDuration(1000);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mViewPager.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return false;
                    }
                });
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
        HomeScreen.buttonCatcher = false;
    }

    public static TextView comicNeue(TextView textView, int size)
    {
        if (comicNeue == null)
            comicNeue = Typeface.createFromAsset(Tutorial.context.getAssets(), "fonts/ComicNeue-Angular-Regular.ttf");

        textView.setTypeface(comicNeue);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        return textView;
    }
}

class CollectionPagerAdapter extends FragmentStatePagerAdapter {

    public CollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new NewObjectFragment();
        Bundle args = new Bundle();
        args.putInt(NewObjectFragment.ARG_OBJECT, i + 1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return Tutorial.NUMBER_OF_CARDS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }
}

class NewObjectFragment extends Fragment {
    static int resID;
    public static final String ARG_OBJECT = "object";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;
        Bundle args = getArguments();
        int card = args.getInt(ARG_OBJECT);
        if ((!Tutorial.showMainMenu)&&(card>=2))
            card++;
        switch(card)
        {
            case 1: rootView = inflater.inflate(R.layout.card_left, container, false); break;
            case 2: rootView = inflater.inflate(R.layout.card_first, container, false);
                Tutorial.comicNeue((TextView) rootView.findViewById(R.id.textView2), 30);
                Tutorial.comicNeue((TextView) rootView.findViewById(R.id.textView3),30);
                break;
            case 3: rootView = inflater.inflate(R.layout.card2, container, false); break;
            case 4: rootView = inflater.inflate(R.layout.card3, container, false); break;
            case 5: rootView = inflater.inflate(R.layout.card4, container, false); break;
            case 6: rootView = inflater.inflate(R.layout.card5, container, false); break;
            case 7: rootView = inflater.inflate(R.layout.card_final, container, false); break;
            default: break;
        }

        return rootView;
    }
}
