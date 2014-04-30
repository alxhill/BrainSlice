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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by alexander on 4/23/14.
 */
public class QuizController extends AbstractController {

    private FrameLayout mainOverlay;
    private FrameLayout labelOverlay;
    private Button quizButton;

    private Context context;
    private ArrayList<BrainSegment> learnedSegments;
    private HashSet<String> testedTasks;
    private LayoutInflater inflater;

    private boolean isQuizzing = false;
    private List<BrainSegment> segments;

    private BrainSegment checkedSegment;
    private TextView quizTitle;

    public QuizController(Context context)
    {
        this.context = context;

        inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void loadView()
    {
        mainOverlay.removeAllViews();

        final FrameLayout quizView = (FrameLayout) inflater.inflate(R.layout.quiz_screen, null);
        assert quizView != null;

        labelOverlay = (FrameLayout) quizView.findViewById(R.id.quiz_overlay);
        Labels.setFrameLayout(labelOverlay);

        mainOverlay.addView(quizView);

//        quizTitle = (TextView) mainOverlay.findViewById(R.id.question_title);
        quizTitle.setText("Press play to start quizzing!");

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
        mainOverlay.removeAllViews();
        Labels.setFrameLayout(mainOverlay);
        learnedSegments = new ArrayList<BrainSegment>();
    }

    private void startQuiz()
    {
        isQuizzing = true;
        segments = new ArrayList<BrainSegment>(BrainInfo.getSegments().values());
        learnedSegments = new ArrayList<BrainSegment>();
        testedTasks = new HashSet<String>();

        BrainModel.smoothMoveToGeneric(BrainModel.sidePosition, 0, 400);
        BrainModel.smoothZoom(0.25f, 400);
        BrainModel.setLabelsToDisplay(true);

        learnNewSegment(false);
    }

    // shows the user a segment of the brain they have not yet seen
    private BrainSegment learnNewSegment(boolean lastSegment)
    {
        BrainSegment newSegment;

        if (segments.size() == 0)
        {
            Toast.makeText(context, "Well done, you win quiz mode!", Toast.LENGTH_SHORT).show();
            isQuizzing = false;
            return null;
        }

        Collections.shuffle(segments);

        for (BrainSegment segment : segments)
        {
            newSegment = segment;
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
            segments.remove(newSegment);
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
                Object[] taskArray = tasks.toArray();
                for (Object task : taskArray)
                {
                    String taskStr = (String) task;
                    if (!testedTasks.contains(taskStr))
                    {
                        testTask = taskStr;
                        break;
                    }
                }

                if (testTask != null)
                    break;
            }
        }

        // TODO: make it work when there are no unique tasks
        if (testSegment != null)
        {
            String questionTask = "Which section of the brain is responsible for " + testTask + "?";
            LinearLayout optionsLayout = (LinearLayout) inflater.inflate(R.layout.quiz_options, null);
            assert optionsLayout != null;
            LinearLayout questionsList = (LinearLayout) optionsLayout.findViewById(R.id.questions_list);
            TextView questionTitle = (TextView) optionsLayout.findViewById(R.id.question_title);

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
            final String finalTestTask = testTask;

            quizButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (checkedSegment != null)
                    {
                        if (checkedSegment == finalTestSegment)
                        {
                            Toast.makeText(context, "Well done, that's right!", Toast.LENGTH_SHORT).show();
                            testedTasks.add(finalTestTask);
                            checkedSegment = null;
                            learnNewSegment(true);
                        }
                        else
                        {
                            Toast.makeText(context, "Wrong answer, try again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Log.d("QUIZMODE", "no section was checked");
                        Toast.makeText(context, "Select an option to continue.", Toast.LENGTH_SHORT).show();
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

    public void setMainOverlay(FrameLayout mainOverlay)
    {
        this.mainOverlay = mainOverlay;
    }

    public void setOverlayLabel(TextView overlayLabel)
    {
        this.quizTitle = overlayLabel;
    }
}
