package com.lemonslice.brainslice;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import com.threed.jpct.SimpleVector;

import java.io.IOException;
import java.io.InputStream;


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

        String filename = "audio/" + name.toLowerCase() + ".wav";
        Log.d("BRAINSEGMENT", "audio filename: " + filename);
        try
        {
            MediaPlayer player = new MediaPlayer();
            BrainInfo.speaker.release();
            BrainInfo.speaker = player;

            AssetFileDescriptor afd = ctx.getAssets().openFd(filename);

            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            player.prepare();
            Log.d("BRAINSEGMENT", "playing audio");
            player.start();

        } catch (IOException e)
        {
            e.printStackTrace();
            Log.d("BRAINSEGMENT", "audio file not found");
        }

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
