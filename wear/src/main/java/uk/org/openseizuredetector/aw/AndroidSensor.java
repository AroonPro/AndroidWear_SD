package uk.org.openseizuredetector.aw;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

abstract class AndroidSensor implements MeasurableSensor, SensorEventListener {

    private final String TAG = this.getClass().getName();
    private Context mContext = null;
    String mSensorFeature = null;
    int mSensorType = 0;
    int mSensorSamplingPeriodUs = 0;
    int mSensorMaxReportLatencyUs;
    private boolean isSensorManagerInitialised;
    private boolean isSensorListening;
    private String[] mSensorPermissions;
    private boolean successUnRegister;



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
        mSensorPermissions = new String[]{""};
        if (!isSensorManagerInitialised) initialiseSensorManager();
    }
    AndroidSensor(Context context,
                  String sensorFeature,
                  String sensorPermission,
                  int sensorType,
                  int samplingPeriodUs,
                  int maxReportLatencyUs){
        mContext = context;
        mSensorFeature = sensorFeature;
        mSensorPermissions = new String[]{sensorPermission};
        mSensorType = sensorType;
        mSensorSamplingPeriodUs = samplingPeriodUs;
        mSensorMaxReportLatencyUs = maxReportLatencyUs;
        if (!isSensorManagerInitialised) initialiseSensorManager();
    }
AndroidSensor(Context context,
                  String sensorFeature,
                  String[] sensorPermissions,
                  int sensorType,
                  int samplingPeriodUs,
                  int maxReportLatencyUs){
        mContext = context;
        mSensorFeature = sensorFeature;
        mSensorPermissions = sensorPermissions;
        mSensorType = sensorType;
        mSensorSamplingPeriodUs = samplingPeriodUs;
        mSensorMaxReportLatencyUs = maxReportLatencyUs;
        if (!isSensorManagerInitialised) initialiseSensorManager();
    }

    private void initialiseSensorManager()
    {   OsdUtil.sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        isSensorManagerInitialised = OsdUtil.sensorManager != null;
        assert OsdUtil.sensorManager != null;
        OsdUtil.availableSensors = OsdUtil.sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.d(this.getClass().getName(),"Filled availableSensors");
    }

    @Override
    public boolean getDoesSensorExist() {
        boolean featureMatches = false;
        if (!isSensorManagerInitialised)
            return false;

        if (PackageManager.FEATURE_SENSOR_HEART_RATE.equals(mSensorFeature)) {
            mSensorFeature = !Objects.equals(Build.BOARD, Constants.GLOBAL_CONSTANTS.WEAR_EMULATED_IDENTIFIER) ? PackageManager.FEATURE_SENSOR_HEART_RATE :
                    PackageManager.FEATURE_SENSOR_HEART_RATE.replace(".hardware.sensor.heartrate", ".sensor.heart_rate");
        }
        //Check here if we are dealing with Android Round Wear Qemu virtual device.
        //Here the check if the feature exists in the list of found features breaks premature.
        //This solution is at this moment the cleanest.
        for (Sensor sensor1: OsdUtil.availableSensors){
            if (sensor1.getStringType().equals(mSensorFeature)) {
                featureMatches = true;
                break;
            }
        }
        return mContext.getPackageManager().hasSystemFeature(mSensorFeature) ||
                featureMatches;
    }


    private boolean hasNoPermission(String permission){
        return mContext.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public boolean getHasSensorPermissionGranted(){
        boolean returnValue = true;
        for (String permission : mSensorPermissions){
            if (!permission.isEmpty()){
                if (hasNoPermission(permission)) {
                    returnValue = false;
                    break;
                }
            }

        }
        return  returnValue;
    }

    @Override
    public boolean isSensorListening() {
        return isSensorListening;
    }

    private SensorManager sensorManager;
    private Sensor sensor;

    @Override
    public void startListening() {
        Log.d(TAG,"AndroidSensor( StartListening() starting with typeId: "+ mSensorType);
        if (!getDoesSensorExist() || isSensorListening){
            Log.e(TAG,"startListening() failed with: getDoesSensorExist(): "+
                    getDoesSensorExist() + " and isSensorListening: " + isSensorListening + " in: \n" + Arrays.toString(Thread.currentThread().getStackTrace()));
            return;
        }
        if (!isSensorManagerInitialised)
                return;
        if (Objects.isNull(sensor)){

            sensor = OsdUtil.sensorManager.getDefaultSensor(mSensorType);
        }
        if (Objects.nonNull(sensor)){
            if (!getHasSensorPermissionGranted() && mContext instanceof Activity)
            {
                for (String permission: mSensorPermissions){
                    if (!permission.isEmpty()){
                        if (hasNoPermission(permission) ) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,permission))
                                ActivityCompat.requestPermissions((Activity) mContext, new String[]{permission}, 0);
                        }
                    }
                }
            }
            isSensorListening = OsdUtil.sensorManager.registerListener(this,sensor, mSensorSamplingPeriodUs, mSensorMaxReportLatencyUs);
            Log.d(this.getClass().getName(), "Exiting startListening() with status: Sensor is " + (!isSensorListening? "not ":"") + "listening" );
        }else
        {
            try {
                Log.e(TAG, "AndroidSensor(): startListening(): getDefaultSensor with type: " + mSensorType + " failed.", new Throwable(Arrays.stream(Thread.currentThread().getStackTrace()).toString()));
            }catch (Throwable throwable){
                //empty pass
            }
        }
    }


    @Override
    public void stopListening() {
        if(!getDoesSensorExist()||!isSensorManagerInitialised||!isSensorListening ){
            return;
        }

        try{
            OsdUtil.sensorManager.unregisterListener(this);
            successUnRegister = true;
        }
        finally {
            if (successUnRegister) {
                isSensorListening = false;

            }
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
     * @param sensor The Senor as source of event.
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (!getDoesSensorExist()){
            return;
        }
        if (sensor.getType() == mSensorType){
            onSensorAccuracyValueChanged(sensor,accuracy);
        }
    }

    /**
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable {
        Log.d(TAG,"finalize() being called. Status of sensor "+ mSensorFeature +": is " + (!isSensorListening? "not ":"")+ "listening.");
        if (Objects.nonNull(sensor))
        {
            if (isSensorListening())
                stopListening();
            sensor = null;
        }
        if (Objects.nonNull(mContext)){
            mContext = null;
        }
        if (Objects.nonNull(OsdUtil.sensorManager)) {
            OsdUtil.sensorManager.flush(this);
            OsdUtil.sensorManager = null;
        }if (Objects.nonNull(OsdUtil.availableSensors)) {
            OsdUtil.availableSensors = null;
        }
        super.finalize();
    }
}
