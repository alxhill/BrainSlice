package com.lemonslice.brainslice.event;

import android.content.Context;
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

/**
 * Created by James on 26/03/14.
 */
public class HelpCards {

    static Context context;
    static FrameLayout frameLayout;
    static LayoutInflater inflater;

    public static void setContext(Context c)
    {
        context = c;
    }
    public static void setFrameLayout(FrameLayout l) {frameLayout = l; }

    static ViewPager mViewPager;
    static CollectionPagerAdapter mCollectionPagerAdapter;

    public static void show() {
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout cardsScreen = (LinearLayout)inflater.inflate(R.layout.cardholder, null);


        FragmentActivity main = (FragmentActivity)context;
        mCollectionPagerAdapter =
                new CollectionPagerAdapter(
                       main.getSupportFragmentManager());
        assert cardsScreen != null;
        mViewPager = (ViewPager)cardsScreen.findViewById(R.id.pager);
        mViewPager.setPageMargin(-800);
        mViewPager.setHorizontalFadingEdgeEnabled(true);
        mViewPager.setFadingEdgeLength(30);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mCollectionPagerAdapter);

        frameLayout.removeAllViews();
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
        // Our object is just an integer :-P
        args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 100;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }
}

// Instances of this class are fragments representing a single
// object in our collection.
class DemoObjectFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.card, container, false);
        Bundle args = getArguments();

        Log.d("newui", Integer.toString(args.getInt(ARG_OBJECT)));

        return rootView;
    }
}
