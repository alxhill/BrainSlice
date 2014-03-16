package com.lemonslice.brainslice.event;

/**
 * Created by alexander on 10/03/2014.
 */
public interface EventListener {
    public void receiveEvent(String name, Object ...data);
}
