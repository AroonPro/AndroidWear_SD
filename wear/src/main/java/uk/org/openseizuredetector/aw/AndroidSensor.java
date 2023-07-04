package uk.org.openseizuredetector.aw;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Objects;

abstract class AndroidSensor implements MeasurableSensor, SensorEventListener {
    Context mContext = null;
    String mSensorFeature = null;
    int mSensorType = 0;
    int mSensorSamplingPeriodUs = 0;
    int mSensorMaxReportLatencyUs;
    private boolean isSensorManagerInitialised;
    private boolean isSensorListening;
    AndroidSensor(Context context,
                  String sensorFeature,
                  int sensorType,
                  int samplingPeriodUs,
                  int maxReportLatencyUs){
        mContext = context;
        mSensorFeature = sensorFeature;
        mSensorType = sensorType;
        mSensorSamplingPeriodUs = samplingPeriodUs;
        mSensorMaxReportLatencyUs = maxReportLatencyUs;
        isSensorManagerInitialised = false;
    }

    @Override
    public boolean getDoesSensorExist() {
        return mContext.getPackageManager().hasSystemFeature(mSensorFeature);
    }

    @Override
    public boolean isSensorListening() {
        return isSensorListening;
    }

    private SensorManager sensorManager;
    private Sensor sensor;

    @Override
    public void startListening() {
        if (!getDoesSensorExist() || isSensorListening){
            return;
        }
        if (!isSensorManagerInitialised&&sensor == null){
            sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            isSensorManagerInitialised = sensorManager != null;
            assert sensorManager != null;
            sensor = sensorManager.getDefaultSensor(mSensorType);
        }
        if (Objects.nonNull(sensor)){
            isSensorListening = sensorManager.registerListener(this,sensor, mSensorSamplingPeriodUs, mSensorMaxReportLatencyUs);
        }
    }

    @Override
    public void stopListening() {
        if(!getDoesSensorExist()||!isSensorManagerInitialised||!isSensorListening ){
            return;
        }
        try{
            sensorManager.unregisterListener(this);
        }
        finally {
            isSensorListening = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!getDoesSensorExist()){
            return;
        }
        if (event.sensor.getType() == mSensorType){
            onSensorValuesChanged(event);
        }
    }

    /**
     * Called when the accuracy of the registered sensor has changed.  Unlike
     * onSensorChanged(), this is only called when this accuracy value changes.
     *
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
