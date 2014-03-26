package com.lemonslice.brainslice;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.content.Context;
import android.view.Surface;
import android.view.WindowManager;
import android.view.Display;
import android.graphics.Point;
import android.widget.TextView;

import com.lemonslice.brainslice.event.Event;
import com.lemonslice.brainslice.event.EventListener;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handles gyro input and passes it to the brain model
 * Created by alexander on 28/01/2014.
 */
public class VisualiseController extends AbstractController implements SensorEventListener, EventListener {
    // current gyro rotation
    private float axisX, axisY, axisZ;

    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private static Context context;
    long oldTime;
    private boolean isLoaded;

    private TextView overlayLabel;

    private WindowManager mWindowManager;
    private float[] startRotation;

    public VisualiseController(SensorManager manager)
    {
        axisX = 0;
        axisY = 0;
        axisZ = 0;

        isLoaded = false;

        // initialise the sensor manager and listen for gyro events
        sensorManager = manager;
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


    }

    @Override
    public void loadView()
    {
        isLoaded = true;
        oldTime = System.currentTimeMillis();
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
        axisX = axisY = axisZ = 0;
        BrainModel.smoothRotateToFront();
        BrainModel.smoothZoom(0.58f, 1200);
        BrainModel.setLabelsToDisplay(false);
    }

    @Override
    public void unloadView()
    {
        isLoaded = false;
        sensorManager.unregisterListener(this);
    }

    @Override
    public void updateScene()
    {
        if (!isLoaded) return;
        
        long newTime = System.currentTimeMillis();
        long timeDiff = newTime - oldTime;

        if (timeDiff == 0)
        {
            oldTime = newTime;
            return;
        }

        // time since last frame
        float fTimeDiff = (float) timeDiff;

        // Calculate the movement based on the time elapsed
        float x = axisX * fTimeDiff / 1000.0f;
        float y = axisY * fTimeDiff / 1000.0f;
        float z = axisZ * fTimeDiff / 1000.0f;

        oldTime = newTime;

        Context cx = context.getApplicationContext();
        Display d = ((WindowManager) cx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        //Point size = new Point();
        int rotation = d.getRotation();

        if (rotation==Surface.ROTATION_0 || rotation==Surface.ROTATION_180) {
            // this is a tablet
            BrainModel.rotate(y, -x, z);
        } else {
            BrainModel.rotate(x,y,z);
        }

        showSection();

        //BrainModel.adjustCamera();
    }

    public void showSection()
    {
        SimpleVector camPos = BrainModel.getCamera().getPosition();
        Matrix rotMat = BrainModel.getRotationMatrix();
        SimpleVector minPos = null;

        ArrayList<Object3D> spheres = BrainModel.getSpheres();
        String segmentName = null;

        for (Object3D sphere : spheres)
        {
            SimpleVector spherePos = sphere.getTransformedCenter();
            spherePos.rotate(rotMat);
            Log.d("SPHERECENTRE", sphere.getName() + spherePos.toString());
            if (minPos == null || minPos.distance(camPos) > spherePos.distance(camPos))
            {
                minPos = spherePos;
                segmentName = sphere.getName();
            }
        }

        if (segmentName == null) return;

        final BrainSegment finalCurrentSegment = BrainInfo.getSegment(segmentName);

        overlayLabel.post(new Runnable() {
            @Override
            public void run()
            {
                overlayLabel.setText(finalCurrentSegment.getTitle());
            }
        });
    }

    @Override
    public boolean touchEvent(MotionEvent me)
    {
        // visualise mode does not use touch events
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        // Axes set for landscape gyro data
        axisX = -event.values[1];
        axisY = event.values[0];
        axisZ = -event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    public static void setContext(Context c)
    {
        context = c;
    }

    public void setOverlayLabel(TextView overlayLabel)
    {
        this.overlayLabel = overlayLabel;
    }

    @Override
    public void receiveEvent(String name, Object... data)
    {
        if (name.equals("tap:calibrate"))
        {
            startRotation = BrainModel.getPosition();

        }

    }
}
