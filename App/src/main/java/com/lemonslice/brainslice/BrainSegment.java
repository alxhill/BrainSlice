package com.lemonslice.brainslice;

import android.util.Log;

import com.threed.jpct.SimpleVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

/**
 * Created by James on 29/01/14.
 */

public class BrainSegment {
    private String title;
    private String description;
    private SimpleVector position;
    private HashMap<String, Object> metadata = new HashMap<String, Object>();
    private ArrayList<String[]> questions = new ArrayList<String[]>();  //First string is question, second is correct answer, rest are answers.
    private ArrayList<Integer> answer = new ArrayList<Integer>();

    public BrainSegment(String title, String description, SimpleVector position, HashMap<String, Object> metadata, ArrayList<String[]> questions, ArrayList<Integer> answer)
    {
        this.title = title;
        this.description = description;
        this.position = position;
        this.metadata = metadata;
        this.questions = questions;
        this.answer = answer;
    }

    public BrainSegment(String title, String description, SimpleVector position, ArrayList<String[]> questions, ArrayList<Integer> answer)
    {
        this.title = title;
        this.description = description;
        this.position = position;
        this.questions = questions;
        this.answer = answer;
        Log.d("BrainSlice", "Created "+title);
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public HashMap<String, Object> getMetadata()
    {
        return metadata;
    }

    public Object getMetaData(String key){ return metadata.get(key); }

    public void setMetadata(String key, Object obj) { this.metadata.put(key, obj);}

    public SimpleVector getPosition()
    {
        return position;
    }

    public void setPosition(SimpleVector position)
    {
        this.position = position;
    }

    public String[] getRandomQuestion(){
        Random r = new Random();
        return questions.get(r.nextInt(questions.size()));
    }

    //Returns index of question array tat has the correct answer
    public int getCorrectAnswer(){

    }
}
