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
public class LearnController extends AbstractController implements OnScaleGestureListener, OnGestureListener, GestureDetector.OnDoubleTapListener {

    //Variables that we transform the brain with
    private float scale = 1.0f;
    private float dragX = 0;
    private float dragY = 0;
    private float velocityX = 0;
    private float velocityY = 0;

    //Used to prevent flinging at the end of a scale
    private int scaleEnd = 0;

    //Constants that dictate cutoffs and speed multipliers
    private static final double decelCutoff = 0.001;
    private static final double decelRate = 0.97;
    private static final float velocityMult = 0.00005f;
    private static final float velocityThreshold = 0.05f;
    private static final float moveMult = 0.005f;
    private static final float scaleMult = 0.001f;
    // limits for scaling
    private static final float minScale = 0.2f;
    private static final float maxScale = 0.69f;

    private float cumulativeScale = 1.0f;

    private boolean isLoaded;

    // Used to interpret motion events as gestures
    private ScaleGestureDetector scaleDetector = null;
    private GestureDetector gestureDetector = null;


    public LearnController(Context applicationContext)
    {
        //Initialise detectors
        scaleDetector = new ScaleGestureDetector(applicationContext, this);
        gestureDetector = new GestureDetector(applicationContext, this);
        gestureDetector.setOnDoubleTapListener(this);
    }

    @Override
    public void loadView()
    {
        velocityX = 0;
        velocityY = 0;
        isLoaded = true;
    }

    @Override
    public void unloadView()
    {
        velocityX=0;
        velocityY=0;
        isLoaded = false;
    }

    @Override
    public void updateScene()
    {
        double totalVelocity = Math.sqrt(velocityX*velocityX + velocityY*velocityY);
        /*if(velocityX > decelCutoff || velocityX < -decelCutoff || velocityY > decelCutoff || velocityY < -decelCutoff)
            Log.d("Update Scene", velocityX + " " + velocityY + " " + dragX + " " + dragY);*/

        if(scaleEnd > 0){
            scaleEnd--;
        }

        if(totalVelocity > decelCutoff || totalVelocity < -decelCutoff)
        {
            velocityX *= decelRate;
            velocityY *= decelRate;
            Log.e("ffs", "X " + velocityX + " Y " + velocityY);
        } else {
            velocityX = 0;
            velocityY = 0;
        }

        dragX += velocityX;
        dragY += velocityY;

        BrainModel.rotate(dragX, dragY, 0.0f);
        dragX = 0;
        dragY = 0;

        BrainModel.scale(scale);
        scale = 1.0f;
    }

    @Override
    public void stop(){
        velocityX = 0;
        velocityY = 0;
    }


    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float distanceY, float distanceX)
    {
        if(scaleEnd > 0)
        {
            scaleEnd--;
            return true;
        }
        dragX = distanceX * moveMult;
        dragY = distanceY * moveMult;
        Log.d("Touch Input", "onScroll: " + dragY + " " + dragX + " " + distanceX + " " + distanceY);
        return true;
    }


    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float vY, float vX)
    {
        if(scaleEnd > 0)
        {
            scaleEnd = 0;
            return true;
        }

        velocityX = -vX * velocityMult;
        velocityY = -vY * velocityMult;
        if(velocityX < velocityThreshold && velocityX > -velocityThreshold) velocityX = 0;
        if(velocityY < velocityThreshold && velocityY > -velocityThreshold) velocityY = 0;

        return true;
    }


    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        //Put things in here for single tap such as labels popping up
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

    @Override
    public boolean onScale(ScaleGestureDetector detector)
    {
        if (!isLoaded) return false;
        float difference = detector.getCurrentSpan() - detector.getPreviousSpan();
        scale += scaleMult * difference;

        float cumulativeScale = BrainModel.getScale();

        if(scale * cumulativeScale > maxScale ||  scale * cumulativeScale < minScale)
            scale = 1.0f;

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector)
    {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector)
    {
        scaleEnd = 5;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        BrainModel.moveToFront();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        return true;
    }

    @Override
    public boolean touchEvent(MotionEvent me)
    {
        scaleDetector.onTouchEvent(me);
        if (scaleDetector.isInProgress())
            return true;

        if(me.getPointerCount() == 1)
            gestureDetector.onTouchEvent(me);

        return true;
    }

}
