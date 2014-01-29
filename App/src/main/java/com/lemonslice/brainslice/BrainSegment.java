package com.lemonslice.brainslice;

import android.util.Log;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by James on 29/01/14.
 */

public class BrainSegment {
    private String title;
    private String description;
    private HashMap<String, Object> metadata = new HashMap<String, Object>();

    public BrainSegment(String title, String description, HashMap<String, Object> metadata)
    {
        this.title = title;
        this.description = description;
        this.metadata = metadata;
    }

    public BrainSegment(String title, String description)
    {
        this.title = title;
        this.description = description;
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

    public Object getMetaData(String key)
    {
        return metadata.get(key);
    }

    public void setMetadata(String key, Object obj)
    {
        this.metadata.put(key, obj);
    }
}
