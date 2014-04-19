package com.lemonslice.brainslice;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import java.util.ArrayList;
import java.util.Arrays;

public class QuestionPopup extends DialogFragment {

    private ArrayList<String> question = new ArrayList();
    private int correctAnswer;
    private int guessedAnswer = -1;

    public QuestionPopup(BrainSegment brainSegment){
        question = new ArrayList(Arrays.asList(brainSegment.getRandomQuestion()));
        correctAnswer = brainSegment.getCorrectAnswer();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.question_popup, container);
        getDialog().setTitle("Quiz Time!");

        TextView tv = (TextView) view.findViewById(R.id.question_text);
        tv.setText(question.get(0));

        RadioButton rb = (RadioButton) view.findViewById(R.id.answer1);
        rb.setText(question.get(1));
        rb = (RadioButton) view.findViewById(R.id.answer2);
        rb.setText(question.get(2));
        rb = (RadioButton) view.findViewById(R.id.answer3);
        rb.setText(question.get(3));
        rb = (RadioButton) view.findViewById(R.id.answer4);
        rb.setText(question.get(4));

        RadioGroup rg = (RadioGroup) view.findViewById(R.id.radiogroup);
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int radioButtonId) {

                switch(radioButtonId) {
                    case R.id.answer1:
                        guessedAnswer = 1;
                        break;
                    case R.id.answer2:
                        guessedAnswer = 2;
                        break;
                    case R.id.answer3:
                        guessedAnswer = 3;
                        break;
                    case R.id.answer4:
                        guessedAnswer = 4;
                        break;
                }

                Log.d("Quiz", "Checked answer " + guessedAnswer);
            }
        });

        Button answer = (Button) view.findViewById(R.id.answer_button);
        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(guessedAnswer == correctAnswer)
                    correct();
                else if(guessedAnswer != -1)
                    incorrect();
            }
        });

        final DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        view.setMinimumHeight((int) (Math.max(displaymetrics.widthPixels, displaymetrics.heightPixels) * 0.8f));
        view.setMinimumWidth((int) (Math.min(displaymetrics.widthPixels, displaymetrics.heightPixels) * 0.8f));
        return view;
    }

    public static QuestionPopup newInstance(BrainSegment brainSection) {
        QuestionPopup qp = new QuestionPopup(brainSection);
        Bundle args = new Bundle();
        qp.setArguments(args);
        return qp;
    }

    private void correct(){
        Log.d("Quiz", "Correct!");
    }

    private void incorrect(){
        Log.d("Quiz", "Incorrect :(");
    }
}