package com.lemonslice.brainslice;

import android.content.res.Resources;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import com.threed.jpct.GLSLShader;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

import java.util.Timer;
import java.util.TimerTask;

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

        objs = Loader.loadSerializedObjectArray(res.openRawResource(R.raw.brain_model));
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
        plane.rotateX((float) -Math.PI / 2.0f);
        scale(0.5f);

        plane.build();
        plane.strip();

        // Centre the model (as calculated with painful trial and error)
        plane.setOrigin(SimpleVector.create(0, 20, 10));

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
        Matrix m = plane.getRotationMatrix().cloneMatrix().invert3x3();
        m.orthonormalize();
        m.matMul(frontMatrix);

        float R[] = m.getDump();
        float axis[] = new float[3];

        SensorManager.getOrientation(R, axis);

        float x = axis[1];
        float y = axis[2];

        if (Float.isNaN(y) || Float.isNaN(x))
        {
            Log.e("BrainSlice", "Invalid x and y values");
            return;
        }

        float maxDist = 20.0f;
        float minDist = 10.0f;

        float ex = (float) (maxDist*Math.cos(y));
        float ey = (float) (minDist*Math.sin(y));

        float dist = (float) Math.sqrt(ex*ex + ey*ey);

        float maxSize = 0.6f;
        float minSize = 0.5f;

        float distScale = (dist - minDist) / (maxDist - minDist);

        distScale = 1.0f - distScale;

        float xAdjust = 0;
        float scaleFactor = distScale*(maxSize - minSize);

        final double eighthPi = Math.PI / 8.0f;

        if (x < eighthPi && x > -eighthPi)
            xAdjust = (float) ((eighthPi - Math.abs(x)) / eighthPi);

        //plane.setScale(scaleFactor*xAdjust + minSize);


    }

    public static void scale(float scale)
    {
        plane.scale(scale);
        shader.setUniform("heightScale", 1.0f);
    }

    public static void moveToFront()
    {
        Log.d("BrainSlice", "moveToFront");

        double e1, e2, e3;

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
        final double angle = Math.acos((r.get(0, 0) + r.get(1, 1) + r.get(2, 2) - 1.0f) / 2.0f);

        double sinTheta = 2 * Math.sin(angle);

        // vector values representing the axis
        e1 = (r.get(2, 1) - r.get(1, 2)) / sinTheta;
        e2 = (r.get(0, 2) - r.get(2, 0)) / sinTheta;
        e3 = (r.get(1, 0) - r.get(0, 1)) / sinTheta;

        if (Double.isNaN(e1) || Double.isNaN(e2) || Double.isNaN(e3) || Double.isNaN(angle))
            return;

        final SimpleVector axis = new SimpleVector((float) e1, (float) e2, (float) e3);

//        Log.d("BrainSlice", String.format("axis-angle: %s %s", axis.toString(), angle));



        final int zoomTime=1200;
        Timer zoomTimer = new Timer();
        zoomTimer.schedule(new TimerTask() {
            final double scaleDiff = 0.6f - getScale();
            // time in ms for each step
            final int stepTime = 15;
            // current number of milliseconds elapsed
            int i = stepTime;
            @Override
            public void run() {
                //double stepZoom = (easeOutElastic(scaleDiff,i,zoomTime) - easeOutElastic(scaleDiff,i - stepTime,zoomTime) + getScale()) / getScale();
                double stepZoom = (easeOutExpo(scaleDiff,i,zoomTime) - easeOutExpo(scaleDiff,i - stepTime,zoomTime) + getScale()) / getScale();
                scale((float) stepZoom);
                i += stepTime;
                if (i >= zoomTime) cancel();
            }
        },100,15);


        final int rotateTime = 300 + (int) Math.round(Math.abs(angle)*350.0f);
        Timer rotateTimer = new Timer();
        rotateTimer.schedule(new TimerTask() {
            // time in ms for each step
            final int stepTime = 15;
            // current number of milliseconds elapsed
            int i = stepTime;
            @Override
            public void run()
            {
                // calculate the next rotation step to move by
                //double stepRotation = easeOutExpo(angle, i, time) - easeOutExpo(angle, i - stepTime, time);
                double stepRotation = easeOutElastic(angle, i, rotateTime) - easeOutElastic(angle, i - stepTime, rotateTime);
                plane.rotateAxis(axis, (float) stepRotation); 
                i += stepTime;
                if (i >= rotateTime) cancel();
            }

        }, 0, 15);
    }

    // adapted from http://easings.net/
    private static double easeOutExpo(double delta, double currentTime, double totalTime)
    {
        if (currentTime == totalTime) return delta;
        return delta * (-Math.pow(2, -10 * currentTime/totalTime) + 1);
    }


    public static float getScale()
    {
        return plane.getScale();
    }


    // adapted from http://easings.net/
    private static double easeOutElastic(double delta, double currentTime, double totalTime)
    {
        if (currentTime==0)
            return 1;

        currentTime = currentTime/totalTime;
        if (currentTime==1)
            return 1+delta;

        double p=totalTime*0.5;
        double s = p/4;
        return delta*Math.pow(2,-10*currentTime) * Math.sin( (currentTime*totalTime-s)*(2*Math.PI)/p ) + delta + 1;
    }

    // adapted from http://easings.net/
    private static double easeOutBack(double delta, double currentTime, double totalTime)
    {
        double s = 1.70158;
        return delta*((currentTime=currentTime/totalTime-1)*currentTime*((s+1)*currentTime + s) + 1) + 1;
    }
}
