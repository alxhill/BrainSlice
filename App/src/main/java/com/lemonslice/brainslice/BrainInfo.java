package com.lemonslice.brainslice;

import com.threed.jpct.SimpleVector;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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
        String[][] cerebellumQuestions =  {{"What part of the brain is this?", "Cerebellum", "Parietal Lobe", "Brainstem", "Ear lobe"},
                                           {"Where is this part of the brain?", "Top", "Bottom", "Inside", "Front"}};
        Integer[] cerebellumAnswers = {1, 2};

        String[][] brainstemQuestions =  {{"What part of the brain is this?", "Cerebellum", "Parietal Lobe", "Frontal Lobe", "Brainstem"},
                                          {"What does this connect to?", "Spine", "Eyes", "Ears", "Hair"}};
        Integer[] brainstemAnswers = {4, 1};

        String[][] hippocampusQuestions =  {{"What part of the brain is this?", "Cerebellum", "Hippocampus", "Amygdala", "Ear Lobe"},
                                            {"Where is this part of the brain?", "Top", "Bottom", "Inside", "Front"}};
        Integer[] hippocampusAnswers = {2, 3};

        String[][] amygdalaQuestions =  {{"What is this part of the brain shaped like?", "Walnut", "Hazelnut", "Almond", "Pear"},
                                         {"Where is this part of the brain?", "Inside", "Bottom", "Back", "Front"}};
        Integer[] amygdalaAnswers = {1, 1};

        String[][] occipitalQuestions =  {{"What part of the brain is this?", "Occipital Lobe", "Parietal Lobe", "Brainstem", "Ear lobe"},
                                          {"What does this part of the brain connect to?", "Toenails", "Ears", "Spine", "Eyes"}};
        Integer[] occipitalAnswers = {1, 3};

        String[][] hypothalamusQuestions =  {{"What part of the brain is this?", "Cerebellum", "Parietal Lobe", "Brainstem", "Hypothalamus"},
                                             {"What does the hypothalamus help you do?", "See things", "Remember things", "Get hungry", "Stay warm"}};
        Integer[] hypothalamusAnswers = {4, 2};

        String[][] frontalQuestions =  {{"What part of the brain is this?", "Frontal Lobe", "Occipital Lobe", "Amygdala", "Brainstem"},
                                        {"Where is this part of the brain?", "Top", "Bottom", "Inside", "Front"}};
        Integer[] frontalAnswers = {1, 4};

        String[][] temporalQuestions =  {{"What part of the brain is this?", "Cerebellum", "Parietal Lobe", "Brainstem", "Temporal Lobe"},
                                         {"Where is this part of the brain?", "Bottom", "Side", "Inside", "Front"}};
        Integer[] temporalAnswers = {4, 2};

        BrainSegment cerebellum = new BrainSegment(
            "Cerebellum",
            "This part of your brain helps you stay balanced and helps you move.",
            SimpleVector.create(0, 100.0f, -50.0f),
            new ArrayList<String[]>(Arrays.asList(cerebellumQuestions)),
            new ArrayList<Integer>(Arrays.asList(cerebellumAnswers)));

        BrainSegment cerebrum = new BrainSegment(
            "Cerebrum",
            "The cerebrum consists of the cerebral cortex and subcortical structures. It controls all voluntary actions in the body",
            null, null, null);

        BrainSegment cerebralCortex = new BrainSegment(
            "Cerebral cortex",
            "The cerebral cortex is the outermost layer of tissue, covering the cerebrum. It takes part in memory, attention, and thought.",
            null, null, null);

        BrainSegment brainStem = new BrainSegment(
            "Brainstem",
            "The brainstem is the back part of the brain, connected to the spinal cord. It keeps you breathing, sleeping, and eating.",
            SimpleVector.create(0, 15.0f, -40.0f),
            new ArrayList<String[]>(Arrays.asList(brainstemQuestions)),
            new ArrayList<Integer>(Arrays.asList(brainstemAnswers)));

        BrainSegment hippocampus = new BrainSegment(
            "Hippocampus",
            "The hippocampus is important in making short-term memories long-term. It looks a bit like a seahorse.",
            null,
            new ArrayList<String[]>(Arrays.asList(hippocampusQuestions)),
            new ArrayList<Integer>(Arrays.asList(hippocampusAnswers)));

        BrainSegment amygdala = new BrainSegment(
            "Amygdala",
            "The amygdalae are almond-shaped and help you process your memories and emotions.",
            SimpleVector.create(30.0f, 15.0f, -40.0f),
            new ArrayList<String[]>(Arrays.asList(amygdalaQuestions)),
            new ArrayList<Integer>(Arrays.asList(amygdalaAnswers)));

        BrainSegment medullaOblongata = new BrainSegment(
            "Medulla oblongata",
            "The medulla oblongata is the lower half of the brainstem, which helps regulate your bodily functions, like breathing and blood flow.",
            null, null, null);

        BrainSegment hypothalamus = new BrainSegment(
            "Hypothalamus",
            "The hypothalamus is a part of the brain that links your nervous system to your kidneys and other organs, as well as telling you when you're hungry or thirsty",
            null,
            new ArrayList<String[]>(Arrays.asList(hypothalamusQuestions)),
            new ArrayList<Integer>(Arrays.asList(hypothalamusAnswers)));

        BrainSegment frontalLobe = new BrainSegment(
            "Frontal lobe",
            "The frontal lobe is the front part of your brain, which helps you choose between good and bad actions, and understand what's going to happen next.",
            SimpleVector.create(0, -70.0f, 0),
            new ArrayList<String[]>(Arrays.asList(frontalQuestions)),
            new ArrayList<Integer>(Arrays.asList(frontalAnswers)));

        BrainSegment parietalLobe = new BrainSegment(
            "Parietal lobe",
            "The parietal lobe is the top part of your brain, which helps you understand your senses, like touch.",
            SimpleVector.create(0, 80.0f, 50.0f),
            new ArrayList<String[]>(Arrays.asList(frontalQuestions)),
            new ArrayList<Integer>(Arrays.asList(frontalAnswers)));

        BrainSegment occipitalLobe = new BrainSegment(
            "Occipital lobe",
            "The occipital lobe is connected directly to your eyes, and turns the picture from your eyes the right way up. It also helps you understand what you see.",
            SimpleVector.create(0, 100.0f, 0),
            new ArrayList<String[]>(Arrays.asList(occipitalQuestions)),
            new ArrayList<Integer>(Arrays.asList(occipitalAnswers)));

        BrainSegment temporalLobe = new BrainSegment(
            "Temporal lobe",
            "The temporal lobe lets you keep visual memories, understand languages, and stores new memories and emotion.",
            SimpleVector.create(-75.0f, 0, 0),
            new ArrayList<String[]>(Arrays.asList(temporalQuestions)),
            new ArrayList<Integer>(Arrays.asList(temporalAnswers)));


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
