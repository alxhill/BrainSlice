package com.lemonslice.brainslice;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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

import org.w3c.dom.Text;

import java.lang.reflect.Field;

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

    //Frame overlaying 3d rendering for labels, instructions etc...
    private FrameLayout overlayingFrame;

    protected void onCreate(Bundle savedInstanceState)
    {
        Logger.log("onCreate");
        Logger.setLogLevel(Logger.LL_DEBUG);

        if (master != null)
            copy(master);

        setContentView(R.layout.activity_main);

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
            }
        });


        LinearLayout visualiseButton = (LinearLayout) findViewById(R.id.visualise_button);
        visualiseButton.setOnClickListener(new FrameLayout.OnClickListener() {
            @Override
            public void onClick(View v)
            {
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
        overlayingFrame = (FrameLayout)findViewById(R.id.overlay_layout);

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

    class MyRenderer implements GLSurfaceView.Renderer {

        public MyRenderer()
        {
            Texture.defaultToMipmapping(true);
            Texture.defaultTo4bpp(true);
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
