package uk.org.openseizuredetector.aw;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.Objects;

import uk.org.openseizuredetector.OsdUtil;

/**
 * AndroidSensor - en_GB
 * Unified base class for OSD wearable sensors.
 * * * R-Free & SDK 17 'Eggshell' Compatibility:
 * Cleaned from duplicate declarations to ensure stable sensor polling.
 * * LSA Audit 2024:
 * Ensures precise lifecycle management (start/stop) to prevent battery
 * drain and ghost-data during forensic analysis.
 */
abstract class AndroidSensor implements SensorEventListener {

    protected final String TAG;
    protected Context mContext;
    protected String mSensorFeature;
    protected int mSensorType;
    protected int mSensorSamplingPeriodUs;
    protected int mSensorMaxReportLatencyUs;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean isSensorListening = false;

    public AndroidSensor(Context context, String sensorFeature, int sensorType,
                         int sensorSamplingPeriodUs, int sensorMaxReportLatencyUs) {
        this.TAG = this.getClass().getSimpleName();
        this.mContext = context;
        this.mSensorFeature = sensorFeature;
        this.mSensorType = sensorType;
        this.mSensorSamplingPeriodUs = sensorSamplingPeriodUs;
        this.mSensorMaxReportLatencyUs = sensorMaxReportLatencyUs;

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensor = mSensorManager.getDefaultSensor(mSensorType);
        }
    }

    public boolean getDoesSensorExist() {
        return mSensor != null;
    }

    public void startListening() {
        if (getDoesSensorExist() && !isSensorListening) {
            Log.d(TAG, "LSA Audit: Starting sensor listening for " + mSensorFeature);
            mSensorManager.registerListener(this, mSensor, mSensorSamplingPeriodUs, mSensorMaxReportLatencyUs);
            isSensorListening = true;
        }
    }

    public void stopListening() {
        if (isSensorListening) {
            Log.d(TAG, "LSA Audit: Stopping sensor listening for " + mSensorFeature);
            mSensorManager.unregisterListener(this);
            isSensorListening = false;
        }
    }

    public boolean isSensorListening() {
        return isSensorListening;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (getDoesSensorExist() && sensor.getType() == mSensorType) {
            Log.v(TAG, "Accuracy changed: " + accuracy);
        }
    }

    // Abstract method to be implemented by specific sensors (Accel, HR, etc.)
    @Override
    public abstract void onSensorChanged(SensorEvent event);

    /**
     * finalize - en_GB
     * Safety net for the 2012 Eggshell architecture to ensure resources
     * are released if the object is garbage collected.
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            if (isSensorListening) {
                stopListening();
            }
        } finally {
            super.finalize();
        }
    }
}