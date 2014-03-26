package com.lemonslice.brainslice.event;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lemonslice.brainslice.MainActivity;
import com.lemonslice.brainslice.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by James on 26/03/14.
 */
public class HelpCards {

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


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void show() {
        int sdk = android.os.Build.VERSION.SDK_INT;

        frameLayout.removeAllViews();
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout cardsScreen = (FrameLayout)inflater.inflate(R.layout.cardholder, null);

        LinearLayout circleHolder = (LinearLayout)cardsScreen.findViewById(R.id.page_indicator_holder);
        circles = new ArrayList<LinearLayout>(5);
        for(int i=0; i<5; i++)
        {
            circles.add((LinearLayout)circleHolder.getChildAt(i));
        }
        circles.get(0).setBackground(context.getResources().getDrawable(R.drawable.circle_gold));

        FragmentActivity main = (FragmentActivity)context;
        mCollectionPagerAdapter =
                new CollectionPagerAdapter(
                       main.getSupportFragmentManager());

        mViewPager = (ViewPager)cardsScreen.findViewById(R.id.pager);
        mViewPager.setPageMargin(-800);
        mViewPager.setHorizontalFadingEdgeEnabled(true);
        mViewPager.setFadingEdgeLength(30);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mCollectionPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

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

        cardsScreen.removeView(circleHolder);
        cardsScreen.addView(circleHolder);
        frameLayout.addView(cardsScreen);
    }

}

class CollectionPagerAdapter extends FragmentPagerAdapter {

    public CollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new DemoObjectFragment();
        Bundle args = new Bundle();
        args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }
}

class DemoObjectFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.card, container, false);
        Bundle args = getArguments();

        Log.d("newui", Integer.toString(args.getInt(ARG_OBJECT)));

        return rootView;
    }
}
