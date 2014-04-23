package com.lemonslice.brainslice.event;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.lemonslice.brainslice.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alexander on 09/03/2014.
 */
public class Events {

    private static ArrayList<EventListener> eventListeners = new ArrayList<EventListener>();

    public static void register(EventListener listener)
    {
        eventListeners.add(listener);
    }

    public static void trigger(String name, Object ...args)
    {
        for (EventListener listener : eventListeners)
            listener.receiveEvent(name, args);
    }

}
