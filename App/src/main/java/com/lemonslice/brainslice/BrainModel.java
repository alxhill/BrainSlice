package com.lemonslice.brainslice;

import android.content.res.Resources;
import android.os.SystemClock;
import android.util.Log;

import com.threed.jpct.GLSLShader;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
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

    private static Matrix frontMatrix;

    public static void load(Resources res)
    {

        // brain is parented to small plane
        plane = Primitives.getPlane(1, 1);

        plane.setCulling(true);

        // Load the 3d model
        Log.d("BrainSlice", "Loading .3ds file");
        objs = Loader.loadSerializedObjectArray(res.openRawResource(R.raw.brain_complete_ser));
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
        plane.rotateY((float) Math.PI);
        plane.rotateX((float) -Math.PI/2.0f);
        scale(0.5f);

        plane.build();
        plane.strip();

        // Centre the model
        plane.setOrigin(SimpleVector.create(0, 0, 10));

        // get the rotation matrix for the current position
        frontMatrix = new Matrix(plane.getRotationMatrix());
        // removes scale from the rotation matrix
        frontMatrix.orthonormalize();
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
        plane.rotateX(x);
        plane.rotateY(y);
        plane.rotateZ(z);
    }

    // moves the camera so it's a constant distance from the brain.
    public static void adjustCamera()
    {
        Matrix m = plane.getRotationMatrix().cloneMatrix();
        m.orthonormalize();

        Log.d("BrainSlicelol", m.toString());

        float y = (float) Math.atan2(-m.get(2, 0), m.get(0, 0));
        float z = (float) Math.asin(m.get(1, 0));
        float x = (float) Math.atan2(-m.get(1,2), m.get(1,1));

        if(Float.isNaN(y))
        {
            Log.d("BrainSlicelol", "failll");
            return;
        }

        Log.d("BrainSlicecunt3", String.valueOf(y));

        float ex = (float) (20.0f*Math.cos(y));
        float ey = (float) (10.0f*Math.sin(y));

        float dist = (float) Math.sqrt(ex*ex + ey*ey);

        float maxDist = 20.0f;
        float minDist = 10.0f;

        float maxSize = 0.6f;
        float minSize = 0.5f;

        float distScale = (dist - minDist) / (maxDist - minDist);

        distScale = 1.0f - distScale;

        Log.d("BrainSlicecunt2", String.valueOf(distScale));

        float scale = distScale*(maxSize - minSize) + minSize;

        Log.d("BrainSlicecunt", String.valueOf(scale));

        plane.setScale(scale);


    }

    public static void scale(float scale)
    {
        plane.scale(scale);
        // I have no idea if we need this
        shader.setUniform("heightScale", 1.0f);
    }

    public static void smoothMove(int time)
    {
        Log.d("BrainSlice", "smoothMove");

        double e1, e2, e3, angle;
        SimpleVector axis = new SimpleVector();

        Matrix r = plane.getRotationMatrix().cloneMatrix().invert3x3();
        r.orthonormalize();
        r.matMul(frontMatrix);

        Log.d("BrainSlice", "front matrix: " + frontMatrix.toString());
        Log.d("BrainSlice", "rotation matrix: " + r.toString());

        /*
        convert matrix into axis-angle representation.
        see wikipedia for explanation of why/how this works:
        https://en.wikipedia.org/wiki/Rotation_formalisms_in_three_dimensions#Rotation_matrix_.E2.86.94_Euler_axis.2Fangle
        */

        // angle to rotate about the axis
        angle = Math.acos((r.get(0, 0) + r.get(1, 1) + r.get(2, 2) - 1.0f) / 2.0f);

        double sinTheta = 2 * Math.sin(angle);

        // vector values representing the axis
        e1 = (r.get(2, 1) - r.get(1, 2)) / sinTheta;
        e2 = (r.get(0, 2) - r.get(2, 0)) / sinTheta;
        e3 = (r.get(1, 0) - r.get(0, 1)) / sinTheta;

        if (Double.isNaN(e1) || Double.isNaN(e2) || Double.isNaN(e3) || Double.isNaN(angle))
            return;

        axis.set((float) e1, (float) e2, (float) e3);

//        Log.d("BrainSlice", String.format("axis-angle: %s %s", axis.toString(), angle));

        for (int i = 1; i <= time; i++)
        {
            double stepRotation = easeOutExpo(angle, i, time) - easeOutExpo(angle, i-1, time);
            plane.rotateAxis(axis, (float) stepRotation);
            SystemClock.sleep(1);
        }

    }

    // adapted from http://easings.net/
    private static double easeOutExpo(double delta, double currentTime, double totalTime)
    {
        if (currentTime == totalTime) return delta;
        return delta * (-Math.pow(2, -10 * currentTime/totalTime) + 1);
    }

}
