package com.lemonslice.brainslice;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.lemonslice.brainslice.event.Events;
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

import java.util.concurrent.Semaphore;
import java.util.Random;

/**
 * Renders the brain to the screen and handles moving the model
 * Created by alexander on 27/01/2014.
 */
public class BrainModel {

    // 3D stuff
    private static Object3D plane;
    private static Object3D glowPlane;
    private static Object3D[] objs;
    private static Object3D[] subCortical;

    // Shaders
    private static GLSLShader shader = null;
    private static GLSLShader[] shads;
    private static GLSLShader brainShad;
    private static GLSLShader shineyShader;
    private static GLSLShader glowShader;

    // camera
    private static Camera camera = null;
    private static FrameBuffer buf = null;


    // Spheres
    private static boolean shouldDisplaySpheres = true;
    private static final float sphereRad = 2.0f;
    private static ArrayList<Object3D> spheres = new ArrayList<Object3D>();
    public static boolean spheresLoaded = false;
//    private static RGBColor sphereNormalColor = new RGBColor(255, 255, 255);
//    private static RGBColor sphereTouchedColor = new RGBColor(255, 255, 0);


    // Positions
    public static SimpleVector sidePosition = SimpleVector.create(-22,0,0);
    //public static SimpleVector homePosition = SimpleVector.create(-30,-5,0); // Delete this
    public static SimpleVector startPosition = SimpleVector.create(0,0,0);
    private static SimpleVector camPos;


    // Misc
    private static Context context;
    private static final Semaphore brainSemaphore = new Semaphore(1);

    public static boolean isLoaded = false;
    private static int selection = -1;
    private static Matrix frontMatrix;

    static boolean showBrain = false;
    static boolean onlyRotateY = true;
    static boolean disableDoubleTap = true;
    static boolean infoShowing = false;
    private static int screenWidth, screenHeight;

    public static Boolean drawBackground;

    public static void load(Resources res, Context con)
    {
        context = con;

        shader = new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.vertexshader_offset)),
                                Loader.loadTextFile(res.openRawResource(R.raw.fragmentshader_spheres)));

        brainShad = new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.vertexshader_brain)),
                                Loader.loadTextFile(res.openRawResource(R.raw.fragmentshader_brain)));

        shineyShader = new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.vertexshader_brain_shiny)),
                                Loader.loadTextFile(res.openRawResource(R.raw.fragmentshader_brain_shiny)));

        glowShader =  new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.glowshader_vert)),
                                Loader.loadTextFile(res.openRawResource(R.raw.glowshader_frag)));



        shineyShader.setUniform("cameraPos", SimpleVector.create(0,0,0));

        // brain is parented to small plane
        plane = Primitives.getPlane(1, 1);
        glowPlane = Primitives.getPlane(1, 500.0f);

        plane.setCulling(true);

        // Load the 3d model
        objs = Loader.loadSerializedObjectArray(res.openRawResource(R.raw.fullmodel2));
        Log.d("BrainSlice", "Copied serialised brain model into memory");
        subCortical = Loader.loadSerializedObjectArray(res.openRawResource(R.raw.subcor2));
        Log.d("BrainSlice", "Copied serialised subcortical sections into memory");

        // compile and load shaders for plane
        plane.setShader(shader);
        plane.setSpecularLighting(false);
        shader.setStaticUniform("invRadius", 0.0003f);

        Random randomNumberGenerator = new Random();

        // initialise brain sub-objs

            /*Object array indices:
            0: right parietal
            1: right frontal
            2: right cerebellum cortex (hidden)
            3: right extra
            4: left temporal
            5: left occipital
            6: left parietal
            7: left extra
            8: brain stem
            9: left frontal
            10: right temporal
            11: right occipital
            12: left cerebellum cortex (hidden)

         */

        /* Sub-cortical indices
        0: Left Thalamus
        1: Left Hippocampus
        2: Left Cerebellum Cortex
        3: Brain Stem
        4: Right Cerebellum cortex
        5: Left Amygdala
        6: Left Cerebellum white matter
        7: Right cerebellum white matter

            */

        for (Object3D obj : objs)
        {
            obj.setCulling(true);
            obj.setSpecularLighting(false); //was true
            obj.build();
            obj.compile();
            obj.strip();
            obj.addParent(plane);
            obj.setShader(shineyShader);

            double amount = (randomNumberGenerator.nextGaussian()/20.0) + 1.0;

            if(amount < 0.90)
                amount = 0.90;

            if(amount > 1.10)
                amount = 1.10;

            obj.setScale((float) amount);
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

            double amount = (randomNumberGenerator.nextGaussian()/20.0) + 1.0;

            if(amount < 0.90)
                amount = 0.90;

            if(amount > 1.10)
                amount = 1.10;

            obj.setScale((float) amount);
        }

        setDisplayMode(true,false);

        objs[2].setVisibility(false);
        objs[12].setVisibility(false);

        brainShad.setUniform("transparent", 1);

        // Set the model's initial position
        plane.rotateX((float) Math.PI / 2.0f);
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

        glowPlane.setOrigin(SimpleVector.create(0, 0, 50));

        glowPlane.setShader(glowShader);

        glowShader.setUniform("sw", screenWidth);
        glowShader.setUniform("sh", screenHeight);

        enableBackgroundGlow();

        isLoaded=true;
        Events.trigger("model:loaded");
        Log.d("Brainslice","BrainModel isLoaded");
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

    public static void setDisplayMode(boolean xRayMode, boolean colourMode)
    {
        if(colourMode && xRayMode) {
            for (Object3D obj : subCortical)
            {
                obj.clearShader();
            }
            for (Object3D obj : objs)
            {
                obj.clearShader();
            }
            objs[4].setShader(brainShad);
            objs[5].setShader(brainShad);
            objs[6].setShader(brainShad);
            objs[7].setShader(brainShad);
            objs[9].setShader(brainShad);
            subCortical[2].setShader(brainShad);
        } else if (colourMode) {
            for (Object3D obj : objs)
            {
                obj.clearShader();
            }
            for (Object3D obj : subCortical)
            {
                obj.clearShader();
            }
        } else if (xRayMode) {
            for (Object3D obj : objs)
            {
                obj.setShader(shineyShader);
            }
            for (Object3D obj : subCortical)
            {
                obj.setShader(shineyShader);
            }
            objs[4].setShader(brainShad);
            objs[5].setShader(brainShad);
            objs[6].setShader(brainShad);
            objs[7].setShader(brainShad);
            objs[9].setShader(brainShad);
            subCortical[2].setShader(brainShad);
        } else {
            for (Object3D obj : objs)
            {
                obj.setShader(shineyShader);
            }
            for (Object3D obj : subCortical)
            {
                obj.setShader(shineyShader);
            }
        }

    }

    public static ArrayList<Object3D> getSpheres()
    {
        return spheres;
    }

    public static void setCamera(Camera c)
    {

        camera = c;
        if (camPos == null)
        {
            // get a vector pointing directly to the camera
            camPos = camera.getDirection();
            camPos = camPos.reflect(camPos);
        }

        if(buf == null)
            return;

        if (!spheresLoaded) return;

        for(int i=0; i<spheres.size(); i++)
        {
            SimpleVector vec = Interact2D.project3D2D(camera, buf, spheres.get(i).getTransformedCenter());
            vec.y = buf.getHeight() - vec.y;
            shads[i].setUniform("spherePos", vec);
        }
    }

    public static void updateCameraPos()
    {
        shineyShader.setUniform("cameraPos", camera.getPosition());
    }

    public static Camera getCamera()
    {
        return camera;
    }

    public static void setFrameBuffer(FrameBuffer b)
    {
        buf = b;
        if(camera == null)
            return;

        screenWidth = buf.getWidth();
        screenHeight = buf.getHeight();

        if (!spheresLoaded) return;

        for(int i=0; i<spheres.size(); i++)
        {
            SimpleVector vec = Interact2D.project3D2D(camera, buf, spheres.get(i).getTransformedCenter());
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
        float hodgeFactor = 1.1f;

        SimpleVector bvec = plane.getTransformedCenter();
        SimpleVector cvec = camera.getPosition();

        float dx, dy, dz;
        dx = bvec.x - cvec.x;
        dy = bvec.y - cvec.y;
        dz = bvec.z - cvec.z;

        ///Not really a Z value but never mind
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
        Log.d("BrainSlice","notifyTap");
        boolean selected = false;
        int i;
        int pos = -1;

        if(camera == null || buf == null || spheres == null)
            return;

        if (camPos == null)
        {
            // get a vector pointing directly to the camera
            camPos = camera.getDirection();
            camPos = camPos.reflect(camPos);
        }

        for(i = 0; i<spheres.size(); i++)
        {
            Object3D sphere = spheres.get(i);

            SimpleVector v = Interact2D.project3D2D(camera, buf, sphere.getTransformedCenter());

            if(v == null)
                continue;

            float xd = v.x - x;
            float yd = v.y - y;

            float dist = (float) Math.sqrt(xd * xd + yd * yd);

            if(dist < sphereRad*26.0f && isVisibilityHodgePodge(sphere))
            {
                String name = sphere.getName();

                if(selection == i)
                {
                    Labels.removeLabels();
                    selection = -1;
                    shads[i].setUniform("isSelected", 0);
                }

                selection = i;
                Labels.displayLabel(name);

                BrainInfo.getSegment(name).playAudio(context);

                rotateToSphere(sphere);

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
            if(!disableDoubleTap) {
                infoShowing = false;
                smoothMoveToGeneric(startPosition, 0, 400);
            }
        }
    }

    public static void infoTapped()
    {
        Log.d("BrainSlice","infoTapped");
        if(!infoShowing && !disableDoubleTap)
        {
            infoShowing = true;
            smoothMoveToGeneric(sidePosition, 0, 400);
            smoothZoom(0.25f,400);
        }
        else
        {
            if(!disableDoubleTap) {
                infoShowing = false;
                smoothMoveToGeneric(startPosition, 0, 400);
            }
        }
    }

    public static void rotateToSegment(String name)
    {
        for (Object3D sphere : spheres)
        {
            Log.d("SPHERENAME", sphere.getName());
            if (sphere.getName().equals(name))
            {
                rotateToSphere(sphere);
                return;
            }
        }
        Log.d("BRAINMODEL", "did not rotate to any segment :(");
    }

    private static void rotateToSphere(Object3D sphere)
    {
        SimpleVector spherePos = sphere.getTransformedCenter();
        // ignore the translation of the plane when calculating rotation
        spherePos = spherePos.calcSub(plane.getTransformedCenter());

        // convert the vectors into axis-angle representation
        SimpleVector axis = spherePos.calcCross(camPos);
        double angle = spherePos.calcAngle(camPos);

        smoothRotateToGeneric(axis, -angle, false);
    }

    public static void addToScene(World world)
    {
        if(plane == null || !showBrain)
            return;

        world.addObject(plane);

        if(objs == null)
            return;

        for(int i=0; i<objs.length; i++)
        {
            //if(i == 2)
            //    continue;
            if(i == 4 || i == 5 || i == 6 || i == 7 || i == 9)
                continue;

            world.addObject(objs[i]);
        }

        if(subCortical == null)
            return;

        for(int i=0; i<subCortical.length; i++)
        {
            //if(i == 4)
            //    continue;
            if(i == 2)
                continue;

            world.addObject(subCortical[i]);
        }


        if(drawBackground)
            world.addObject(glowPlane);

        if(plane == null || camera == null || buf == null)
            return;

        SimpleVector v = Interact2D.project3D2D(camera, buf, plane.getTransformedCenter());

        if(v == null)
            return;

        glowShader.setUniform("scale", plane.getScale());
        glowShader.setUniform("centre", v);
    }

    public static void addToTransp(World world)
    {
        if (subCortical == null || objs == null || !showBrain)
            return;

        if(spheres == null)
            return;

        if(shouldDisplaySpheres)
        {
            for (Object3D sphere : spheres)
                world.addObject(sphere);
        }

        world.addObject(subCortical[2]);
        world.addObject(objs[4]);
        world.addObject(objs[5]);
        world.addObject(objs[6]);
        world.addObject(objs[7]);
        world.addObject(objs[9]);
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
            if(!onlyRotateY) {
                plane.rotateX(x);
                plane.rotateZ(z);
            }
            plane.rotateY(y);

            updateSpheres();
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            brainSemaphore.release();
        }
    }

    public static void rotate(SimpleVector axis, float angle)
    {
        try
        {
            brainSemaphore.acquire();
            plane.rotateAxis(axis, angle);
            updateSpheres();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            brainSemaphore.release();
        }
    }

    public static void translate(float x, float y, float z)
    {
        try
        {
            brainSemaphore.acquire();
            plane.translate(x, y, z);
            updateSpheres();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            brainSemaphore.release();
        }

    }

    public static void scale(float scale)
    {
        if(scale <= 0)
        {
            Log.d("Brainslice","Error: Attempt to scale with factor below or equal to zero");
            return;
        }

        try
        {
            brainSemaphore.acquire();
            plane.scale(scale);
            shader.setUniform("heightScale", 1.0f);
            for (Object3D sphere : spheres) {
                sphere.scale(1.0f / scale);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            brainSemaphore.release();
        }

    }

    private static void updateSpheres()
    {
        // updates the sphere shaders so the gradient is drawn in the right location
        if (!spheresLoaded) return;

        for (int i = 0; i < spheres.size(); i++)
        {
            if(camera !=null && buf!=null)
            {
                SimpleVector vec = Interact2D.project3D2D(camera, buf, spheres.get(i).getTransformedCenter());

                if(vec == null)
                    continue;

                vec.y = buf.getHeight() - vec.y;

                shads[i].setUniform("spherePos", vec);
            }
        }
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

            final Ease xEase = new Ease(xDiff, duration, Ease.Easing.OUT_EXPO);
            final Ease yEase = new Ease(yDiff, duration, Ease.Easing.OUT_EXPO);
            final Ease zEase = new Ease(zDiff, duration, Ease.Easing.OUT_EXPO);

            @Override
            public void run() {
                float stepMovementX = (float) (xEase.step(i) - xEase.step(i - stepTime));
                float stepMovementY = (float) (yEase.step(i) - yEase.step(i - stepTime));
                float stepMovementZ = (float) (zEase.step(i) - zEase.step(i - stepTime));

                translate(stepMovementX, stepMovementY, stepMovementZ);

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

            final Ease ease = new Ease(scaleDiff, zoomTime, Ease.Easing.OUT_EXPO);

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
        if (!isLoaded)
            return;

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

                // This is 100% necessary to prevent a bug where the brain model disappears
                if (stepRotation != 0.0)
                    rotate(axis, (float) stepRotation);

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


    static class Ease {

        private final double delta;
        private final double totalTime;
        private final Easing type;

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

    public static void enableBackgroundGlow()
    {
        if(glowShader != null)
            glowShader.setUniform("is_back", 1);
    }

    public static void disableBackgroundGlow()
    {
        if(glowShader != null)
            glowShader.setUniform("is_back", 0);
    }
}
