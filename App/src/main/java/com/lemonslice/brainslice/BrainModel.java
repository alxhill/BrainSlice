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
        scale(0.05f);

        plane.build();
        plane.strip();

        // Centre the model
        plane.setOrigin(SimpleVector.create(0, 0, 10));

        frontMatrix = new Matrix(plane.getRotationMatrix());
        // removes the scale to make it a pure rotation matrix
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

    public static void scale(float scale)
    {
        plane.scale(scale);
        // I have no idea if we need this
        shader.setUniform("heightScale", 1.0f);
    }

    public static void smoothMove(int time)
    {
        Log.d("BrainSlice", "smoothMove");

        float epsilon = 0.01f;

        double e1, e2, e3, angle;
        SimpleVector axis = new SimpleVector();

        Matrix r = plane.getRotationMatrix().cloneMatrix().invert3x3();
        r.orthonormalize();


        Log.d("BrainSlice", "front matrix (normalised): " + frontMatrix.toString());
        Log.d("BrainSlice", "plane matrix: " + r.toString());
        r.matMul(frontMatrix);
        Log.d("BrainSlice", "rotation matrix: " + r.toString());

        // convert matrix into axis-angle representation
        angle = Math.acos((r.get(0, 0) + r.get(1, 1) + r.get(2, 2) - 1.0f) / 2.0f);

        double sinTheta = 2 * Math.sin(angle);

        Log.d("BrainSlice", "sinTheta: " + sinTheta);

        e1 = (r.get(2, 1) - r.get(1, 2)) / sinTheta;
        e2 = (r.get(0, 2) - r.get(2, 0)) / sinTheta;
        e3 = (r.get(1, 0) - r.get(0, 1)) / sinTheta;

        Log.d("BrainSlice", "e1: " + e1);
        Log.d("BrainSlice", "e2: " + e2);
        Log.d("BrainSlice", "e3: " + e3);

        axis.set((float) e1, (float) e2, (float) e3);

        Log.d("BrainSlice", String.format("axis-angle: %s %s", axis.toString(), angle));

        float stepRotation = (float) (angle / time);
        for (int i = 0; i < time; i++)
        {
            plane.rotateAxis(axis, stepRotation);
            SystemClock.sleep(1);
        }
    }

    private static float easeOutExpo(float t, float b, float c, float d)
    {
        return c * (float)(-Math.pow(2, -10 * t/d) + 1) + b;
    }

}
