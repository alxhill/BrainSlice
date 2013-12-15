package com.lemonslice.brainslice;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.AsyncTask;
import android.os.Bundle;

import java.lang.reflect.Field;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.GLSLShader;
import com.threed.jpct.ITextureEffect;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureInfo;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

/**
 * @author Based off JPCT HelloShader freely licenced example by EgonOlsen, heavily modified by LemonSlice
 */
public class MainActivity extends Activity implements OnScaleGestureListener, SensorEventListener {

    // Used to handle pause and resume...
    private static MainActivity master = null;

    // Sensor stuff
    private ScaleGestureDetector scaleDetector = null;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    // current gyro rotation
    public float axisX, axisY, axisZ;


    private boolean ifTouch = true;
    private boolean ifGyro = true;

    // current fingermove distance
    private float xpos1 = -1;
    private float ypos1 = -1;

    private int firstPointerID = -1;

    private float touchTurn = 0;
    private float touchTurnUp = 0;

    // scale size of brain
    private float scale = 1.0f;

    // 3D stuff
    private GLSurfaceView mGLView;
    private MyRenderer renderer = null;
    private FrameBuffer fb = null;
    private World world = null;
    private RGBColor back = new RGBColor(0, 0, 0);
    private Texture font = null;
    private Object3D plane;
    private GLSLShader shader = null;


    protected void onCreate(Bundle savedInstanceState) {
        Logger.log("onCreate");
        Logger.setLogLevel(Logger.LL_DEBUG);

        if (master != null) {
            copy(master);
        }

        super.onCreate(savedInstanceState);
        mGLView = new GLSurfaceView(getApplication());

        // Enable the OpenGL ES2.0 context
        mGLView.setEGLContextClientVersion(2);

        renderer = new MyRenderer();
        mGLView.setRenderer(renderer);
        setContentView(mGLView);

        scaleDetector = new ScaleGestureDetector(this.getApplicationContext(), this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        Logger.log("onPause");
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        Logger.log("onResume");
        super.onResume();
        mGLView.onResume();
    }

    @Override
    protected void onStop() {
        Logger.log("onStop");
        super.onStop();
    }

    private void copy(Object src) {
        try {
            Logger.log("Copying data from master Activity!");
            Field[] fs = src.getClass().getDeclaredFields();
            for (Field f : fs) {
                f.setAccessible(true);
                f.set(this, f.get(src));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public boolean onTouchEvent(MotionEvent me) {
        if (!ifTouch) return true;
        scaleDetector.onTouchEvent(me);
        if(scaleDetector.isInProgress()){
            return true;
        }
        int pointerIndex;

        switch (me.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Logger.log("ACTION_DOWN\t");
                xpos1 = me.getX();
                ypos1 = me.getY();
                firstPointerID = me.getPointerId(0);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Logger.log("ACTION_POINTER_DOWN\t");
                touchTurn = 0;
                touchTurnUp = 0;
                break;

            case MotionEvent.ACTION_UP:
                Logger.log("ACTION_UP\t");
                xpos1 = -1;
                ypos1 = -1;
                touchTurn = 0;
                touchTurnUp = 0;
                firstPointerID = -1;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                //Get index of pointer that was lifted up
                pointerIndex = me.getActionIndex();

                Logger.log("ACTION_POINTER_UP\t");
                if (me.getPointerId(pointerIndex) == firstPointerID) {
                    //Choose new firstPointer
                    int newPointerIndex;
                    if(pointerIndex == 0){
                        newPointerIndex = 1;
                    } else {
                        newPointerIndex = 0;
                    }
                    xpos1 = me.getX(newPointerIndex);
                    ypos1 = me.getY(newPointerIndex);
                    firstPointerID = me.getPointerId(newPointerIndex);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if(me.getPointerCount() == 1){
                    pointerIndex = me.findPointerIndex(firstPointerID);
                    Logger.log("ACTION_MOVE " + pointerIndex);
                    float xd = me.getX(pointerIndex) - xpos1;
                    float yd = me.getY(pointerIndex) - ypos1;
                    xpos1 = me.getX(pointerIndex);
                    ypos1 = me.getY(pointerIndex);
                    touchTurn = xd / -200f;
                    touchTurnUp = yd / -200f;
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                firstPointerID = -1;
                break;
        }

        try {
            //Thread.sleep(15);
        } catch (Exception e) {
            // No need for this...
        }

        return true;
    }

    public boolean onScale(ScaleGestureDetector detector) {
        float difference = detector.getCurrentSpan() - detector.getPreviousSpan();
        scale += 0.001f * difference;
        return true;
    }

    // must be implemented for onscale
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    // must be implemented for onscale
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    protected boolean isFullscreenOpaque() {
        return true;
    }


    public void onSensorChanged(SensorEvent event)
    {
        if (!ifGyro) return;
        axisX = event.values[0];
        axisY = event.values[1];
        axisZ = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // do nothing
    }

    class MyRenderer implements GLSurfaceView.Renderer {

        private int fps = 0;
        private int lfps = 0;

        private long time = System.currentTimeMillis();

        public MyRenderer() {
            Texture.defaultToMipmapping(true);
            Texture.defaultTo4bpp(true);
        }

        public void onSurfaceChanged(GL10 gl, int w, int h) {
            if (fb != null) {
                fb.dispose();
            }

            Resources res = getResources();

            fb = new FrameBuffer(w, h);

            if (master == null) {
                world = new World();

                TextureManager tm = TextureManager.getInstance();

                // brain is parented to small plane
                plane = Primitives.getPlane(1, 1);

                plane.setCulling(true);


                Log.d("BrainSlice","Loading .obj file");
                Object3D objs[] = Loader.load3DS(res.openRawResource(R.raw.brain), 10.0f);
                Log.d("BrainSlice","Loaded .obj file");

                //number of subobjs for brain
                int len = objs.length;

                font = new Texture(res.openRawResource(R.raw.numbers));
                font.setMipmap(false);

                // compile and load shaders for plane
                shader = new GLSLShader(Loader.loadTextFile(res.openRawResource(R.raw.vertexshader_offset)), Loader.loadTextFile(res.openRawResource(R.raw.fragmentshader_offset)));
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

                plane.rotateX(-3.141592f/2.0f);

                plane.build();
                plane.strip();

                plane.setOrigin(SimpleVector.create(0, 0, 10));

                world.addObject(plane);
                world.addObjects(objs);

                // create lights on opposite sides of brain

                Light light = new Light(world);
                light.enable();
                light.setIntensity(122, 80, 80);
                light.setPosition(SimpleVector.create(-10, 50, -100));

                world.setAmbientLight(61, 40, 40);

                // construct camera and move it into position
                Camera cam = world.getCamera();
                cam.moveCamera(Camera.CAMERA_MOVEOUT, 70);
                cam.lookAt(plane.getTransformedCenter());

                MemoryHelper.compact();

                world.compileAllObjects();

                if (master == null) {
                    Logger.log("Saving master Activity!");
                    master = MainActivity.this;
                }
            }
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Logger.log("onSurfaceCreated");
        }

        // initialise to current time
        long oldTime = System.currentTimeMillis();

        public void onDrawFrame(GL10 gl) {
            // rotate by finger movement
            plane.rotateX(touchTurnUp);
            plane.rotateY(touchTurn);

            touchTurnUp = 0.0f;
            touchTurn = 0.0f;

            plane.scale(scale);

            scale = 1.0f;

            // rotate by gyro movement
            float x, y, z;
            long newTime = System.currentTimeMillis();
            long timeDiff = newTime - oldTime;

            if(timeDiff == 0)
            {
                oldTime = newTime;
                return;
            }

            // time since last frame
            float fTimeDiff = (float)timeDiff;

            // Calculate the movement based on the time elapsed
            x = axisX * fTimeDiff/1000.0f;
            y = axisY * fTimeDiff/1000.0f;
            z = axisZ *-fTimeDiff/1000.0f;

            oldTime = newTime;

            // rotate brain by gyro
            plane.rotateX(x);
            plane.rotateY(y);
            plane.rotateZ(z);

            shader.setUniform("heightScale", scale);

            // clear buffers and draw framerate
            fb.clear(back);
            world.renderScene(fb);
            world.draw(fb);
            blitNumber(lfps, 5, 5);
            fb.display();

            // calculate framerate
            if (System.currentTimeMillis() - time >= 1000) {
                lfps = fps;
                fps = 0;
                time = System.currentTimeMillis();
            }
            fps++;
        }

        // display a number from bitmap font file
        private void blitNumber(int number, int x, int y) {
            if (font != null) {
                String sNum = Integer.toString(number);

                for (int i = 0; i < sNum.length(); i++) {
                    char cNum = sNum.charAt(i);
                    int iNum = cNum - 48;
                    fb.blit(font, iNum * 5, 0, x, y, 5, 9, 5, 9, 10, true, null);
                    x += 5;
                }
            }
        }
    }

}
