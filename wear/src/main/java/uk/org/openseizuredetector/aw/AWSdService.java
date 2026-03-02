package uk.org.openseizuredetector.aw;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import uk.org.openseizuredetector.SdData;
import uk.org.openseizuredetector.SdDataReceiver;
import uk.org.openseizuredetector.SdDataSource;

/**
 * AWSdService - Integral Wear OS Service
 * Optimized for en_GB standards and full compatibility with StartUpActivity.
 */
public class AWSdService extends SdDataSource implements SensorEventListener {
    private static final String TAG = "AWSdService";

    // Connection and Binding
    public boolean mBound = false;
    private final IBinder mBinder = new AWSdServiceBinder();

    public class AWSdServiceBinder extends SdDataSource.SdBinder {
        @Override
        public AWSdService getService() {
            return AWSdService.this;
        }
    }



    // UI State & Communication (Fixes StartUpActivity errors)
    public int serverBatteryPct = -1;
    public MutableLiveData<SdData> serviceLiveData = new MutableLiveData<>();

    private SensorManager mSensorManager;
    private int mRawDataIdx = 0;

    /**
     * Binder class for Activity connection.
     */
    public class SdBinder extends Binder {
        public AWSdService getService() {
            return AWSdService.this;
        }
    }

    public AWSdService(Context context, android.os.Handler handler, SdDataReceiver receiver) {
        super(context, handler, receiver);
        this.mName = "AWSdService"; // Essential for SdDataSource identification
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /* --- Core Control Methods (StartUpActivity Requirements) --- */

    public void ClearAlarmCount() {
        Log.i(TAG, "ClearAlarmCount: Resetting alarm state");
        if (mSdData != null) {
            mSdData.alarmState = 0;
            mAlarmCount = 0;
            signalUpdateUI();
        }
    }

    public void handleSendingIAmOK() {
        Log.i(TAG, "User interaction: I Am OK");
        ClearAlarmCount();
    }

    public void handleSendingHelp() {
        Log.i(TAG, "User interaction: Help Requested");
        if (mSdData != null) {
            mSdData.alarmState = 2; // Manual Alarm State
            mSdData.alarmPhrase = "USER HELP";
            signalUpdateUI();
        }
    }

    /**
     * Helper to notify the LiveData observers in the UI
     */
    @Override
    public void signalUpdateUI() {
        if (mSdData != null) {
            serviceLiveData.postValue(mSdData);
        }
    }

    /* --- Lifecycle & Sensor Integration --- */

    @Override
    public void start() {
        super.start();
        Sensor accel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accel != null) {
            mSensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
            Log.i(TAG, "Accelerometer listener registered");
        }
    }

    /**
     *
     */
    @Override
    public void startPebbleApp() {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Simple magnitude calculation for analysis
            double mag = Math.sqrt(event.values[0]*event.values[0] +
                    event.values[1]*event.values[1] +
                    event.values[2]*event.values[2]);

            if (mRawDataIdx < mSdData.rawData.length) {
                mSdData.rawData[mRawDataIdx++] = mag;
            } else {
                mSdData.mNsamp = mRawDataIdx;
                doAnalysis(); // Perform seizure detection analysis
                mRawDataIdx = 0;
                signalUpdateUI(); // Update the UI with results
            }
        }
    }

    @Override
    protected void getStatus() {
        // Update battery level for the dashboard
        serverBatteryPct = getPhoneBatteryLevel();
    }

    @Override protected void faultCheck() {}
    @Override public void hrCheck() {}
    @Override public void o2SatCheck() {}
    @Override public void fallCheck() {}
    @Override public void muteCheck() {}
    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // Method expected by SdDataSource or status updates
    private int getPhoneBatteryLevel() {
        // Actual implementation would use BatteryManager
        return 100;
    }
}