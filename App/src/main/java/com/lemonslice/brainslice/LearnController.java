package com.lemonslice.brainslice;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import com.threed.jpct.Logger;

/**
 * Created by alexander on 28/01/2014.
 */
public class LearnController extends AbstractController implements OnScaleGestureListener, OnGestureListener {

    private float touchX;
    private float touchY;

    private float xpos1 = -1;

    private float ypos1 = -1;
    private int firstPointerID = -1;

    // scale size of brain
    private float scale = 1.0f;

    private boolean isLoaded;

    // Sensor stuff
    private ScaleGestureDetector scaleDetector = null;
    private GestureDetector gestureDetector = null;


    public LearnController(Context applicationContext)
    {
        scaleDetector = new ScaleGestureDetector(applicationContext, this);
        gestureDetector = new GestureDetector(applicationContext, this);
    }

    @Override
    public void loadView()
    {
        touchX = touchY = 0;
        isLoaded = true;
    }

    @Override
    public void unloadView()
    {
        isLoaded = false;
    }

    @Override
    public void updateScene()
    {
        BrainModel.rotate(touchX, touchY, 0.0f);
        touchX = 0;
        touchY = 0;

        BrainModel.scale(scale);
        scale = 1.0f;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        //Put things in here for single tap (labels and the like)
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {

        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean touchEvent(MotionEvent me)
    {
        scaleDetector.onTouchEvent(me);
        if (scaleDetector.isInProgress())
            return true;

        int pointerIndex;

        switch (me.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                Logger.log("ACTION_DOWN\t");
                xpos1 = me.getX();
                ypos1 = me.getY();
                firstPointerID = me.getPointerId(0);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Logger.log("ACTION_POINTER_DOWN\t");
                touchY = 0;
                touchX = 0;
                break;

            case MotionEvent.ACTION_UP:
                Logger.log("ACTION_UP\t");
                xpos1 = -1;
                ypos1 = -1;
                touchY = 0;
                touchX = 0;
                firstPointerID = -1;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                //Get index of pointer that was lifted up
                pointerIndex = me.getActionIndex();

                Logger.log("ACTION_POINTER_UP\t");
                if (me.getPointerId(pointerIndex) == firstPointerID)
                {
                    //Choose new firstPointer
                    int newPointerIndex;
                    if (pointerIndex == 0)
                    {
                        newPointerIndex = 1;
                    } else
                    {
                        newPointerIndex = 0;
                    }
                    xpos1 = me.getX(newPointerIndex);
                    ypos1 = me.getY(newPointerIndex);
                    firstPointerID = me.getPointerId(newPointerIndex);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (me.getPointerCount() == 1)
                {
                    pointerIndex = me.findPointerIndex(firstPointerID);
                    Logger.log("ACTION_MOVE " + pointerIndex);
                    float xd = me.getX(pointerIndex) - xpos1;
                    float yd = me.getY(pointerIndex) - ypos1;
                    xpos1 = me.getX(pointerIndex);
                    ypos1 = me.getY(pointerIndex);
                    touchY = xd / -200f;
                    touchX = yd / -200f;
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                firstPointerID = -1;
                break;
        }

        return true;
    }

    public boolean onScale(ScaleGestureDetector detector)
    {
        if (!isLoaded) return false;
        float difference = detector.getCurrentSpan() - detector.getPreviousSpan();
        scale += 0.001f * difference;
        return true;
    }

    // must be implemented for onscale
    public boolean onScaleBegin(ScaleGestureDetector detector)
    {
        return true;
    }

    // must be implemented for onscale
    public void onScaleEnd(ScaleGestureDetector detector)
    {

    }

}
