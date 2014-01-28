package com.lemonslice.brainslice;

import android.view.MotionEvent;

/**
 * Created by alexander on 28/01/2014.
 */
public abstract class AbstractController {

    public abstract void loadView();

    public abstract void unloadView();

    public abstract void updateScene();

    public abstract boolean touchEvent(MotionEvent me);
}
