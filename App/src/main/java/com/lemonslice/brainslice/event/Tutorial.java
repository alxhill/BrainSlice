package com.lemonslice.brainslice.event;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.lemonslice.brainslice.R;

import java.util.ArrayList;

/**
 * Created by James on 26/03/14.
 */
public class Tutorial {

    public static final int NUMBER_OF_CARDS = 6;

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
        FrameLayout cardsScreen = (FrameLayout)inflater.inflate(R.layout.card_holder, null);

        LinearLayout circleHolder = (LinearLayout)cardsScreen.findViewById(R.id.page_indicator_holder);
        circles = new ArrayList<LinearLayout>(NUMBER_OF_CARDS);
        for(int i=0; i< NUMBER_OF_CARDS; i++)
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

    public static void hide()
    {

        frameLayout.removeAllViews();
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
        switch(args.getInt(ARG_OBJECT))
        {
            case 1: rootView = inflater.inflate(R.layout.card_first, container, false);
                assert rootView != null;
                (rootView.findViewById(R.id.skip_tutorial)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Tutorial.hide();
                    }
                });
                (rootView.findViewById(R.id.get_started)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Tutorial.mViewPager.setCurrentItem(1, true);
                    }
                });
                break;
            case 2: rootView = inflater.inflate(R.layout.card2, container, false); break;
            case 3: rootView = inflater.inflate(R.layout.card3, container, false); break;
            case 4: rootView = inflater.inflate(R.layout.card4, container, false); break;
            case 5: rootView = inflater.inflate(R.layout.card5, container, false); break;
            case 6: rootView = inflater.inflate(R.layout.card_final, container, false);
                assert rootView != null;
                (rootView.findViewById(R.id.get_started)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Tutorial.hide();
                    }
                });
                break;
        }

        return rootView;
    }
}
