package com.lemonslice.brainslice;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by James on 29/01/14.
 */
public class BrainSegment {
    private String title;
    private String description;
    private HashMap<String, Object> metadata;

    public BrainSegment(HashMap<String, Object> metadata, String description, String title)
    {
        this.description = description;
        this.title = title;
        this.metadata = metadata;
    }

    public BrainSegment(String description, String title)
    {
        this.description = description;
        this.title = title;
        this.metadata = null;
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
