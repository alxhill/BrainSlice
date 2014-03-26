package com.lemonslice.brainslice;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.util.Log;

import com.lemonslice.brainslice.event.Event;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.GLSLShader;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.Interact2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.media.MediaPlayer;
import android.media.AudioManager;
import android.view.KeyEvent;

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
    private static Object3D[] subCortical;
    private static GLSLShader shader = null;

    private static Semaphore brainSemaphore = new Semaphore(1);

    private static Matrix frontMatrix;

    private static ArrayList<Object3D> spheres = new ArrayList<Object3D>();
    public static boolean spheresLoaded = false;
//    private static RGBColor sphereNormalColor = new RGBColor(255, 255, 255);
//    private static RGBColor sphereTouchedColor = new RGBColor(255, 255, 0);

    private static Camera cam = null;


    private static FrameBuffer buf = null;
    private static float sphereRad = 2.0f;

    public static boolean isLoaded = false;

    private static int selection = -1;

    private static GLSLShader[] shads = new GLSLShader[0];

    private static SimpleVector sidePosition = SimpleVector.create(-25,20,10);
    public static SimpleVector startPosition = SimpleVector.create(0,20,10);
    public static SimpleVector offScreenRightPosition = SimpleVector.create(50,20,10);

    private static GLSLShader brainShad;
    private static GLSLShader shineyShader;

    private static SimpleVector camPos;
    private static MediaPlayer speak = new MediaPlayer();

    private static Context context;
    private static AudioManager audioManager;

    private static boolean shouldDisplaySpheres = true;

    public static void load(Resources res, AudioManager audio, Context con)
    {
        context = con;

        audioManager = audio;

        shader = new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.vertexshader_offset)),
                                Loader.loadTextFile(res.openRawResource(R.raw.fragmentshader_spheres)));

        brainShad = new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.vertexshader_brain)),
                                Loader.loadTextFile(res.openRawResource(R.raw.fragmentshader_brain)));

        shineyShader = new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.vertexshader_brain_shiny)),
                                Loader.loadTextFile(res.openRawResource(R.raw.fragmentshader_brain_shiny)));

        shineyShader.setUniform("cameraPos", SimpleVector.create(0,0,0));

        // brain is parented to small plane
        plane = Primitives.getPlane(1, 1);

        plane.setCulling(true);

        // Load the 3d model
        Log.d("BrainSlice", "Loading .3ds file");

        objs = Loader.loadSerializedObjectArray(res.openRawResource(R.raw.new_ser));
        Log.d("BrainSlice", "Loaded .3ds file");

        subCortical = Loader.loadSerializedObjectArray(res.openRawResource(R.raw.underbrain));

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
            obj.setShader(shineyShader);
        }

        for(Object3D obj : subCortical)
        {
            obj.setCulling(true);
            obj.setSpecularLighting(false); //was true
            obj.build();
            obj.compile();
            obj.strip();
            obj.addParent(plane);
            obj.setShader(shineyShader);
        }

        setXRayMode(true);

        objs[0].setVisibility(false);
        objs[1].setVisibility(false);

        brainShad.setUniform("transparent", 1);

        // Set the model's initial position
        plane.rotateY((float) Math.PI);
        plane.rotateX((float) -Math.PI / 2.0f);
        plane.rotateZ((float) Math.PI);
        scale(0.5f);

        plane.build();
        plane.strip();

        // Centre the model (as calculated with painful trial and error)
        plane.setOrigin(startPosition);

        // get the rotation matrix for the current position
        frontMatrix = new Matrix(plane.getRotationMatrix());
        // removes scale from the rotation matrix
        frontMatrix.orthonormalize();

        isLoaded = true;
        Event.trigger("model:loaded");
    }

    public static void loadSegments(Resources res)
    {
        Log.d("BrainSlice", "loading segments");
        HashMap<String, BrainSegment> segments = BrainInfo.getSegments();
        spheres = new ArrayList<Object3D>(segments.size());

        for (BrainSegment segment : segments.values())
        {
            if (segment.getPosition() == null)
                continue;

            Object3D sphere = Primitives.getSphere(sphereRad);
            sphere.addParent(plane);
            sphere.setLighting(Object3D.LIGHTING_NO_LIGHTS);
            sphere.build();
            sphere.compile();
            sphere.strip();
            sphere.setName(segment.getName());
            sphere.translate(segment.getPosition());
            sphere.scale(2.0f);

            spheres.add(sphere);
        }

        shads = new GLSLShader[spheres.size()];

        for (int i = 0; i < spheres.size(); i++)
        {
            shads[i] = new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.vertexshader_offset)),
                    Loader.loadTextFile(res.openRawResource(R.raw.fragmentshader_spheres)));

            shads[i].setUniform("isSelected", 0);

            spheres.get(i).setShader(shads[i]);
        }

        spheresLoaded = true;
    }

    public static void setXRayMode(boolean xRayMode)
    {
        if (xRayMode) {
            objs[2].setShader(brainShad);
            subCortical[4].setShader(brainShad);
        } else {
            objs[2].setShader(shineyShader);
            subCortical[4].setShader(shineyShader);
        }
    }

    public static ArrayList<Object3D> getSpheres()
    {
        return spheres;
    }

    public static void setCamera(Camera c)
    {
        cam = c;
        if(buf == null)
            return;

        if (!spheresLoaded) return;

        for(int i=0; i<spheres.size(); i++)
        {
            SimpleVector vec = Interact2D.project3D2D(cam, buf, spheres.get(i).getTransformedCenter());
            vec.y = buf.getHeight() - vec.y;
            shads[i].setUniform("spherePos", vec);
        }
    }

    public static void updateCameraPos()
    {
        shineyShader.setUniform("cameraPos", cam.getPosition());
    }

    public static Camera getCamera()
    {
        return cam;
    }

    public static void setFrameBuffer(FrameBuffer b)
    {
        buf = b;
        if(cam == null)
            return;

        if (!spheresLoaded) return;

        for(int i=0; i<spheres.size(); i++)
        {
            SimpleVector vec = Interact2D.project3D2D(cam, buf, spheres.get(i).getTransformedCenter());
            vec.y = buf.getHeight() - vec.y;
            shads[i].setUniform("spherePos", vec);
        }
    }

    public static void setLabelsToDisplay(boolean x)
    {
        shouldDisplaySpheres = x;
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

        if (camPos == null)
        {
            // get a vector pointing directly to the camera
            camPos = cam.getDirection();
            camPos = camPos.reflect(camPos);
        }

        for(i = 0; i<spheres.size(); i++)
        {
            Object3D sphere = spheres.get(i);

            SimpleVector v = Interact2D.project3D2D(cam, buf, sphere.getTransformedCenter());

            float xd = v.x - x;
            float yd = v.y - y;

            float dist = (float) Math.sqrt(xd * xd + yd * yd);

            if(dist < sphereRad*26.0f && isVisibilityHodgePodge(sphere))
            {
                int audioID;
                String name = sphere.getName();

                if(selection == i)
                {
                    Labels.removeLabels();
                    selection = -1;
                    shads[i].setUniform("isSelected", 0);
                }

                selection = i;
                Labels.displayLabel(name);

                if(name.equals("Brainstem"))
                    audioID = R.raw.brain_stem;
                else if(name.equals("Temporal lobe"))
                    audioID = R.raw.temporal_lobe;
                else if(name.equals("Parietal lobe"))
                    audioID = R.raw.parietal_lobe;
                else if(name.equals("Occipital lobe"))
                    audioID = R.raw.occipital_lobe;
                else if(name.equals("Frontal lobe"))
                    audioID = R.raw.frontal_lobe;
                else if(name.equals("Cerebellum"))
                    audioID = R.raw.cerebellum;
                else
                    audioID = R.raw.brain_stem;

                speak.stop();
                speak.release();
                speak = MediaPlayer.create(context, audioID);
                speak.start();

                SimpleVector spherePos = sphere.getTransformedCenter();
                // ignore the translation of the plane when calculating rotation
                spherePos = spherePos.calcSub(plane.getTransformedCenter());

                // convert the vectors into axis-angle representation
                SimpleVector axis = spherePos.calcCross(camPos);
                double angle = spherePos.calcAngle(camPos);

                smoothRotateToGeneric(axis, -angle, false);

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

        for(; i<spheres.size(); i++)
        {
            shads[i].setUniform("isSelected", 0);
        }

        if(selected)
        {
            shads[pos].setUniform("isSelected", 1);
            smoothMoveToGeneric(sidePosition, 0, 400);
            smoothZoom(0.25f,400);
        }
        else
        {
            smoothMoveToGeneric(startPosition, 0, 400);
        }
    }


    public static void addToScene(World world)
    {
        if(plane == null)
            return;

        world.addObject(plane);

        if(objs == null)
            return;

        for(int i=0; i<objs.length; i++)
        {
            if(i == 2)
                continue;
            world.addObject(objs[i]);
        }

        if(subCortical == null)
            return;

        for(int i=0; i<subCortical.length; i++)
        {
            if(i == 4)
                continue;
            world.addObject(subCortical[i]);
        }

        if(spheres == null)
            return;

        if(shouldDisplaySpheres)
        {
            for (Object3D sphere : spheres)
                world.addObject(sphere);
        }
    }

    public static void addToTransp(World world)
    {
        if(subCortical == null || objs == null)
            return;

        world.addObject(subCortical[4]);
        world.addObject(objs[2]);
    }

    public static void removeAll(World world)
    {
        world.removeAll();
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

        if (!spheresLoaded) return;

        // updates the sphere shaders so the gradient is drawn in the right location
        for(int i=0; i<spheres.size(); i++)
        {
            if(cam!=null && buf!=null)
            {
                SimpleVector vec = Interact2D.project3D2D(cam, buf, spheres.get(i).getTransformedCenter());

                if(vec == null)
                    continue;

                vec.y = buf.getHeight() - vec.y;
                //GOT A NULL POINTER HERE ^, ON THIS LINE ABOVE WHEN TAPPING A PRETTY BUTTON
                //ASSUMING IT WAS buf.getHeight(); ... Berrow?
                ///fixed another nullptr expection as well. Double whoops

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
        for(int i=0; i<spheres.size(); i++)
        {
            spheres.get(i).scale(1.0f / scale);
        }

        brainSemaphore.release();
    }

    public static void smoothMoveToGeneric(SimpleVector simpleVector, int delay, final int duration)
    {
        Log.d("BrainSlice", "smoothMoveToGeneric");

        if (!isLoaded)
            return;

        SimpleVector currentPosition = plane.getTransformedCenter();

        final float xDiff = simpleVector.x - currentPosition.x;
        final float yDiff = simpleVector.y - currentPosition.y;
        final float zDiff = simpleVector.z - currentPosition.z;

        Timer moveTimer = new Timer();
        moveTimer.schedule(new TimerTask() {
            // time in ms for each step
            final int stepTime = 15;
            // current number of milliseconds elapsed
            int i = stepTime;

            Ease xEase = new Ease(xDiff, duration, Ease.Easing.OUT_EXPO);
            Ease yEase = new Ease(yDiff, duration, Ease.Easing.OUT_EXPO);
            Ease zEase = new Ease(zDiff, duration, Ease.Easing.OUT_EXPO);

            @Override
            public void run()
            {
                float stepMovementX = (float) (xEase.step(i) - xEase.step(i - stepTime));
                float stepMovementY = (float) (yEase.step(i) - yEase.step(i - stepTime));
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
                if (i >= duration) cancel();
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

            Ease ease = new Ease(scaleDiff, zoomTime, Ease.Easing.OUT_EXPO);

            @Override
            public void run()
            {
                double stepZoom = (ease.step(i) - ease.step(i - stepTime) + getScale()) / getScale();

                if (Double.isNaN(stepZoom))
                    return;

                scale((float) stepZoom);
                i += stepTime;
                if (i >= zoomTime) cancel();
            }
        }, 100, 15);
    }

    public static void smoothRotateToGeneric(Matrix targetMatrix, final boolean elasticBounce)
    {
        Log.d("BrainSlice", String.format("smoothRotateToGeneric" + isLoaded));

        if (!isLoaded)
            return;

        double e1, e2, e3;
        Matrix r = plane.getRotationMatrix().cloneMatrix().invert3x3();
        Matrix target = targetMatrix.cloneMatrix();
        target.orthonormalize();
        r.orthonormalize();
        r.matMul(target);

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
        smoothRotateToGeneric(axis, angle, elasticBounce);
    }

    public static void smoothRotateToGeneric(final SimpleVector axis, final double angle, final boolean elasticBounce)
    {
        final int rotateTime = 300 + (int) Math.round(Math.abs(angle)*350.0f);

        final Ease ease;
        if (elasticBounce) {
            ease = new Ease(angle,rotateTime, Ease.Easing.OUT_ELASTIC);
        } else {
            ease = new Ease(angle,rotateTime, Ease.Easing.OUT_EXPO);
        }

        Timer rotateTimer = new Timer();
        rotateTimer.schedule(new TimerTask() {
            // time in ms for each step
            final int stepTime = 15;
            // current number of milliseconds elapsed
            int i = stepTime;

            @Override
            public void run() {
                // calculate the next rotation step to move by
                double stepRotation = ease.step(i) - ease.step(i - stepTime);


                if (Double.isNaN(stepRotation))
                    return;

                if (Double.isInfinite(stepRotation))
                    return;

                try {
                    brainSemaphore.acquire();
                } catch (InterruptedException e) {
                    return;
                }

                // This is 100% necessary to prevent a bug where the brain model disappears
                if (stepRotation != 0.0)
                    plane.rotateAxis(axis, (float) stepRotation);

                brainSemaphore.release();

                i += stepTime;
                if (i >= rotateTime) cancel();
            }

        }, 0, 15);
    }

    public static void smoothRotateToFront()
    {
        smoothRotateToGeneric(frontMatrix, true);
    }

    public static float getScale()
    {
        return plane.getScale();
    }

    public static void onVolumeKey(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
        }

    }

    public static float[] getPosition()
    {
        Matrix rotationMatrix = plane.getRotationMatrix().cloneMatrix();
        rotationMatrix.orthonormalize();
        float[] orientation = new float[3];
        SensorManager.getOrientation(rotationMatrix.getDump(), orientation);
        return orientation;
    }

    public static Matrix getRotationMatrix()
    {
        return plane.getRotationMatrix();
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

        public double step(double currentTime) {
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
