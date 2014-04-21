package com.lemonslice.brainslice;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.threed.jpct.SimpleVector;

import java.util.HashMap;


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

    public BrainSegment(String name)
    {
        this.name = name;
    }

    public void playAudio(Context ctx)
    {
        if (BrainInfo.speaker.isPlaying()) return;

        String filename = "raw/audio/" + name.toLowerCase() + ".wav";
        Log.d("FILENAME", "audio file name: " + filename);
        int audioId = ctx.getResources().getIdentifier(filename, null, ctx.getPackageName());

        if (audioId == 0) return;

        BrainInfo.speaker = MediaPlayer.create(ctx, audioId);
        BrainInfo.speaker.start();
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

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
