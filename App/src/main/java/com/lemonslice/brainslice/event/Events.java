package com.lemonslice.brainslice.event;

import java.util.ArrayList;

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
