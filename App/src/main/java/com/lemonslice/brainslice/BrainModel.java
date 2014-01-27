package com.lemonslice.brainslice;

import android.content.res.Resources;
import android.util.Log;

import com.threed.jpct.GLSLShader;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

/**
 * Renders the brain to the screen and handles moving the model
 * Created by alexander on 27/01/2014.
 */
public class BrainModel {

    private float x, y, z;

    // magic 3D stuff
    private Object3D plane;
    private Object3D[] objs;
    private GLSLShader shader = null;

    public BrainModel() {
    }

    public void load(Resources res)
    {

        // brain is parented to small plane
        plane = Primitives.getPlane(1, 1);

        plane.setCulling(true);

        // Load the 3d model
        Log.d("BrainSlice", "Loading .3ds file");
        objs = Loader.loadOBJ(res.openRawResource(R.raw.brain_new), res.openRawResource(R.raw.brain_material), 10.0f);
        Log.d("BrainSlice","Loaded .3ds file");

        //number of subobjs for brain
        int len = objs.length;

        // compile and load shaders for plane
        shader = new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.vertexshader_offset)),
                Loader.loadTextFile(res.openRawResource(R.raw.fragmentshader_offset)));
        plane.setShader(shader);
        plane.setSpecularLighting(true);
        shader.setStaticUniform("invRadius", 0.0003f);

        // initialise brain sub-objs
        for(int i=0; i<len; i++)
        {
            objs[i].setCulling(true);
            objs[i].setSpecularLighting(false); //was true
            objs[i].build();
            objs[i].strip();
            objs[i].addParent(plane);
        }

        // Set the model's initial position
        plane.rotateX(-3.141592f / 2.0f);

        plane.build();
        plane.strip();

        // Centre the model
        plane.setOrigin(SimpleVector.create(0, 0, 10));

    }

    public void addToScene(World world) {
        world.addObject(plane);
        world.addObjects(objs);
    }

    public SimpleVector getTransformedCenter()
    {
        return plane.getTransformedCenter();
    }

    public void rotate(float x, float y, float z)
    {
        plane.rotateX(x);
        plane.rotateY(y);
        plane.rotateZ(z);
    }

    public void scale(float scale)
    {
        plane.scale(scale);
        // I have no idea if we need this
        shader.setUniform("heightScale", 1.0f);
    }

}
