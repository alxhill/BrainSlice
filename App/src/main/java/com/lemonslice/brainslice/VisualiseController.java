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

    private SensorManager sensorManager;
    private Sensor gyroSensor;

    long oldTime;
    private boolean isLoaded;

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
        BrainModel.smoothRotateToFront(200);
        BrainModel.smoothZoom(0.62f, 1200);
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

        BrainModel.rotate(x, y, z);

//        BrainModel.adjustCamera();
    }

    @Override
    public boolean touchEvent(MotionEvent me)
    {
        // visualise mode does not use touch events
        return false;
    }

    @Override
    public void stop() {
        return;
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
}
