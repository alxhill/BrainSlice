package com.lemonslice.brainslice;

import android.content.res.Resources;
import android.os.SystemClock;
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

    private static float absX, absY, absZ = 0;

    // magic 3D stuff
    private static Object3D plane;
    private static Object3D[] objs;
    private static GLSLShader shader = null;

    public static void load(Resources res)
    {

        // brain is parented to small plane
        plane = Primitives.getPlane(1, 1);

        plane.setCulling(true);

        // Load the 3d model
        Log.d("BrainSlice", "Loading .3ds file");
        objs = Loader.load3DS(res.openRawResource(R.raw.brain_fast), 10.0f);
        Log.d("BrainSlice", "Loaded .3ds file");

        // compile and load shaders for plane
        shader = new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.vertexshader_offset)),
                Loader.loadTextFile(res.openRawResource(R.raw.fragmentshader_offset)));
        plane.setShader(shader);
        plane.setSpecularLighting(true);
        shader.setStaticUniform("invRadius", 0.0003f);

        // initialise brain sub-objs
        for (Object3D obj : objs)
        {
            obj.setCulling(true);
            obj.setSpecularLighting(false); //was true
            obj.build();
            obj.strip();
            obj.addParent(plane);
        }

        // Set the model's initial position
        plane.rotateX(-3.141592f / 2.0f);
        //rotate(-3.141592f / 2.0f,0,0);
        scale(0.05f);

        plane.build();
        plane.strip();

        // Centre the model
        plane.setOrigin(SimpleVector.create(0, 0, 10));

    }

    public static void addToScene(World world)
    {
        world.addObject(plane);
        world.addObjects(objs);
    }

    public static SimpleVector getTransformedCenter()
    {
        return plane.getTransformedCenter();
    }

    public static void rotate(float x, float y, float z)
    {
        absX += x;
        absY += y;
        absZ += z;
        plane.rotateX(x);
        plane.rotateY(y);
        plane.rotateZ(z);
    }

    public static void scale(float scale)
    {
        plane.scale(scale);
        // I have no idea if we need this
        shader.setUniform("heightScale", 1.0f);
    }

    public static void smoothMove(float x, float y, float z, int time)
    {
        Log.d("BrainSlice", "smoothMove");
        float xDiff = (x - absX) % (2*(float)Math.PI);
        float yDiff = (y - absY) % (2*(float)Math.PI);
        float zDiff = (z - absZ) % (2*(float)Math.PI);

        if (xDiff > (float)Math.PI) xDiff = 2*(float)Math.PI - xDiff;
        if (yDiff > (float)Math.PI) yDiff = 2*(float)Math.PI - yDiff;
        if (zDiff > (float)Math.PI) zDiff = 2*(float)Math.PI - zDiff;

        float originalAbsX = absX;
        float originalAbsY = absY;
        float originalAbsZ = absZ;

        for (int i=0; i<time; i++)
        {
            float newAbsX = easeOutExpo(i,originalAbsX,xDiff,time);
            float newAbsY = easeOutExpo(i,originalAbsY,yDiff,time);
            float newAbsZ = easeOutExpo(i,originalAbsZ,zDiff,time);

            rotate(newAbsX - absX,newAbsY - absY,newAbsZ - absZ);

            SystemClock.sleep(1);
        }

        //absX = x;
        //absY = y;
        //absZ = z;
    }

    private static float easeOutExpo(float t, float b, float c, float d)
    {
        return c * (float)(-Math.pow(2, -10 * t/d) + 1) + b;
    }

}
