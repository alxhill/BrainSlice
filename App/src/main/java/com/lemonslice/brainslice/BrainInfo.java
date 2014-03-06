package com.lemonslice.brainslice;

import com.threed.jpct.SimpleVector;

import java.util.HashMap;

/**
 * Created by James on 29/01/14.
 */
public class BrainInfo
{
    private static HashMap<String, BrainSegment> segments = new HashMap<String, BrainSegment>();

    public static HashMap<String, BrainSegment> getSegments() {
        return segments;
    }

    static
    {
        BrainSegment cerebellum = new BrainSegment(
            "Cerebellum",
            "The cerebellum (Latin for little brain) is a region of the brain that plays an important role in motor control. It may also be involved in some cognitive functions such as attention and language, and in regulating fear and pleasure responses, but its movement-related functions are the most solidly established.",
            SimpleVector.create(0, 100.0f, 50.0f));

        BrainSegment cerebrum = new BrainSegment(
            "Cerebrum",
            "The cerebrum consists of the cerebral cortex and subcortical structures. It controls all voluntary actions in the body",
            null);

        BrainSegment cerebralCortex = new BrainSegment(
            "Cerebral cortex",
            "The cerebral cortex is the outermost layer of tissue, covering the cerebrum. It takes part in memory, attention, and thought.",
            null);

        BrainSegment brainStem = new BrainSegment(
            "Brainstem",
            "The brainstem is the back part of the brain, connected to the spinal cord. It keeps you breathing, sleeping, and eating.",
            null);

        BrainSegment hippocampus = new BrainSegment(
            "Hippocampus",
            "The hippocampus lies under the cerebral cortex, and is important in making short-term memories long-term. It looks a bit like a seahorse.",
            null);

        BrainSegment amygdala = new BrainSegment(
            "Amygdala",
            "The amygdalae (the plural of amygdala) are almond-shaped groups of nuclei that help you process your memories and emotions.",
            null);

        BrainSegment medullaOblongata = new BrainSegment(
            "Medulla oblongata",
            "The medulla oblongata is the lower half of the brainstem, which helps regulate your bodily functions, like breathing and blood flow.",
            null);

        BrainSegment hypothalamus = new BrainSegment(
            "Hypothalamus",
            "The hypothalamus is a part of the brain that links your nervous system to your kidneys and other organs, as well as telling you when you're hungry or thirsty",
            null);

        BrainSegment frontalLobe = new BrainSegment(
            "Frontal lobe",
            "The frontal lobe is the front part of your brain, which helps you choose between good and bad actions, and understand what's going to happen next.",
            SimpleVector.create(0, -70.0f, 0));

        BrainSegment parietalLobe = new BrainSegment(
            "Parietal lobe",
            "The parietal lobe is the top part of your brain, which helps you understand your senses, like touch.",
            SimpleVector.create(0, 80.0f, -50.0f));

        BrainSegment occipitalLobe = new BrainSegment(
            "Occipital lobe",
            "The occipital lobe is the back part of your brain, which mostly decodes what you see and helps you understand what you see.",
            SimpleVector.create(0, 100.0f, 0));

        BrainSegment temporalLobe = new BrainSegment(
            "Temporal lobe",
            "The temporal lobe is the bottom part of your brain, which lets you keep visual memories, understand languages, and stores new memories and emotion.",
            SimpleVector.create(75.0f, 0, 0));


        segments.put(cerebellum.getTitle(), cerebellum);
        segments.put(cerebrum.getTitle(), cerebrum);
        segments.put(cerebralCortex.getTitle(), cerebralCortex);
        segments.put(brainStem.getTitle(), brainStem);
        segments.put(hippocampus.getTitle(), hippocampus);
        segments.put(amygdala.getTitle(), amygdala);
        segments.put(medullaOblongata.getTitle(), medullaOblongata);
        segments.put(hypothalamus.getTitle(), hypothalamus);
        segments.put(frontalLobe.getTitle(), frontalLobe);
        segments.put(parietalLobe.getTitle(), parietalLobe);
        segments.put(occipitalLobe.getTitle(), occipitalLobe);
        segments.put(temporalLobe.getTitle(), temporalLobe);
    }

    static BrainSegment getSegment(String segment) {
        return segments.get(segment);
    }
}
