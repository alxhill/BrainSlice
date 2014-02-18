package com.lemonslice.brainslice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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
import android.widget.Toast;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private String selectedSegment = null;

    // Horizontal progress bar on loading screen
    private ProgressBar progressBar;

    protected void onCreate(Bundle savedInstanceState)
    {
        Logger.log("onCreate");
        Logger.setLogLevel(Logger.LL_DEBUG);

        if (master != null)
            copy(master);

        setContentView(R.layout.activity_main);

        hideSystemBars();
        overlayingFrame = (FrameLayout)findViewById(R.id.overlay_layout);
        progressBar = (ProgressBar)findViewById(R.id.progressBarMain);
        startLoadingScreen();

        super.onCreate(savedInstanceState);
        mGLView = (GLSurfaceView)findViewById(R.id.openGlView);

        // Enable the OpenGL ES2.0 context
        mGLView.setEGLContextClientVersion(2);

        // initialise and show the 3D renderer
        renderer = new MyRenderer();
        mGLView.setRenderer(renderer);

        learnController = new LearnController(getApplicationContext());
        learnController.loadView();
        baseController = learnController;

        visualiseController = new VisualiseController((SensorManager) getSystemService(Context.SENSOR_SERVICE));

        LinearLayout learnButton = (LinearLayout) findViewById(R.id.learn_button);
        learnButton.setOnClickListener(new FrameLayout.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                visualiseController.unloadView();
                learnController.loadView();
                baseController = learnController;
                (findViewById(R.id.segment_list)).setVisibility(View.VISIBLE);
            }
        });


        LinearLayout visualiseButton = (LinearLayout) findViewById(R.id.visualise_button);
        visualiseButton.setOnClickListener(new FrameLayout.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                (findViewById(R.id.segment_list)).setVisibility(View.GONE);
                ((FrameLayout)(findViewById(R.id.overlay_layout))).removeAllViews();
                learnController.unloadView();
                visualiseController.loadView();
                baseController = visualiseController;
            }
        });

        Typeface fontAwesome = Typeface.createFromAsset(getAssets(), "fonts/glyphicons-halflings-regular.ttf");

        TextView learnIcon = (TextView) learnButton.getChildAt(0);
        assert learnIcon != null;
        learnIcon.setTypeface(fontAwesome);
        learnIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        TextView visIcon = (TextView) visualiseButton.getChildAt(0);
        assert visIcon != null;
        visIcon.setTypeface(fontAwesome);
        visIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        //frame layout to pass view to
//        overlayingFrame = (FrameLayout)findViewById(R.id.overlay_layout);

//        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        overlayingFrame.addView(Labels.getLabel(inflater, "Cerebellum"));

////        set custom font
//        Typeface typeface=Typeface.createFromAsset(getAssets(),"fonts/Futura.otf");
//        TextView segmentTitleView =(TextView)findViewById(R.id.segment_title);
//        segmentTitleView.setTypeface(typeface);


        ListView segListView = (ListView)findViewById(R.id.segment_list);
        ArrayList<String> segList = new ArrayList<String>();

        BrainInfo bi = new BrainInfo();
        HashMap<String, BrainSegment> bs = bi.getSegments();

        for(BrainSegment s : bs.values()) {
            segList.add(s.getTitle());
            Log.d("BRAIN SLICE", s.getTitle());
        }

        Collections.sort(segList);

        StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, segList);
        segListView.setAdapter(adapter);


        segListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String segment = ((TextView)view).getText().toString();

                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                overlayingFrame.removeAllViews();

                if((segment.equals(selectedSegment)))
                {
                    selectedSegment = null;
                }
                else
                {
                    if(Labels.getLabel(inflater, segment) != null)
                        overlayingFrame.addView(Labels.getLabel(inflater, segment));

                    selectedSegment = segment;
                }
            }
        });
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

    public void startLoadingScreen()
    {
        progressBar.setProgress(0);
        showLoadingScreen();
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        progressBar.setProgress(progressBar.getProgress()+1);
                        if ((progressBar.getProgress() > 99) && (renderer.isLoaded()))
                            hideLoadingScreen();
                    }
                });
            }
        }, 0, 40);
    }

    public void showLoadingScreen()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                overlayingFrame.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideLoadingScreen()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                overlayingFrame.setVisibility(View.INVISIBLE);
            }
        });
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
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

            if (master == null)
            {
                world = new World();

                BrainModel.load(res);

                BrainModel.addToScene(world);

                // create a light
                Light light = new Light(world);
                light.enable();
                light.setIntensity(122, 80, 80);
                light.setPosition(SimpleVector.create(-10, 50, -100));

                world.setAmbientLight(61, 40, 40);

                // construct camera and move it into position
                Camera cam = world.getCamera();
                cam.moveCamera(Camera.CAMERA_MOVEOUT, 70);
                cam.lookAt(BrainModel.getTransformedCenter());

                MemoryHelper.compact();

                world.compileAllObjects();

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

//    public class AndroidExternalFontsActivity extends Activity {
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.activity_main);
//
//            // Font path
//            String fontPath = "fonts/Futura.otf";
//
//            // text view label
//            TextView txtFutura = (TextView) findViewById(R.id.segment_title);
//
//            // Loading Font Face
//            Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
//
//            // Applying font
//            txtFutura.setTypeface(tf);
//        }
//    }
}
