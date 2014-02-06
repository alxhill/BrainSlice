package com.lemonslice.brainslice;

import android.util.Log;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.GestureDetector.OnGestureListener;

/**
 * Created by alexander on 28/01/2014.
 */
public class LearnController extends AbstractController implements OnScaleGestureListener, OnGestureListener {

    //How much to rotate the brain
    private float dragX;
    private float dragY;
    private float velocityX = 0;
    private float velocityY = 0;

    private boolean scaleEnd = false;

    //These multipliers affect how fast the brain moves.
    static final double decelCutoff = 0.01;
    static final double decelRate = 0.95;
    static final float velocityDiv = 10000;
    static final float moveDiv = 100;

    //If initial velocity is below this threshold ignore it
    static final double velocityThreshold = 0.1;

    // scale size of brain
    private float scale = 1.0f;
    private static float minScale = 0.1f;
    private static float maxScale = 1.6f;
    private float totalScale = 1.0f;

    private boolean isLoaded;

    // Sensor stuff
    private ScaleGestureDetector scaleDetector = null;
    private GestureDetector gestureDetector = null;


    public LearnController(Context applicationContext)
    {
        scaleDetector = new ScaleGestureDetector(applicationContext, this);
        gestureDetector = new GestureDetector(applicationContext, this);
    }

    //Returns the multiple needed to return the brain to it's original size
    public float resetScale(){
        return totalScale;
    }

    @Override
    public void loadView()
    {
        dragX = dragY = 0;
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
        if(velocityX > decelCutoff || velocityX < -decelCutoff || velocityY > decelCutoff || velocityY < -decelCutoff)
            Log.d("Update Scene", velocityX + " " + velocityY + " " + dragX + " " + dragY);

        if(velocityX > decelCutoff || velocityX < -decelCutoff) velocityX *= decelRate;
        else velocityX = 0;

        if(velocityY > decelCutoff || velocityY < -decelCutoff) velocityY *= decelRate;
        else velocityY = 0;

        dragX += velocityX;
        dragY += velocityY;

        BrainModel.rotate(dragX, dragY, 0.0f);
        dragX = 0;
        dragY = 0;

        BrainModel.scale(scale);
        scale = 1.0f;
    }


    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float distanceY, float distanceX)
    {
        dragX = distanceX / moveDiv;
        dragY = distanceY / moveDiv;
        Log.d("Touch Input", "onScroll: " + dragY + " " + dragX + " " + distanceX + " " + distanceY);
        return true;
    }


    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float vY, float vX)
    {
        if(scaleEnd)
        {
            scaleEnd = false;
            return true;
        }
        velocityX = -vX / velocityDiv;
        velocityY = -vY / velocityDiv;

        Log.d("Touch Input", "onFling before: " + velocityX + " " + velocityY);

        if(velocityX < velocityThreshold && velocityX > -velocityThreshold) velocityX = 0;
        if(velocityY < velocityThreshold && velocityY > -velocityThreshold) velocityY = 0;

        Log.d("Touch Input", "onFling after: " + velocityX + " " + velocityY);
        return true;
    }


    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        //Put things in here for single tap (labels and the like)
        velocityX = 0;
        velocityY = 0;
        return true;
    }


    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public void onShowPress(MotionEvent motionEvent)
    {
        velocityX = 0;
        velocityY = 0;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent)
    {
        velocityX = 0;
        velocityY = 0;
        return true;
    }

    public boolean onScale(ScaleGestureDetector detector)
    {
        if (!isLoaded) return false;
        float difference = detector.getCurrentSpan() - detector.getPreviousSpan();
        scale += 0.001f * difference;

        if(scale * totalScale > maxScale ||  scale * totalScale < minScale)
            scale = 1.0f;
        else
            totalScale *= scale;

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
        scaleEnd = true;
    }

    @Override
    public boolean touchEvent(MotionEvent me)
    {
        scaleDetector.onTouchEvent(me);
        if (scaleDetector.isInProgress())
            return true;

        gestureDetector.onTouchEvent(me);

        return true;
    }

}
