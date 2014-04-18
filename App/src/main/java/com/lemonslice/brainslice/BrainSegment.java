package com.lemonslice.brainslice;

import android.util.Log;

import com.threed.jpct.SimpleVector;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by James on 29/01/14.
 */

public class BrainSegment {
    private String name;

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    private String title;
    private String description;
    private SimpleVector position;
    private HashMap<String, Object> metadata = new HashMap<String, Object>();

    public BrainSegment(String name)
    {
        this.name = name;
    }

    public BrainSegment(String title, String description, SimpleVector position)
    {
        this.title = title;
        this.description = description;
        this.position = position;
        Log.d("BrainSlice", "Created "+title);
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public HashMap<String, Object> getMetadata()
    {
        return metadata;
    }

    public Object getMetaData(String key){ return metadata.get(key); }

    public void setMetadata(String key, Object obj) { this.metadata.put(key, obj);}

    public SimpleVector getPosition()
    {
        return position;
    }

    public void setPosition(SimpleVector position)
    {
        this.position = position;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
