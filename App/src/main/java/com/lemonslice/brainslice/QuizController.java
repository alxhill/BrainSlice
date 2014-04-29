package com.lemonslice.brainslice;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.lemonslice.brainslice.event.EventListener;
import com.lemonslice.brainslice.event.Events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by alexander on 4/23/14.
 */
public class QuizController extends AbstractController implements EventListener {

    private TextView topLabel;
    private FrameLayout mainOverlay;
    private FrameLayout labelOverlay;
    private Button quizButton;

    private Context context;
    private ArrayList<BrainSegment> learnedSegments;
    private LayoutInflater inflater;

    private boolean isQuizzing = false;
    private Iterator<BrainSegment> segmentIterator;

    private BrainSegment checkedSegment;

    public QuizController(Context context)
    {
        Events.register(this);
        this.context = context;

        inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void loadView()
    {
        topLabel.setText("Press play to start quiz mode!");

        mainOverlay.removeAllViews();

        final FrameLayout quizView = (FrameLayout) inflater.inflate(R.layout.quiz_screen, null);
        assert quizView != null;

        labelOverlay = (FrameLayout) quizView.findViewById(R.id.quiz_overlay);
        Labels.setFrameLayout(labelOverlay);

        mainOverlay.addView(quizView);

        quizButton = (Button) quizView.findViewById(R.id.play_button);
        quizButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                quizButton.setVisibility(Button.INVISIBLE);
                startQuiz();
            }
        });
    }

    @Override
    public void unloadView()
    {
        topLabel.setText("");
        mainOverlay.removeAllViews();
        Labels.setFrameLayout(mainOverlay);
        learnedSegments = new ArrayList<BrainSegment>();
    }

    private void startQuiz()
    {
        isQuizzing = true;
        segmentIterator = BrainInfo.getSegments().values().iterator();
        learnedSegments = new ArrayList<BrainSegment>();
        topLabel.setText("");

        BrainModel.smoothMoveToGeneric(BrainModel.sidePosition, 0, 400);
        BrainModel.smoothZoom(0.25f, 400);
        BrainModel.setLabelsToDisplay(true);

        learnNewSegment(false);
    }

    // shows the user a segment of the brain they have not yet seen
    private BrainSegment learnNewSegment(boolean lastSegment)
    {
        BrainSegment newSegment;

        while (segmentIterator.hasNext())
        {
            newSegment = segmentIterator.next();
            if (newSegment.getPosition() == null) continue;
            Log.d("BRAINQUIZ", "new segment is " + newSegment.getName());

            if (lastSegment)
            {
                quizButton.setText("Test me!");
                quizButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        quizButton.setVisibility(Button.INVISIBLE);
                        labelOverlay.removeAllViews();
                        startTest();
                    }
                });
            }
            else
            {
                quizButton.setText("Next Segment...");
                quizButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        learnNewSegment(true);
                    }
                });
            }
            quizButton.setVisibility(Button.VISIBLE);

            Labels.displayLabel(newSegment.getName());

            BrainModel.rotateToSegment(newSegment.getName());

            learnedSegments.add(newSegment);
            return newSegment;
        }

        return null;
    }

    private void startTest()
    {
        // find the unique attribute to test
        BrainSegment testSegment = null;
        String testTask = null;
        for (BrainSegment learnedSection : learnedSegments)
        {
            Set<String> tasks = learnedSection.getTasks();
            Log.d("QUIZMODE", "Checking segment " + learnedSection.getName());
            Log.d("QUIZMODE", "tasks are " + tasks.toString());
            for (BrainSegment otherSection : learnedSegments)
                if (otherSection != learnedSection)
                    tasks.removeAll(otherSection.getTasks());

            if (tasks.size() > 0)
            {
                Log.d("QUIZMODE", "testable segment found:" + learnedSection.getName());
                testSegment = learnedSection;
                testTask = (String) tasks.toArray()[0];
                break;
            }
        }

        // TODO: make it work when there are no unique tasks
        if (testSegment != null)
        {
            String questionTask = "Which section of the brain is responsible for " + testTask + "?";
            LinearLayout optionsLayout = (LinearLayout) inflater.inflate(R.layout.quiz_options, null);
            assert optionsLayout != null;
            TextView questionTitle = (TextView) optionsLayout.findViewById(R.id.question_title);
            LinearLayout questionsList = (LinearLayout) optionsLayout.findViewById(R.id.questions_list);

            questionTitle.setText(questionTask);

            RadioGroup buttonsGroup = new RadioGroup(context);
            List<RadioButton> buttonsList = new ArrayList<RadioButton>();

            for (final BrainSegment segment : learnedSegments)
            {
                RadioButton segmentButton = new RadioButton(context);
                segmentButton.setText(segment.getTitle());
                segmentButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        Log.d("QUIZMODE", "oncheckedchanged: " + segment.getTitle());
                        if (isChecked)
                            checkedSegment = segment;
                        else
                            checkedSegment = null;
                    }
                });

                buttonsList.add(segmentButton);
            }

            Collections.shuffle(buttonsList);

            for (RadioButton radioButton : buttonsList)
                buttonsGroup.addView(radioButton);

            quizButton.setText("Submit");
            quizButton.setVisibility(Button.VISIBLE);
            final BrainSegment finalTestSegment = testSegment;
            quizButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (checkedSegment != null)
                    {
                        Log.d("QUIZMODE", "Submitted quiz question" + checkedSegment.getName());
                        if (checkedSegment == finalTestSegment)

                        learnNewSegment(true);
                    }
                }
            });

            questionsList.addView(buttonsGroup);
            labelOverlay.addView(optionsLayout);
        }
    }

    @Override
    public void updateScene()
    {

    }

    @Override
    public boolean touchEvent(MotionEvent me)
    {
        return false;
    }

    @Override
    public void receiveEvent(String name, Object... data)
    {

    }

    public void setTopLabel(TextView topLabel)
    {
        this.topLabel = topLabel;
    }

    public void setMainOverlay(FrameLayout mainOverlay)
    {
        this.mainOverlay = mainOverlay;
    }
}
