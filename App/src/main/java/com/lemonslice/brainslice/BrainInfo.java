package com.lemonslice.brainslice;

import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by James on 29/01/14.
 */
public class BrainInfo
{
    private static HashMap<String, BrainSegment> segments = new HashMap<String, BrainSegment>();

    static
    {
        BrainSegment Cerebellum = new BrainSegment(
            "Cerebellum",
            "This part of your brain helps you stay balanced and helps you move"
        );
        BrainSegment Cerebrum = new BrainSegment(
                "Cerebrum",
                "The cerebrum consists of the cerebral cortex and subcortical structures. It controls all voluntary actions in the body"
        );

        BrainSegment CerebralCortex = new BrainSegment(
                "Cerebral cortex",
                "The cerebral cortex is the outermost layer of tissue, covering the cerebrum. It takes part in memory, attention, and thought."
        );

        BrainSegment BrainStem = new BrainSegment(
                "Brainstem",
                "The brainstem connects the brain to the rest of the body. It also keeps you breathing, sleeping, and eating."
        );

        BrainSegment Hippocampus = new BrainSegment(
                "Hippocampus",
                "The hippocampus is important in making short-term memories long-term. It looks a bit like a seahorse."
        );

        BrainSegment Amygdala = new BrainSegment(
                "Amygdala",
                "The amygdalae are almond-shaped and help you process your memories and emotions."
        );

        /*BrainSegment MedullaOblongata = new BrainSegment(
                "Medulla oblongata",
                "The medulla oblongata is the lower half of the brainstem, which helps regulate your bodily functions, like breathing and blood flow."
        );*/

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
                "The occipital lobe is connected directly to your eyes and turns the picture from your eyes the right way up."
        );

        BrainSegment TemporalLobe = new BrainSegment(
                "Temporal lobe",
                "The temporal lobe lets you keep visual memories, understand languages, and stores new memories and emotion."
        );


        segments.put(Cerebellum.getTitle(), Cerebellum);
        segments.put(Cerebrum.getTitle(), Cerebrum);
        segments.put(CerebralCortex.getTitle(), CerebralCortex);
        segments.put(BrainStem.getTitle(), BrainStem);
        segments.put(Hippocampus.getTitle(), Hippocampus);
        segments.put(Amygdala.getTitle(), Amygdala);
        //segments.put(MedullaOblongata.getTitle(), MedullaOblongata);
        segments.put(Hypothalamus.getTitle(), Hypothalamus);
        segments.put(FrontalLobe.getTitle(), FrontalLobe);
        segments.put(ParietalLobe.getTitle(), ParietalLobe);
        segments.put(OccipitalLobe.getTitle(), OccipitalLobe);
        segments.put(TemporalLobe.getTitle(), TemporalLobe);
    }


    static BrainSegment getSegment(String segment) {
        return segments.get(segment);
    }

    HashMap<String, BrainSegment> getSegments() {
        return segments;
    }
}
