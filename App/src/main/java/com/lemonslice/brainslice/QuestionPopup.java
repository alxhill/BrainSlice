package com.lemonslice.brainslice;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class QuestionPopup extends DialogFragment {
    public QuestionPopup(){

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.question_popup, container);
        getDialog().setTitle("Quiz Time!");

        Button answer = (Button) view.findViewById(R.id.answer_button);
        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return view;
    }

    public static QuestionPopup newInstance(BrainSegment brainSection) {
        QuestionPopup qp = new QuestionPopup();
        Bundle args = new Bundle();
        qp.setArguments(args);
        return qp;
    }
}