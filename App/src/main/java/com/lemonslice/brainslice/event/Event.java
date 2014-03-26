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
public class Event {

    private static HashMap<String, ArrayList<EventListener>> eventListeners = new HashMap<String, ArrayList<EventListener>>();

    public static void register(String name, EventListener listener)
    {
        if (eventListeners.get(name) == null) eventListeners.put(name, new ArrayList<EventListener>());

        ArrayList<EventListener> listeners = eventListeners.get(name);
        listeners.add(listener);
    }

    public static void trigger(String name, Object ...args)
    {
        ArrayList<EventListener> listeners = eventListeners.get(name);

        if (listeners == null) return;

        for (EventListener listener : listeners)
            listener.receiveEvent(name, args);
    }

}
