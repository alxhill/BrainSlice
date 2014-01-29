package com.lemonslice.brainslice;

import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * Created by James on 29/01/14.
 */
public class BrainInfo
{
    private HashMap<String, BrainSegment> segments = new HashMap<String, BrainSegment>();

    public BrainInfo()
    {
        BrainSegment Cerebellum = new BrainSegment(
            "Cerebellum",
            "The cerebellum (latin for little brain)..."
        );
        BrainSegment ANOTHERSEGMENT = new BrainSegment(
                "ANOTHERSEGMENT",
                "ANOTHER BRAIN SEGMENT"
        );

        

        segments.put(Cerebellum.getTitle(), Cerebellum);
        segments.put(ANOTHERSEGMENT.getTitle(), ANOTHERSEGMENT);
    }

    BrainSegment getSegement(String segment) {
        return segments.get(segment);
    }
}
