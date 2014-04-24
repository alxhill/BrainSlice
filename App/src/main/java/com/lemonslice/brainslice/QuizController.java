package com.lemonslice.brainslice;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lemonslice.brainslice.event.EventListener;
import com.lemonslice.brainslice.event.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by alexander on 4/23/14.
 */
public class QuizController extends AbstractController implements EventListener {

    private TextView topLabel;
    private FrameLayout mainOverlay;
    private FrameLayout labelOverlay;

    private Context context;
    private ArrayList<BrainSegment> learnedSections;
    private LayoutInflater inflater;

    private boolean isQuizzing = false;
    private Iterator<BrainSegment> segmentIterator;

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

        FrameLayout quizView = (FrameLayout) inflater.inflate(R.layout.quiz_screen, null);
        assert quizView != null;

        labelOverlay = (FrameLayout) quizView.findViewById(R.id.quiz_overlay);
        Labels.setFrameLayout(labelOverlay);

        final Button playButton = (Button) quizView.findViewById(R.id.play_button);
        playButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                playButton.setVisibility(Button.INVISIBLE);
                startQuiz();
            }
        });

        mainOverlay.addView(quizView);
    }

    @Override
    public void unloadView()
    {
        topLabel.setText("");
        mainOverlay.removeAllViews();
        Labels.setFrameLayout(mainOverlay);
        learnedSections = new ArrayList<BrainSegment>();
    }

    private void startQuiz()
    {
        isQuizzing = true;
        segmentIterator = BrainInfo.getSegments().values().iterator();
        learnedSections = new ArrayList<BrainSegment>();
        topLabel.setText("");
        learnNewSegment();
    }

    // shows the user a segment of the brain they have not yet seen
    private BrainSegment learnNewSegment()
    {
        if (segmentIterator.hasNext())
        {
            BrainSegment newSegment = segmentIterator.next();
            Log.d("BRAINQUIZ", "new segment is " + newSegment.getName());

            Labels.displayLabel(newSegment.getName());

            BrainModel.smoothMoveToGeneric(BrainModel.sidePosition, 0, 400);
            BrainModel.smoothZoom(0.25f, 400);
            BrainModel.rotateToSegment(newSegment.getName());

            learnedSections.add(newSegment);
            return newSegment;
        }
        return null;
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
