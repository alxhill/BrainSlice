package com.lemonslice.brainslice;

import android.content.res.Resources;
import android.hardware.SensorManager;
import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.GLSLShader;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.Interact2D;

import java.util.Timer;
import java.util.TimerTask;

import java.util.concurrent.Semaphore;

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

    private static Semaphore brainSemaphore = new Semaphore(1);

    private static Matrix frontMatrix;

    private static Object3D[] spheres = null;
//    private static RGBColor sphereNormalColor = new RGBColor(255, 255, 255);
//    private static RGBColor sphereTouchedColor = new RGBColor(255, 255, 0);

    private static Camera cam = null;
    private static FrameBuffer buf = null;

    private static float sphereRad = 2.0f;

    private static boolean isLoaded = false;

    private static int selection = -1;

    private static SimpleVector sidePosition = SimpleVector.create(-20,20,10);
    public static SimpleVector startPosition = SimpleVector.create(0,20,10);

    private static GLSLShader[] shads = new GLSLShader[6];

    private static String[] brainSegments =
    {
        "Frontal lobe",
        "Parietal lobe",
        "Occipital lobe",
        "Temporal lobe",
        "Cerebellum",
        "Brainstem"
    };

    private static Matrix[] segmentRotations = new Matrix[6];

    public static void load(Resources res)
    {
        shader = new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.vertexshader_offset)),
                                Loader.loadTextFile(res.openRawResource(R.raw.fragmentshader_spheres)));

        // brain is parented to small plane
        plane = Primitives.getPlane(1, 1);

        plane.setCulling(true);

        spheres = new Object3D[6];

        for(int i=0; i<spheres.length; i++)
        {
            spheres[i] = Primitives.getSphere(sphereRad);
            spheres[i].addParent(plane);
            spheres[i].setLighting(Object3D.LIGHTING_NO_LIGHTS);
            spheres[i].build();
            spheres[i].compile();
            spheres[i].strip();
        }

        spheres[0].translate(SimpleVector.create(0, -70.0f, 0));
        spheres[1].translate(SimpleVector.create(0, 80.0f, -50.0f));
        spheres[2].translate(SimpleVector.create(0, 100.0f, 0));
        spheres[3].translate(SimpleVector.create(75.0f, 0, 0));
        spheres[4].translate(SimpleVector.create(0, 100.0f, 50.0f));
        spheres[5].translate(SimpleVector.create(0, 12.0f, 40.0f));

        for(int i=0; i<spheres.length; i++)
        {
            shads[i] = new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.vertexshader_offset)),
                    Loader.loadTextFile(res.openRawResource(R.raw.fragmentshader_spheres)));

            shads[i].setUniform("isSelected", 0);

            spheres[i].setShader(shads[i]);
        }

        // Load the 3d model
        Log.d("BrainSlice", "Loading .3ds file");

        objs = Loader.loadSerializedObjectArray(res.openRawResource(R.raw.brain_model));
        Log.d("BrainSlice", "Loaded .3ds file");

        // compile and load shaders for plane
        plane.setShader(shader);
        plane.setSpecularLighting(true);
        shader.setStaticUniform("invRadius", 0.0003f);

        // initialise brain sub-objs
        for (Object3D obj : objs)
        {
            obj.setCulling(true);
            obj.setSpecularLighting(false); //was true
            obj.build();
            obj.compile();
            obj.strip();
            obj.addParent(plane);
        }

        // Set the model's initial position
        plane.rotateY((float) Math.PI);
        plane.rotateX((float)-Math.PI / 2.0f);
        scale(0.5f);

        plane.build();
        plane.strip();

        // Centre the model (as calculated with painful trial and error)
        plane.setOrigin(startPosition);

        // get the rotation matrix for the current position
        frontMatrix = new Matrix(plane.getRotationMatrix());
        // removes scale from the rotation matrix
        frontMatrix.orthonormalize();

        for(int i = 0; i < 6; i++)
            segmentRotations[i] = new Matrix(frontMatrix.cloneMatrix());

        segmentRotations[1].rotateY((float) Math.PI);
        segmentRotations[1].rotateX((float) -Math.PI / 4.0f);
        segmentRotations[2].rotateY((float) Math.PI);
        segmentRotations[3].rotateY((float) -Math.PI / 2.0f);
        segmentRotations[4].rotateY((float) Math.PI);
        segmentRotations[4].rotateX((float) Math.PI / 4.0f);
        segmentRotations[5].rotateX((float) Math.PI / 2.0f);

        isLoaded=true;
    }

    public static void setCamera(Camera c)
    {
        cam = c;
        if(buf == null)
            return;

        for(int i=0; i<spheres.length; i++)
        {
            SimpleVector vec = Interact2D.project3D2D(cam, buf, spheres[i].getTransformedCenter());
            vec.y = buf.getHeight() - vec.y;
            shads[i].setUniform("spherePos", vec);
        }
    }

    public static void setFrameBuffer(FrameBuffer b)
    {
        buf = b;
        if(cam == null)
            return;

        for(int i=0; i<spheres.length; i++)
        {
            SimpleVector vec = Interact2D.project3D2D(cam, buf, spheres[i].getTransformedCenter());
            vec.y = buf.getHeight() - vec.y;
            shads[i].setUniform("spherePos", vec);
        }
    }

    public static void setLabelsToDisplay(boolean x)
    {
        if(spheres==null)
            return;

        for (Object3D sphere : spheres)
        {
            if(x && !sphere.hasParent(plane))
                plane.addChild(sphere);
            else if(!x && plane.hasChild(sphere))
                plane.removeChild(sphere);
        }
    }

    private static boolean isVisibilityHodgePodge(Object3D sphere)
    {
        float hodgeFactor = 1.0f;

        SimpleVector bvec = plane.getTransformedCenter();
        SimpleVector cvec = cam.getPosition();

        float dx, dy, dz;
        dx = bvec.x - cvec.x;
        dy = bvec.y - cvec.y;
        dz = bvec.z - cvec.z;

        ///Not really a Z value but nevermind
        float brainZFromCamera = (float)Math.sqrt(dx*dx + dy*dy + dz*dz);

        SimpleVector svec = sphere.getTransformedCenter();
        float sx, sy, sz;

        sx = svec.x - cvec.x;
        sy = svec.y - cvec.y;
        sz = svec.z - cvec.z;

        float sphereZFromCamera = (float)Math.sqrt(sx*sx + sy*sy + sz*sz);

        return sphereZFromCamera < brainZFromCamera*hodgeFactor;
    }

    public static void notifyTap(float x, float y)
    {
        boolean selected = false;
        int i = 0;
        int pos = -1;

        if(cam == null || buf == null || spheres == null)
            return;

        for(i = 0; i<spheres.length; i++)
        {
            SimpleVector v = Interact2D.project3D2D(cam, buf, spheres[i].getTransformedCenter());

            float xd = v.x - x;
            float yd = v.y - y;

            float dist = (float) Math.sqrt(xd * xd + yd * yd);

            if(dist < sphereRad*30.0f && isVisibilityHodgePodge(spheres[i]))
            {
                if(selection == i)
                {
                    Labels.removeLabels();
                    selection = -1;
                    shads[i].setUniform("isSelected", 0);
                    break;
                }

                selection = i;
                Labels.displayLabel(brainSegments[i]);
                smoothRotateToGeneric(segmentRotations[i], false);
                Log.d("Rotations", "brainSegments[i]: " + plane.getRotationMatrix().toString());

                selected = true;
                pos = i;
                break;
            }
            else
            {
                shads[i].setUniform("isSelected", 0);
                Labels.removeLabels();
            }
        }

        i++;

        for(; i<spheres.length; i++)
        {
            shads[i].setUniform("isSelected", 0);
        }
        if(selected)
        {
            shads[pos].setUniform("isSelected", 1);
            smoothMoveToGeneric(sidePosition, 0);
            smoothZoom(0.3f,400);
        }
        else
        {
            smoothMoveToGeneric(startPosition, 0);
        }
    }


    public static void addToScene(World world)
    {
        world.addObject(plane);
        world.addObjects(objs);
        world.addObjects(spheres);
    }

    public static SimpleVector getTransformedCenter()
    {
        return plane.getTransformedCenter();
    }

    public static void rotate(float x, float y, float z)
    {
        try
        {
            brainSemaphore.acquire();
        }
        catch(InterruptedException e)
        {
            return;
        }

        plane.rotateX(x);
        plane.rotateY(y);
        plane.rotateZ(z);

        for(int i=0; i<spheres.length; i++)
        {
            if(cam!=null && buf!=null)
            {
                SimpleVector vec = Interact2D.project3D2D(cam, buf, spheres[i].getTransformedCenter());
                vec.y = buf.getHeight() - vec.y;
                shads[i].setUniform("spherePos", vec);
            }
        }

        brainSemaphore.release();
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
        try
        {
            brainSemaphore.acquire();
        }
        catch(InterruptedException e)
        {
            return;
        }

        if(scale <= 0)
        {
            brainSemaphore.release();
            return;
        }

        plane.scale(scale);
        shader.setUniform("heightScale", 1.0f);
        for(int i=0; i<spheres.length; i++)
        {
            spheres[i].scale(1.0f/scale);
        }

        brainSemaphore.release();
    }

    public static void smoothMoveToGeneric(SimpleVector simpleVector, int delay)
    {
        Log.d("BrainSlice", "smoothMoveToGeneric");
        SimpleVector currentPosition = plane.getTransformedCenter();

        final float xDiff = simpleVector.x - currentPosition.x;
        final float yDiff = simpleVector.y - currentPosition.y;
        final float zDiff = simpleVector.z - currentPosition.z;

        final int time = 400;
        Timer moveTimer = new Timer();
        moveTimer.schedule(new TimerTask() {
            // time in ms for each step
            final int stepTime = 15;
            // current number of milliseconds elapsed
            int i = stepTime;

            Ease xEase = new Ease(xDiff,time, Ease.Easing.OUT_EXPO);
            Ease yEase = new Ease(yDiff,time, Ease.Easing.OUT_EXPO);
            Ease zEase = new Ease(zDiff,time, Ease.Easing.OUT_EXPO);

            @Override
            public void run()
            {
                float stepMovementX = (float) (xEase.step(i) - xEase.step(i-stepTime));
                float stepMovementY = (float) (yEase.step(i) - yEase.step(i-stepTime));
                float stepMovementZ = (float) (zEase.step(i) - zEase.step(i - stepTime));

                try
                {
                    brainSemaphore.acquire();
                } catch (InterruptedException e)
                {
                    return;
                }

                plane.translate(stepMovementX, stepMovementY, stepMovementZ);

                brainSemaphore.release();

                i += stepTime;
                if (i >= time) cancel();
            }

        }, delay, 15);
    }

    public static void smoothZoom(final float targetScale, final int zoomTime)
    {
        if (!isLoaded)
            return;
        Timer zoomTimer = new Timer();
        zoomTimer.schedule(new TimerTask() {
            final double scaleDiff = targetScale - getScale();
            // time in ms for each step
            final int stepTime = 15;
            // current number of milliseconds elapsed
            int i = stepTime;

            Ease ease = new Ease(scaleDiff,zoomTime, Ease.Easing.OUT_EXPO);
            @Override
            public void run() {
                double stepZoom = (ease.step(i) - ease.step(i-stepTime) + getScale()) / getScale();

                if(Double.isNaN(stepZoom))
                    return;

                scale((float) stepZoom);
                i += stepTime;
                if (i >= zoomTime) cancel();
            }
        },100,15);
    }

    public static void smoothRotateToGeneric(Matrix targetMatrix, final Boolean elasticBounce){
        Log.d("BrainSlice", "smoothRotateToGeneric");

        if (!isLoaded)
            return;

        double e1, e2, e3;

        Matrix r = plane.getRotationMatrix().cloneMatrix().invert3x3();
        targetMatrix.orthonormalize();
        r.orthonormalize();
        r.matMul(targetMatrix);

        //Log.d("BrainSlice", "front matrix: " + targetMatrix.toString());
        //Log.d("BrainSlice", "rotation matrix: " + r.toString());

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




        final int rotateTime = 300 + (int) Math.round(Math.abs(angle)*350.0f);
        Timer rotateTimer = new Timer();
        rotateTimer.schedule(new TimerTask() {
            // time in ms for each step
            final int stepTime = 15;
            // current number of milliseconds elapsed
            int i = stepTime;

            Ease ease = new Ease(angle,rotateTime, Ease.Easing.OUT_ELASTIC);

            @Override
            public void run()
            {
                // calculate the next rotation step to move by
                double stepRotation = ease.step(i) - ease.step(i-stepTime);


                if(Double.isNaN(stepRotation))
                    return;

                if(Double.isInfinite(stepRotation))
                    return;

                try
                {
                    brainSemaphore.acquire();
                }
                catch(InterruptedException e)
                {
                    return;
                }

                // This is 100% necessary to prevent a bug where the brain model disappears
                if(stepRotation != 0.0)
                    plane.rotateAxis(axis, (float) stepRotation);

                brainSemaphore.release();

                i += stepTime;
                if (i >= rotateTime) cancel();
            }

        }, 0, 15);
    }

    public static float getScale()
    {
        return plane.getScale();
    }

    static class Ease {

        private double delta;
        private double totalTime;
        private Easing type;

        public enum Easing {
            OUT_EXPO, IN_OUT_EXPO, OUT_ELASTIC, OUT_BACK
        }

        Ease(double delta, double totalTime, Easing type) {
            this.delta = delta;
            this.totalTime = totalTime;
            this.type = type;
        }

        public double step(double currentTime){
            switch (type){
                case OUT_EXPO:
                    return easeOutExpo(delta, currentTime, totalTime);
                case IN_OUT_EXPO:
                    return easeInOutExpo(delta, currentTime, totalTime);
                case OUT_ELASTIC:
                    return easeOutElastic(delta,currentTime,totalTime);
                case OUT_BACK:
                    return easeOutBack(delta,currentTime,totalTime);
            }
            return 0;
        }


        // adapted from http://easings.net/
        private static double easeOutExpo(double delta, double currentTime, double totalTime)
        {
            if (currentTime == totalTime) return delta;
            return delta * (-Math.pow(2, -10 * currentTime/totalTime) + 1);
        }

        // adapted from http://easings.net/
        private static double easeInOutExpo(double delta, double currentTime, double totalTime)
        {
            if ((currentTime/=totalTime/2) < 1)
                return delta/2 * Math.pow(2, 10 * (currentTime - 1)) + 1;

            return delta/2 * (-Math.pow(2, -10 * --currentTime) + 2) + 1;
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
}
