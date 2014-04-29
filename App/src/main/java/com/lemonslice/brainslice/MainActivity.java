package com.lemonslice.brainslice;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lemonslice.brainslice.event.Events;
import com.lemonslice.brainslice.event.EventListener;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Based off JPCT HelloShader freely licenced example by EgonOlsen, heavily modified by LemonSlice
 */
public class MainActivity extends FragmentActivity implements EventListener {

    private AbstractController baseController;
    private LearnController learnController;
    private VisualiseController visualiseController;
    QuizController quizController;

    private TextView soundButton;

    private AudioManager mAudioManager;
    private SettingsMenu mSettingsMenu;

    // 3D stuff
    private GLSurfaceView mGLView;
    private FrameBuffer fb = null;
    private World world = null;
    private final RGBColor back = new RGBColor(0,0,0,0);

    // Frame overlaying 3d rendering for labels, instructions etc...
    private FrameLayout overlayingFrame;
    private FrameLayout tutorialFrame;
    private Typeface fontAwesome;

    protected void onCreate(Bundle savedInstanceState)
    {
        Logger.log("onCreate");
        Logger.setLogLevel(Logger.LL_DEBUG);

        setContentView(R.layout.activity_main);

        overlayingFrame = (FrameLayout)findViewById(R.id.overlay_layout);
        tutorialFrame = (FrameLayout)findViewById(R.id.tutorial_overlay);
        hideSystemBars();

        MyRenderer renderer = new MyRenderer();

        SplashScreen.setContext(this);
        SplashScreen.setFrameLayout(overlayingFrame);
        SplashScreen.setRenderer(renderer);
        SplashScreen.show();

        Labels.setContext(this);
        Labels.setFrameLayout(overlayingFrame);
        OverlayScreen.setContext(this);
        OverlayScreen.setFrameLayout(overlayingFrame);
        Tutorial.setContext(this);
        Tutorial.setFrameLayout(tutorialFrame);
        HomeScreen.setContext(this);
        HomeScreen.setFrameLayout(overlayingFrame);

        VisualiseController.setContext(this);

        super.onCreate(savedInstanceState);
        mGLView = (GLSurfaceView)findViewById(R.id.openGlView);

        // Enable the OpenGL ES2.0 context
        mGLView.setEGLContextClientVersion(2);
        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mGLView.setZOrderOnTop(false);

        // initialise and show the 3D renderer
        mGLView.setRenderer(renderer);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mSettingsMenu = new SettingsMenu(this);

        // set up listening for events
        Events.register(this);

        TextView overlayLabel = (TextView) findViewById(R.id.label_overlay);

        learnController = new LearnController(getApplicationContext());
        learnController.setOverlayLabel(overlayLabel);

        learnController.loadView();
        baseController = learnController;

        visualiseController = new VisualiseController((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        visualiseController.setOverlayLabel(overlayLabel);

        quizController = new QuizController(getApplicationContext());
        quizController.setMainOverlay(overlayingFrame);
        quizController.setOverlayLabel(overlayLabel);

        // set up the button events
        iconifyView(R.id.help_button, 25);
        iconifyView(R.id.settings_button, 25);
        soundButton = iconifyView(R.id.volume_button, 25);

        addButtonListener(R.id.help_button, "help");
        addButtonListener(R.id.settings_button, "settings");
        addButtonListener(soundButton, "volume");
        addButtonListener(R.id.home_button, "home");
        addButtonListener(R.id.info_button, "info");

        // check for internet connectivity and load the data
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean loaded = false;
        if (networkInfo != null && networkInfo.isConnected())
            loaded = BrainInfo.loadData();
        // if there's no internet or the loading failed, use the local data
        if (!loaded)
            BrainInfo.readData(getResources());
    }

    private TextView iconifyView(int resId, int size)
    {
        if (fontAwesome == null)
            fontAwesome = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
        TextView view = (TextView) findViewById(resId);
        view.setTypeface(fontAwesome);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        return view;
    }

    private void addButtonListener(int resId, String name)
    {
        View v = findViewById(resId);
        addButtonListener(v, name);
    }

    static void addButtonListener(View v, final String name)
    {
        final String eventName = "tap:" + name;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Events.trigger(eventName);
            }
        });
    }

    // This is to prevent accidental presses of the volume keys
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                //BrainModel.onVolumeKey(keyCode, event);
                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onPause()
    {
        Logger.log("onPause");
        super.onPause();
        mGLView.onPause();
        android.os.Process.killProcess(android.os.Process.myPid());
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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

    @Override
    public void receiveEvent(String name, Object... data)
    {
        String[] events = name.split(":");
        if (events[0].equals("tap"))
        {
            String tapType = events[1];
            if (tapType.equals("learn"))
            {
                baseController.unloadView();
                BrainModel.disableDoubleTap = false;
                learnController.loadView();
                overlayingFrame.removeAllViews();
                baseController = learnController;
                findViewById(R.id.info_button).setVisibility(View.VISIBLE);
            }
            else if (tapType.equals("visualise"))
            {
                // overlays the calibrate screen, only loads the visualise controller
                // after the calibrate button has been pressed.
                OverlayScreen.showScreen(R.layout.calibrate_screen);
                baseController.unloadView();
                baseController = visualiseController;
                findViewById(R.id.info_button).setVisibility(View.INVISIBLE);
            }
            else if (tapType.equals("home"))
            {
                baseController = learnController;
                BrainModel.onlyRotateY = true;
                HomeScreen.show();
            }
            else if (tapType.equals("info"))
            {
                BrainModel.infoTapped();
            }
            else if (tapType.equals("quiz"))
            {
                baseController.unloadView();
                overlayingFrame.removeAllViews();
                quizController.loadView();
                baseController = quizController;
                findViewById(R.id.info_button).setVisibility(View.VISIBLE);
            }
            else if (tapType.equals("calibrate"))
            {
                visualiseController.loadView();
            }
            else if (tapType.equals("volume"))
            {
                int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (streamVolume == 0)
                {
                    mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                    soundButton.setText(R.string.volume_icon);
                }
                else
                {
                    mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    soundButton.setText(R.string.volume_icon_mute);
                }
            }
            else if (tapType.equals("help"))
            {
                Tutorial.show();
            }
            else if (tapType.equals("settings"))
            {
                mSettingsMenu.show();
            }
        }
        else if (name.equals("data:loaded"))
        {
            if (BrainModel.isLoaded)
                BrainModel.loadSegments(getResources());
        }
        else if (name.equals("model:loaded"))
        {
            if (!BrainModel.spheresLoaded && BrainInfo.isDataIsLoaded())
                BrainModel.loadSegments(getResources());
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

            world = new World();

            BrainModel.load(res, getApplicationContext());

            BrainModel.addToScene(world);

            // create a light
            Light light = new Light(world);
            light.enable();
            light.setIntensity(122, 80, 80);
            light.setPosition(SimpleVector.create(100, 100, 0));

            Light light2 = new Light(world);
            light2.enable();
            light2.setIntensity(122, 80, 80);
            light2.setPosition(SimpleVector.create(100, -100, 0));

            Light light3 = new Light(world);
            light3.enable();
            light3.setIntensity(122, 80, 80);
            light3.setPosition(SimpleVector.create(-1000, 0, 0));

            world.setAmbientLight(61, 40, 40);

            // construct camera and move it into position
            Camera camera = world.getCamera();
            camera.moveCamera(Camera.CAMERA_MOVEOUT, 70);
            camera.lookAt(BrainModel.getTransformedCenter());

            BrainModel.setCamera(camera);

            MemoryHelper.compact();

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
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            BrainModel.updateCameraPos();
            fb.clear(back);

            BrainModel.removeAll(world);
            BrainModel.addToScene(world);

            world.renderScene(fb);
            world.draw(fb);

            BrainModel.removeAll(world);
            BrainModel.addToTransp(world);

            world.renderScene(fb);
            world.draw(fb);

            fb.display();
        }
    }


}
