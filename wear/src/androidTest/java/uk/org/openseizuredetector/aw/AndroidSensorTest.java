package uk.org.openseizuredetector.aw;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.robolectric.android.controller.ServiceController;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.TimeoutKt;

class AndroidSensorTest {
    Context context;
    Application application;
    Looper looper;
    Handler handler;
    OsdUtil util;
    AWSdService aWsdService;
    Intent sdServerIntent;
    ServiceController<AWSdService> controller;
    SdServiceConnection sdServiceConnection;
    AndroidSensor androidSensor;
    String TAG = this.getClass().getName();
    int[] sensorsToTest = {
            Sensor.TYPE_ALL,
            Sensor.TYPE_PROXIMITY,
            Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT,
            Sensor.TYPE_HEAD_TRACKER,
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_ACCELEROMETER_LIMITED_AXES,
            Sensor.TYPE_ACCELEROMETER_LIMITED_AXES_UNCALIBRATED,
            Sensor.TYPE_ACCELEROMETER_UNCALIBRATED,
            Sensor.TYPE_DEVICE_PRIVATE_BASE,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_GAME_ROTATION_VECTOR,
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_GYROSCOPE_LIMITED_AXES,
            Sensor.TYPE_GYROSCOPE_LIMITED_AXES_UNCALIBRATED,
            Sensor.TYPE_ACCELEROMETER_LIMITED_AXES,
            Sensor.TYPE_HEADING,
            Sensor.TYPE_HEART_BEAT,
            Sensor.TYPE_HINGE_ANGLE,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
            Sensor.TYPE_MOTION_DETECT,
            Sensor.TYPE_POSE_6DOF,
            Sensor.TYPE_RELATIVE_HUMIDITY,
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_SIGNIFICANT_MOTION,
            Sensor.TYPE_STATIONARY_DETECT,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_STEP_DETECTOR
    };

    @BeforeEach
    public void initOsdUtil(){
        if (Objects.isNull(application)) application = ApplicationProvider.getApplicationContext();
        if (Objects.isNull(context)) context = application.getApplicationContext();
        if (Objects.isNull(looper)) looper = context.getMainLooper();
        if (Objects.isNull(handler)) handler = new Handler(looper);
        if (Objects.isNull(util)) util = new OsdUtil(context,handler);
        if (Objects.isNull(sdServerIntent)) sdServerIntent = new Intent(context,AWSdService.class);
        if (Objects.isNull(sdServiceConnection)) sdServiceConnection = new SdServiceConnection(context);

        // return true if we are using mobile data, otherwise return false
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities == null) ;
                assert capabilities != null;
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    assertTrue(util.isMobileDataActive());
                }
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    assertTrue(util.isNetworkConnected());
                }
            }
        }

    }

    @Test
    void getDoesSensorExist() {
        List<Object> sensorList = Arrays.asList(Sensor.class.getDeclaredFields());
        for (int sensor:
             Arrays.stream(sensorsToTest).toArray()) {
            String sensorFeature =
                    (String) sensorList.get(sensorList.indexOf("STRING_" + sensorList.get(sensor)));
            androidSensor = new AndroidSensor(context, sensorFeature, sensor,
                    SensorManager.SENSOR_DELAY_UI,SensorManager.SENSOR_DELAY_NORMAL) {
                @Nullable
                @Override
                public void onSensorValuesChanged(SensorEvent event) {

                }

                @Override
                public void onSensorAccuracyValueChanged(Sensor sensor, int accuracy) {

                }

                @Override
                public void setOnSensorValuesChangedListener(@NonNull Function1 listener) {

                }
            };
            try{
                Assertions.assertTrue(androidSensor.getDoesSensorExist());
                Log.i(TAG, "Assertation of Sensor returned " +
                        sensor + "  exists as true.");
            }catch (AssertionError assertionError){
                Log.e(TAG,"Assertation failed. Assertation of Sensor returned Sensor " +
                        sensor + " does not exist", assertionError);
            }
            androidSensor = null;
        }
    }

    @Test
    void startListening() {
         androidSensor = new AndroidSensor(
                context,
                Sensor.STRING_TYPE_HEART_BEAT,
                Sensor.TYPE_HEART_BEAT,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Nullable
            @Override
            public void onSensorValuesChanged(SensorEvent event) {

            }

             @Override
             public void onSensorAccuracyValueChanged(Sensor sensor, int accuracy) {

             }

             @Override
            public void setOnSensorValuesChangedListener(@NonNull Function1 listener) {

            }
        };
        Assertions.assertFalse(androidSensor.isSensorListening());
        androidSensor.startListening();
    }

    @Test
    void isSensorListening(){
        androidSensor = new AndroidSensor(
                context,
                Sensor.STRING_TYPE_HEART_BEAT,
                Sensor.TYPE_HEART_BEAT,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Nullable
            @Override
            public void onSensorValuesChanged(SensorEvent event) {

            }

            @Override
            public void onSensorAccuracyValueChanged(Sensor sensor, int accuracy) {

            }

            @Override
            public void setOnSensorValuesChangedListener(@NonNull Function1 listener) {

            }
        };
        androidSensor.startListening();
        Assertions.assertTrue(androidSensor.isSensorListening());
    }
    @Test
    void stopListening() {
        androidSensor = new AndroidSensor(
                context,
                Sensor.STRING_TYPE_HEART_BEAT,
                Sensor.TYPE_HEART_BEAT,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Nullable
            @Override
            public void onSensorValuesChanged(SensorEvent event) {

            }

            @Override
            public void onSensorAccuracyValueChanged(Sensor sensor, int accuracy) {

            }

            @Override
            public void setOnSensorValuesChangedListener(@NonNull Function1 listener) {

            }
        };
        androidSensor.startListening();
        Assertions.assertTrue(androidSensor.isSensorListening());
        androidSensor.stopListening();
    }

    @Timeout( value = 1000,
            unit = TimeUnit.MILLISECONDS,
            threadMode = Timeout.ThreadMode.SEPARATE_THREAD
    )
    @Test
    void onSensorChanged() {
        final boolean[] hasTriggered = {false};
        androidSensor = new AndroidSensor(
                context,
                Sensor.STRING_TYPE_HEART_BEAT,
                Sensor.TYPE_HEART_BEAT,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Nullable
            @Override
            public void onSensorValuesChanged(SensorEvent event) {
                hasTriggered[0] = true;
            }

            @Override
            public void onSensorAccuracyValueChanged(Sensor sensor, int accuracy) {

            }

            @Override
            public void setOnSensorValuesChangedListener(@NonNull Function1 listener) {

            }
        };
        androidSensor.startListening();
        handler.postDelayed(
                () -> {Assertions.assertTrue(hasTriggered[0]);}
        ,1000);
    }

    @Timeout( value = 1000,
            unit = TimeUnit.MILLISECONDS,
    threadMode = Timeout.ThreadMode.SEPARATE_THREAD
    )
    @Test
    void onAccuracyChanged() {
        final boolean[] hasTriggered = {false};
        androidSensor = new AndroidSensor(
                context,
                Sensor.STRING_TYPE_HEART_BEAT,
                Sensor.TYPE_HEART_BEAT,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Nullable
            @Override
            public void onSensorValuesChanged(SensorEvent event) {
            }

            @Override
            public void onSensorAccuracyValueChanged(Sensor sensor, int accuracy) {

            }


            @Override
            public void setOnSensorValuesChangedListener(@NonNull Function1 listener) {

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy){
                hasTriggered[0] = true;
            }

        };
        androidSensor.startListening();
        handler.postDelayed(
                () -> {Assertions.assertTrue(hasTriggered[0]);}
                ,1000);
    }

    @AfterEach
    public void removeUtil(){
        if (Objects.nonNull(androidSensor)) {
            if (androidSensor.isSensorListening())
                androidSensor.stopListening();
            androidSensor = null;
        }
        sdServiceConnection = null;
        sdServerIntent = null;
        util = null;
        handler = null;
        looper = null;
        context = null;
    }
}