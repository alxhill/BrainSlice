package com.lemonslice.brainslice;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;

/**
 * Handles gyro input and passes it to the brain model
 * Created by alexander on 28/01/2014.
 */
public class VisualiseController extends AbstractController implements SensorEventListener {
    // current gyro rotation
    private float axisX, axisY, axisZ;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    long oldTime;

    public VisualiseController(SensorManager manager)
    {
        axisX = 0;
        axisY = 0;
        axisZ = 0;

        // initialise the sensor manager and listen for gyro events
        mSensorManager = manager;
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    public void loadView()
    {
        oldTime = System.currentTimeMillis();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
        axisX = axisY = axisZ = 0;
    }

    @Override
    public void unloadView()
    {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void updateScene()
    {
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
        float z = axisZ * -fTimeDiff / 1000.0f;

        oldTime = newTime;

        BrainModel.rotate(x, y, z);

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
        axisX = event.values[0];
        axisY = event.values[1];
        axisZ = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

}
