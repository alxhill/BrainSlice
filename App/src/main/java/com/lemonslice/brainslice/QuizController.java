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

import com.lemonslice.brainslice.event.Events;

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
    private FrameLayout sidebarOverlay;
    private LinearLayout sidebar;
    private LinearLayout explainView;
    private Button playButton;

    private Context context;
    private ArrayList<BrainSegment> learnedSegments;
    private HashSet<String> testedTasks;
    private LayoutInflater inflater;

    private List<BrainSegment> segments;

    private BrainSegment checkedSegment;
    private FrameLayout quizView;

    public QuizController(Context context)
    {
        this.context = context;

        inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sidebar = (LinearLayout) inflater.inflate(R.layout.quiz_sidebar, null);
        sidebarOverlay = (FrameLayout) sidebar.findViewById(R.id.sidebar);
    }

    @Override
    public void loadView()
    {
        mainOverlay.removeAllViews();

        quizView = (FrameLayout) inflater.inflate(R.layout.quiz_screen, null);
        assert quizView != null;

        labelOverlay = (FrameLayout) quizView.findViewById(R.id.quiz_overlay);
        Labels.setFrameLayout(labelOverlay);

        final TextView quizTitle = (TextView) quizView.findViewById(R.id.quiz_intro);

        mainOverlay.addView(quizView);

        playButton = (Button) quizView.findViewById(R.id.play_button);
//        HomeScreen.comicNeue(playButton, 20);

        playButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                quizTitle.setVisibility(TextView.INVISIBLE);
                playButton.setVisibility(Button.INVISIBLE);
                startQuiz();
            }
        });

        BrainModel.smoothMoveToGeneric(BrainModel.startPosition, 0, 400);
        BrainModel.smoothRotateToFront();
        BrainModel.smoothZoom(0.25f, 400);
        BrainModel.setLabelsToDisplay(true);
        BrainModel.enableBackgroundGlow();
    }

    @Override
    public void unloadView()
    {
        mainOverlay.removeAllViews();
        learnedSegments = new ArrayList<BrainSegment>();
    }

    private void startQuiz()
    {
        // initialise the lists needed for testing
        segments = new ArrayList<BrainSegment>();
        learnedSegments = new ArrayList<BrainSegment>();
        testedTasks = new HashSet<String>();

        // populate the list of segments with only those that have valid positions
        for (BrainSegment segment : BrainInfo.getSegments().values())
            if (segment.getPosition() != null)
                segments.add(segment);

        BrainModel.smoothMoveToGeneric(BrainModel.sidePosition, 0, 400);
        BrainModel.smoothZoom(0.20f, 400);
        showExplainView(
                "Welcome to quiz mode!",
                "We'll start by teaching you about two different sections of the brain, then ask you a question.",
                "Get started!",
                null,
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        learnNewSegment(false);
                    }
                },
                null);

    }

    // shows the user a segment of the brain they have not yet seen
    private BrainSegment learnNewSegment(boolean lastSegment)
    {
        BrainSegment newSegment;

        if (segments.size() == 0)
        {
            showExplainView(
                    "Well done, you've completed quiz mode!",
                    "",
                    "< Continue Testing",
                    "Return to Menu",
                    new Button.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            startTest();
                        }
                    },
                    new Button.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            Events.trigger("tap:home");
                        }
                    });
            return null;
        }

        Collections.shuffle(segments);

        for (BrainSegment segment : segments)
        {
            newSegment = segment;
            Log.d("BRAINQUIZ", "new segment is " + newSegment.getName());

            if (lastSegment)
            {
                playButton.setText("Test me!");
                playButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        playButton.setVisibility(Button.INVISIBLE);
                        labelOverlay.removeAllViews();
                        startTest();
                    }
                });
            }
            else
            {
                playButton.setText("Next Segment...");
                playButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        learnNewSegment(true);
                    }
                });
            }
            playButton.setVisibility(Button.VISIBLE);

            mainOverlay.removeAllViews();
            mainOverlay.addView(quizView);

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

        if (testSegment != null && testTask != null)
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
                        {
                            checkedSegment = segment;
                            BrainModel.rotateToSegment(segment.getName());
                        }
                    }
                });

                buttonsList.add(segmentButton);
            }

            Collections.shuffle(buttonsList);

            for (RadioButton radioButton : buttonsList)
                buttonsGroup.addView(radioButton);

            Button quizButton = (Button) optionsLayout.findViewById(R.id.submit_button);
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
                            testedTasks.add(finalTestTask);
                            Log.d("QUIZMODE", "making explain view for " + checkedSegment.getTitle());
                            Log.d("QUIZMODE", "learnedsize:" + learnedSegments.size() + ", segmentsize:" + segments.size());
                            checkedSegment = null;

                            showExplainView(
                                    "Well done!",
                                    "The " + finalTestSegment.getTitle() + " is responsible for " + finalTestTask + ".",
                                    "< Keep Testing",
                                    "Continue Learning >",
                                    new Button.OnClickListener() {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            startTest();
                                        }
                                    },
                                    new Button.OnClickListener() {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            learnNewSegment(true);
                                        }
                                    }
                            );
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
            sidebarOverlay.removeAllViews();
            sidebarOverlay.addView(optionsLayout);
            mainOverlay.removeAllViews();
            mainOverlay.addView(sidebar);
        }
        else
        {
            showExplainView(
                    "Well done!",
                    "You've learnt everything in about those sections of the brain.",
                    "Continue Learning >",
                    null,
                    new Button.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            learnNewSegment(true);
                        }
                    },
                    null);
        }
    }

    private void showExplainView(String top,
                                 String middle,
                                 String leftButtonTitle,
                                 String rightButtonTitle,
                                 Button.OnClickListener leftButtonListener,
                                 Button.OnClickListener rightButtonListener)
    {
        if (explainView == null)
            explainView = (LinearLayout) inflater.inflate(R.layout.quiz_explain_view, null);

        final TextView topText = (TextView) explainView.findViewById(R.id.quiz_explain_top);
        final TextView middleText = (TextView) explainView.findViewById(R.id.quiz_explain_middle);
        final Button leftButton = (Button) explainView.findViewById(R.id.quiz_button_left);
        final Button rightButton = (Button) explainView.findViewById(R.id.quiz_button_right);

        topText.setText(top);
        middleText.setText(middle);

        leftButton.setVisibility(Button.VISIBLE);
        rightButton.setVisibility(Button.VISIBLE);

        if (leftButtonTitle == null)
            leftButton.setVisibility(Button.GONE);
        else
        {
            leftButton.setText(leftButtonTitle);
            leftButton.setOnClickListener(leftButtonListener);
        }

        if (rightButtonTitle == null)
            rightButton.setVisibility(Button.GONE);
        else
        {
            rightButton.setText(rightButtonTitle);
            rightButton.setOnClickListener(rightButtonListener);
        }

        mainOverlay.removeAllViews();
        mainOverlay.addView(sidebar);
        sidebarOverlay.removeAllViews();
        sidebarOverlay.addView(explainView);
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
}
