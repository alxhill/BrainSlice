package com.lemonslice.brainslice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Based off JPCT HelloShader freely licenced example by EgonOlsen, heavily modified by LemonSlice
 */
public class MainActivity extends Activity {

    // Used to handle pause and resume...
    private static MainActivity master = null;

    AbstractController baseController;
    LearnController learnController;
    VisualiseController visualiseController;

    // 3D stuff
    private GLSurfaceView mGLView;
    private MyRenderer renderer = null;
    private FrameBuffer fb = null;
    private World world = null;
    private RGBColor back = new RGBColor(0, 17, 34);

    // Frame overlaying 3d rendering for labels, instructions etc...
    private FrameLayout overlayingFrame;

    protected void onCreate(Bundle savedInstanceState)
    {
        Logger.log("onCreate");
        Logger.setLogLevel(Logger.LL_DEBUG);

        if (master != null)
            copy(master);

        setContentView(R.layout.activity_main);

        overlayingFrame = (FrameLayout)findViewById(R.id.overlay_layout);
        hideSystemBars();

        renderer = new MyRenderer();

        LoadingScreen.setContext(this);
        LoadingScreen.setFrameLayout(overlayingFrame);
        LoadingScreen.setRenderer(renderer);
        LoadingScreen.showLoadingScreen();

        Labels.setContext(this);
        Labels.setFrameLayout(overlayingFrame);
        OverlayScreen.setContext(this);
        OverlayScreen.setFrameLayout(overlayingFrame);

        super.onCreate(savedInstanceState);
        mGLView = (GLSurfaceView)findViewById(R.id.openGlView);

        // Enable the OpenGL ES2.0 context
        mGLView.setEGLContextClientVersion(2);

        // initialise and show the 3D renderer
        mGLView.setRenderer(renderer);

        learnController = new LearnController(getApplicationContext());
        learnController.loadView();
        baseController = learnController;

        visualiseController = new VisualiseController((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        OverlayScreen.setVisualiseController(visualiseController);

        LinearLayout learnButton = (LinearLayout) findViewById(R.id.learn_button);
        learnButton.setOnClickListener(new FrameLayout.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                visualiseController.unloadView();
                learnController.loadView();
                baseController = learnController;
            }
        });


        LinearLayout visualiseButton = (LinearLayout) findViewById(R.id.visualise_button);
        visualiseButton.setOnClickListener(new FrameLayout.OnClickListener() {
            @Override
            public void onClick(View v)
            {
//                ((FrameLayout)(findViewById(R.id.overlay_layout))).removeAllViews();
                OverlayScreen.showScreen(R.layout.calibrate_screen);
                learnController.unloadView();
                baseController = visualiseController;
            }
        });

        LinearLayout centreButton = (LinearLayout) findViewById(R.id.centre_button);
        centreButton.setOnClickListener(new FrameLayout.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                baseController.stop();
                BrainModel.smoothRotateToFront();
            }
        });

        Typeface fontAwesome = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");

        TextView learnIcon = (TextView) learnButton.getChildAt(0);
        assert learnIcon != null;
        learnIcon.setTypeface(fontAwesome);
        learnIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        TextView visIcon = (TextView) visualiseButton.getChildAt(0);
        assert visIcon != null;
        visIcon.setTypeface(fontAwesome);
        visIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        TextView cenIcon = (TextView) centreButton.getChildAt(0);
        assert cenIcon != null;
        cenIcon.setTypeface(fontAwesome);
        cenIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

    }



    @Override
    protected void onPause()
    {
        Logger.log("onPause");
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume()
    {
        Logger.log("onResume");
        super.onResume();
        mGLView.onResume();
    }

    @Override
    protected void onStop()
    {
        Logger.log("onStop");
        super.onStop();
    }

    private void copy(Object src)
    {
        try
        {
            Logger.log("Copying data from master Activity!");
            Field[] fs = src.getClass().getDeclaredFields();
            for (Field f : fs)
            {
                f.setAccessible(true);
                f.set(this, f.get(src));
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    // activities are sent touch events,
    public boolean onTouchEvent(MotionEvent me)
    {
        return baseController.touchEvent(me);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        // Immersive mode is only supported in Android KitKat and above
            super.onWindowFocusChanged(hasFocus);
            if (hasFocus)
                hideSystemBars();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void hideSystemBars()
    {
        //API 14 = android 4.0 (ICS), API 19 = 4.4 (Kitkat)
        int uiOptions;
        if (Build.VERSION.SDK_INT >= 19)
        {

            uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
        else if(Build.VERSION.SDK_INT >= 14)
        {
            uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }

    }


    class MyRenderer implements GLSurfaceView.Renderer {
        private boolean isLoaded = false;

        public MyRenderer()
        {
            Texture.defaultToMipmapping(true);
            Texture.defaultTo4bpp(true);
        }

        public boolean isLoaded()
        {
            return isLoaded;
        }

        public void onSurfaceChanged(GL10 gl, int w, int h)
        {
            if (fb != null)
                fb.dispose();

            Resources res = getResources();

            fb = new FrameBuffer(w, h);

            Log.d("BrainSlice", "New FB created");

            BrainModel.setFrameBuffer(fb);

            if (master == null)
            {
                world = new World();

                BrainModel.load(res, getApplicationContext());

                BrainModel.addToScene(world);

                // create a light
                Light light = new Light(world);
                light.enable();
                light.setIntensity(122, 80, 80);
                light.setPosition(SimpleVector.create(-10, 50, -100));

                world.setAmbientLight(61, 40, 40);

                //world.compileAllObjects();

                // construct camera and move it into position
                Camera cam = world.getCamera();
                cam.moveCamera(Camera.CAMERA_MOVEOUT, 70);
                cam.lookAt(BrainModel.getTransformedCenter());

                //shader seems to be broken at the moment
                //light.setPosition(cam.getPosition());

                BrainModel.setCamera(cam);

                MemoryHelper.compact();

                if (master == null)
                {
                    Logger.log("Saving master Activity!");
                    master = MainActivity.this;
                }
            }
            isLoaded=true;
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config)
        {
            Logger.log("onSurfaceCreated");
        }

        public void onDrawFrame(GL10 gl)
        {
            baseController.updateScene();
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            // clear buffers and draw framerate
            fb.clear(back);
            world.renderScene(fb);
            world.draw(fb);
            fb.display();
        }
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
