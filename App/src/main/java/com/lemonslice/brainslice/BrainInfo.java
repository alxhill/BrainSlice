package com.lemonslice.brainslice;

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
            "The cerebellum is the part of the brain that controls your movement, as well as playing a part in paying attention and recognising fear and pleasure."
        );
        BrainSegment Cerebrum = new BrainSegment(
                "Cerebrum",
                "The cerebrum consists of the cerebral cortex and subcortical structures. It ontrols all voluntary actions in the body"
        );

        BrainSegment CerebralCortex = new BrainSegment(
                "Cerebral cortex",
                "The cerebral cortex is the outermost layer of tissue, covering the cerebrum. It takes part in memory, attention, and thought."
        );

        BrainSegment Brainstem = new BrainSegment(
                "Brainstem",
                "The brainstem is the back part of the brain, connected to the spinal cord. It keeps you breathing, sleeping, and eating."
        );

        BrainSegment Hippocampus = new BrainSegment(
                "Hippocampus",
                "The hippocampus lies under the cerebral cortex, and is important in making short-term memories long-term. It looks a bit like a seahorse.
        );

        BrainSegment Amygdala = new BrainSegment(
                "Amygdala",
                "The amygdalae (the plural of amygdala) are almond-shaped groups of nuclei that help you process your memories and emotions."
        );

        BrainSegment MedullaOblongata = new BrainSegment(
                "Medulla oblongata",
                "The medulla oblongata is the lower half of the brainstem, which helps regulate your bodily functions, like breathing and blood flow."
        );

        BrainSegment Hypothalamus = new BrainSegment(
                "Hypothalamus",
                "The hypothalamus is a part of the brain that links your nervous system to your kidneys and other organs, as well as telling you when you're hungry or thirsty"
        );

        BrainSegment FrontalLobe = new BrainSegment(
                "Frontal lobe",
                "The frontal lobe is the front part of your brain, which helps you choose between good and bad actions, and understand what's going to happen next."
        );

        BrainSegment ParietalLobe = new BrainSegment(
                "Parietal lobe",
                "The parietal lobe is the top part of your brain, which helps you understand your senses, like touch."
        );

        BrainSegment OccipitalLobe = new BrainSegment(
                "Occipital lobe",
                "The occipital lobe is the back part of your brain, which mostly decodes what you see and helps you understand what you see."
        );

        BrainSegment TemporalLobe = new BrainSegment(
                "Temporal lobe",
                "The temporal lobe is the bottom part of your brain, which lets you keep visual memories, understand languages, and stores new memories and emotion."
        );

        

        segments.put(Cerebellum.getTitle(), Cerebellum);
        segments.put(Cerebrum.getTitle(), Cerebrum);
        segments.put(CerebralCortex.getTitle(), CerebralCortex);
        segments.put(Brainstem.getTitle(), BrainStem);
        segments.put(Hippocampus.getTitle(), Hippocampus);
        segments.put(Amygdala.getTitle(), Amygdala);
        segments.put(MedullaOblongata.getTitle(), MedullaOblongata);
        segments.put(Hypothalamus.getTitle(), Hypothalamus);
        segments.put(FrontalLobe.getTitle(), FrontalLobe);
        segments.put(ParietalLobe.getTitle(), ParietalLobe);
        segments.put(OccipitalLobe.getTitle(), OccipitalLobe);
        segments.put(TemporalLobe.getTitle(), TemporalLobe);
    }

    BrainSegment getSegement(String segment) {
        return segments.get(segment);
    }
}
