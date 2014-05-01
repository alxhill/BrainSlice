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
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
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

    private View overlayLabel;

    private TextView soundButton;

    private AudioManager mAudioManager;
    private AboutDialog mAboutDialog;

    // 3D stuff
    private MyRenderer renderer;
    private GLSurfaceView mGLView;
    private FrameBuffer fb = null;
    private World world = null;
    private final RGBColor back = new RGBColor(0,0,0,0);

    // Frame overlaying 3d rendering for labels, instructions etc...
    private FrameLayout overlayingFrame;
    private FrameLayout tutorialFrame;
    private FrameLayout quizFrame;
    private FrameLayout homescreenFrame;
    private Typeface fontAwesome;

    private View switchHolder;
    private CheckBox colourSwitch;
    private CheckBox xRaySwitch;

    protected void onCreate(Bundle savedInstanceState)
    {
        Logger.log("onCreate");
        Logger.setLogLevel(Logger.LL_DEBUG);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);    // Removes title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        overlayingFrame = (FrameLayout)findViewById(R.id.overlay_layout);
        tutorialFrame = (FrameLayout)findViewById(R.id.tutorial_overlay);
        homescreenFrame = (FrameLayout)findViewById(R.id.homescreen_overlay);
        overlayLabel = findViewById(R.id.label_overlay);

        hideSystemBars();

        renderer = new MyRenderer();

        SplashScreen.setContext(this);
        SplashScreen.setFrameLayout(overlayingFrame);
        //SplashScreen.show();

        Labels.setContext(this);
        Labels.setFrameLayout(overlayingFrame);
        OverlayScreen.setContext(this);
        OverlayScreen.setFrameLayout(overlayingFrame);
        Tutorial.setContext(this);
        Tutorial.setFrameLayout(tutorialFrame);
        HomeScreen.setContext(this);
        HomeScreen.setFrameLayout(homescreenFrame);

        VisualiseController.setContext(this);

        super.onCreate(savedInstanceState);
        mGLView = (GLSurfaceView)findViewById(R.id.openGlView);

        // Enable the OpenGL ES2.0 context
        mGLView.setEGLContextClientVersion(2);
        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // initialise and show the 3D renderer
        mGLView.setRenderer(renderer);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mAboutDialog = new AboutDialog(this);

        // set up listening for events
        Events.register(this);

        TextView overlayLabel = (TextView) findViewById(R.id.label_overlay);

        learnController = new LearnController(getApplicationContext());
        learnController.setOverlayLabel(overlayLabel);

        learnController.loadView();
        baseController = learnController;

        visualiseController = new VisualiseController((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        visualiseController.setOverlayLabel(overlayLabel);

        quizFrame = (FrameLayout)findViewById(R.id.quiz_overlay);
        quizController = new QuizController(getApplicationContext());
        quizController.setMainOverlay(quizFrame);

        // set up the buttons with events
        soundButton = iconifyView(R.id.volume_button, 30);
        addButtonListener(soundButton, "volume");
        addButtonListener(R.id.home_button, "home");

        // set up the switches on the visualise and learn views
        switchHolder = findViewById(R.id.switchHolder);

        colourSwitch = (CheckBox)findViewById(R.id.colourSwitch);
        xRaySwitch = (CheckBox)findViewById(R.id.xRaySwitch);

        colourSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrainModel.setDisplayMode(xRaySwitch.isChecked(), colourSwitch.isChecked());
            }
        });

        xRaySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrainModel.setDisplayMode(xRaySwitch.isChecked(), colourSwitch.isChecked());
            }
        });

        // check for internet connectivity and load the data
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean loaded = false;
        Log.d("LOADDATA", "attempting network loading");
        if (networkInfo != null && networkInfo.isConnected())
            loaded = BrainInfo.loadData();
        Log.d("LOADDATA", loaded ? "network loading succeeded" : "attempting local loading ");
        // if there's no internet or the loading failed, use the local data
        if (!loaded)
            BrainInfo.readData(getResources());

        setZOnTop();

        SplashScreen.show();
    }

    public static void setZOnTop()
    {
        //sMGLView.setZOrderOnTop(true);
        //BrainModel.drawBackground = false;
        BrainModel.drawBackground = true;
    }

    public static void setZOnBottom()
    {
        //sMGLView.setZOrderOnTop(false);
        BrainModel.drawBackground = true;
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
        android.os.Process.killProcess(android.os.Process.myPid());
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
        final int uiOptions;
        if (Build.VERSION.SDK_INT >= 19)
        {
            uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int i) {
                    getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                }
            });
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
                if(!HomeScreen.buttonCatcher)
                {
                    HomeScreen.buttonCatcher = true;
                    BrainModel.disableDoubleTap = false;
                    learnController.loadView();
                    HomeScreen.hide();
                    baseController = learnController;
                    Labels.setFrameLayout(overlayingFrame);
                    overlayLabel.setVisibility(View.VISIBLE);
                    switchHolder.setVisibility(View.VISIBLE);
                    renderer.setShouldDraw(true);
                }
            }
            else if (tapType.equals("visualise"))
            {
                if(!HomeScreen.buttonCatcher)
                {
                    HomeScreen.buttonCatcher = true;
                    // overlays the calibrate screen, only loads the visualise controller
                    // after the calibrate button has been pressed.
                    BrainModel.disableBackgroundGlow();
                    HomeScreen.hide();
                    OverlayScreen.showScreen(R.layout.calibrate_screen);
                    baseController = visualiseController;
                    BrainModel.smoothMoveToGeneric(BrainModel.startPosition, 0, 400);
                    BrainModel.smoothRotateToFront();
                    BrainModel.smoothZoom(0.2f, 1200);
                    overlayLabel.setVisibility(View.VISIBLE);
                    switchHolder.setVisibility(View.VISIBLE);
                    renderer.setShouldDraw(true);
                }
            }
            else if (tapType.equals("home"))
            {
                HomeScreen.buttonCatcher = false;
                baseController.unloadView();
                baseController = learnController;
                //BrainModel.onlyRotateY = true;
                quizFrame.removeAllViews();
                overlayingFrame.removeAllViews();
                HomeScreen.show();
                BrainInfo.speaker.stop();
                renderer.setShouldDraw(false);
            }
            else if (tapType.equals("quiz"))
            {
                if(!HomeScreen.buttonCatcher)
                {
                    HomeScreen.buttonCatcher = true;
                    //overlayingFrame.removeAllViews();
                    HomeScreen.hide();
                    quizController.loadView();
                    baseController = quizController;
                    overlayLabel.setVisibility(View.GONE);
                    switchHolder.setVisibility(View.GONE);
                    renderer.setShouldDraw(true);
                }
            }
            else if (tapType.equals("calibrate"))
            {
                visualiseController.loadView();
                renderer.setShouldDraw(true);
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
                if(!HomeScreen.buttonCatcher)
                {
                    HomeScreen.buttonCatcher = true;
                    Tutorial.show(1,false);
                    renderer.setShouldDraw(false);
                }
            }
            else if (tapType.equals("about"))
            {
                mAboutDialog.show();
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
        private boolean shouldDraw = true;

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
            onDrawFrame(gl);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config)
        {
            Logger.log("onSurfaceCreated");
        }

        public void onDrawFrame(GL10 gl)
        {
            if (shouldDraw) {
                //Log.d("BrainSlice","onDrawFrame");
                try {
                    BrainModel.displaySemaphore.acquire();
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
                catch(InterruptedException e)
                {
                    // do nothing
                }
                finally
                {
                    BrainModel.displaySemaphore.release();
                }
            }
        }

        public void setShouldDraw(boolean shouldDraw) {
            this.shouldDraw = shouldDraw;
        }
    }
}
