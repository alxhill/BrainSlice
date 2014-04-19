package com.lemonslice.brainslice;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

public class QuestionPopup extends DialogFragment {

    private ArrayList<String> question = new ArrayList();
    private int correctAnswer;

    public QuestionPopup(BrainSegment brainSegment){
        String[] q =  brainSegment.getRandomQuestion();

        for(int i = 0; i < 5; i++)
            question(i).add()
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.question_popup, container);
        getDialog().setTitle("Quiz Time!");

        TextView tv = (TextView) view.findViewById(R.id.question_text);
        tv.setText(question[0]);

        RadioButton rb = (RadioButton) view.findViewById(R.id.answer1);
        rb.setText(question[]);

        Button answer = (Button) view.findViewById(R.id.answer_button);
        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        final DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        view.setMinimumHeight((int) (Math.max(displaymetrics.widthPixels, displaymetrics.heightPixels) * 0.8f));
        view.setMinimumWidth((int) (Math.min(displaymetrics.widthPixels, displaymetrics.heightPixels) * 0.8f));
        return view;
    }

    public static QuestionPopup newInstance(BrainSegment brainSection) {
        QuestionPopup qp = new QuestionPopup(BrainSegment brainSection);
        Bundle args = new Bundle();
        qp.setArguments(args);
        return qp;
    }
}