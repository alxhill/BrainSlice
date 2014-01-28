package com.lemonslice.brainslice;

import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

/**
 * Created by alexander on 28/01/2014.
 */
public class LearnController implements OnScaleGestureListener {


    public LearnController()
    {

    }

    public void loadView()
    {

    }

    public void unloadView()
    {

    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }
}
