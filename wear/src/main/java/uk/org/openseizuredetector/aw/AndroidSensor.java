package uk.org.openseizuredetector.aw;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import uk.org.openseizuredetector.Constants;

/**
 * AndroidSensor - Abstract base class for all OSD sensors.
 * Handles the lifecycle of Android SensorManager and Event Listeners.
 */
abstract class AndroidSensor implements MeasurableSensor, SensorEventListener {

    private final String TAG = this.getClass().getSimpleName();
    private Context mContext;
    protected String mSensorFeature;
    protected int mSensorType;
    protected int mSensorSamplingPeriodUs;
    protected int mSensorMaxReportLatencyUs;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean isSensorListening = false;
    private String[] mSensorPermissions;

    AndroidSensor(Context context,
                  String sensorFeature,
                  int sensorType,
                  int samplingPeriodUs,
                  int maxReportLatencyUs) {
        this(context, sensorFeature, new String[]{""}, sensorType, samplingPeriodUs, maxReportLatencyUs);
    }

    AndroidSensor(Context context,
                  String sensorFeature,
                  String sensorPermission,
                  int sensorType,
                  int samplingPeriodUs,
                  int maxReportLatencyUs) {
        this(context, sensorFeature, new String[]{sensorPermission}, sensorType, samplingPeriodUs, maxReportLatencyUs);
    }

    AndroidSensor(Context context,
                  String sensorFeature,
                  String[] sensorPermissions,
                  int sensorType,
                  int samplingPeriodUs,
                  int maxReportLatencyUs) {
        this.mContext = context;
        this.mSensorFeature = sensorFeature;
        this.mSensorPermissions = sensorPermissions;
        this.mSensorType = sensorType;
        this.mSensorSamplingPeriodUs = samplingPeriodUs;
        this.mSensorMaxReportLatencyUs = maxReportLatencyUs;

        initialiseSensorManager();
    }

    private void initialiseSensorManager() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager == null) {
            Log.e(TAG, "initialiseSensorManager: SensorManager is NULL");
        }
    }

    @Override
    public boolean getDoesSensorExist() {
        if (mSensorManager == null) return false;

        // Handle Wear Emulator specific heart rate feature string
        String featureToCheck = mSensorFeature;
        if (PackageManager.FEATURE_SENSOR_HEART_RATE.equals(mSensorFeature)) {
            if (Objects.equals(Build.BOARD, Constants.GLOBAL_CONSTANTS.WEAR_EMULATED_IDENTIFIER)) {
                featureToCheck = "android.hardware.sensor.heart_rate"; // Emulator variation
            }
        }

        // Check system features first
        if (mContext.getPackageManager().hasSystemFeature(featureToCheck)) {
            return true;
        }

        // Fallback: Check the actual hardware list
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : sensors) {
            if (s.getType() == mSensorType || s.getStringType().equals(mSensorFeature)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean getHasSensorPermissionGranted() {
        for (String permission : mSensorPermissions) {
            if (!permission.isEmpty() && mContext.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSensorListening() {
        return isSensorListening;
    }

    @Override
    public void startListening() {
        Log.d(TAG, "startListening() called for type: " + mSensorType);

        if (!getDoesSensorExist()) {
            Log.w(TAG, "startListening: Sensor does not exist on this device.");
            return;
        }

        if (isSensorListening) {
            Log.d(TAG, "startListening: Already listening.");
            return;
        }

        if (!getHasSensorPermissionGranted()) {
            Log.e(TAG, "startListening: Permissions not granted for " + Arrays.toString(mSensorPermissions));
            // NOTE: UI-based permission requests should be handled in the Activity, not here.
            return;
        }

        if (mSensor == null) {
            mSensor = mSensorManager.getDefaultSensor(mSensorType);
        }

        if (mSensor != null) {
            isSensorListening = mSensorManager.registerListener(this, mSensor, mSensorSamplingPeriodUs, mSensorMaxReportLatencyUs);
            Log.i(TAG, "startListening: Status = " + isSensorListening);
        } else {
            Log.e(TAG, "startListening: Failed to get default sensor for type " + mSensorType);
        }
    }

    @Override
    public void stopListening() {
        if (isSensorListening && mSensorManager != null) {
            Log.d(TAG, "stopListening() called.");
            mSensorManager.unregisterListener(this);
            isSensorListening = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isSensorListening && event.sensor.getType() == mSensorType) {
            onSensorValuesChanged(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (isSensorListening && sensor.getType() == mSensorType) {
            onSensorAccuracyValueChanged(sensor, accuracy);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            stopListening();
        } finally {
            super.finalize();
        }
    }
}