package uk.org.openseizuredetector.aw;

import static androidx.core.content.ContextCompat.getSystemService;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorAdditionalInfo;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorPrivacyManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.runner.RunWith;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import kotlin.jvm.JvmField;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.TimeoutKt;



public class AndroidSensorTest {
    private Sensor sensor;

    public AndroidSensorTest(){
    }
//    @JvmField
//    @RegisterExtension
//    val scenarioExtension = ActivityScenarioExtension.launch<MyActivity>()

    Context context;
    Application application;
    Looper looper;
    Handler handler;
    OsdUtil util;
    AWSdService aWsdService;
    Intent sdServerIntent;
    //ServiceController<AWSdService> controller;
    SdServiceConnection sdServiceConnection;
    AndroidSensor androidSensor;
    String TAG = this.getClass().getName();
    private  SensorManager sensorManager ;
    List<Sensor> deviceSensors ;

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
            Sensor.TYPE_STEP_DETECTOR,
    };

    @BeforeEach
    void initOsdUtil(){
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
                    Assertions.assertTrue(util.isMobileDataActive());
                }
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Assertions.assertTrue(util.isNetworkConnected());
                }
            }
        }

    }


    public void populateSensors(){
        if (Objects.isNull(sensorManager)) sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (Objects.isNull(deviceSensors)) deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        if (Objects.isNull(sensor)) sensor = sensorManager.getDefaultSensor(Sensor.TYPE_POSE_6DOF,true);

    }

    @Timeout( value = 2,
            unit = TimeUnit.HOURS,
            threadMode = Timeout.ThreadMode.SEPARATE_THREAD
    )@Test
    public void getDoesSensorExist() {
        populateSensors();
        for (Sensor sensor:deviceSensors
             ) {
            String sensorFeature =
                    ((String)sensor.getStringType()).replaceFirst("android.sensor","android.hardware.sensor");
            androidSensor = new AndroidSensor(context, sensorFeature,deviceSensors.indexOf(sensor) ,
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
                Log.i(TAG, "Assertion of Sensor returned " +
                        sensor + "  exists as true.");
            }catch (AssertionError assertionError){
                Log.e(TAG,"Assertion failed. Assertation of Sensor returned Sensor " +
                        sensor + " does not exist", assertionError);
            }
            androidSensor = null;
        }
        androidSensor = new MotionDetectSensor(context,3,9) {
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
        Assertions.assertTrue(androidSensor.getDoesSensorExist());
    }

    @Test
    public void startListening() {
         androidSensor = new AndroidSensor(
                context,
                PackageManager.FEATURE_SENSOR_ACCELEROMETER,
                Sensor.TYPE_ACCELEROMETER,
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
    public void isSensorListening() throws InterruptedException {
        populateSensors();
        androidSensor = new AndroidSensor(
                context,
                PackageManager.FEATURE_SENSOR_ACCELEROMETER,
                Sensor.TYPE_ACCELEROMETER,
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

        Thread.sleep(800);
        Assertions.assertTrue( androidSensor.isSensorListening());
    }
    @Test
    public void stopListening() {
        populateSensors();
        androidSensor = new AndroidSensor(
                context,
                PackageManager.FEATURE_SENSOR_HEART_RATE,
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
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertTrue(androidSensor.isSensorListening());
        androidSensor.stopListening();
    }

    @Timeout( value = 5,
            unit = TimeUnit.HOURS,
            threadMode = Timeout.ThreadMode.SEPARATE_THREAD
    )
    @Test
    public void onSensorChanged() {
        boolean[] hasTriggered = {false};
        final CountDownLatch signal = new CountDownLatch(1);



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
                signal.countDown();
            }

            @Override
            public void onSensorAccuracyValueChanged(Sensor sensor, int accuracy) {

            }

            @Override
            public void setOnSensorValuesChangedListener(@NonNull Function1 listener) {

            }
        };
        androidSensor.startListening();
        try {
            if (androidSensor.isSensorListening()) signal.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertTrue(hasTriggered[0]);

    }


    @Test
    public void testSensorAccelleration(){
        AccelerationSensor accelerationSensor = null;
        if (Objects.isNull(accelerationSensor))
            accelerationSensor= new AccelerationSensor(context,(int)4e4,(int)4e4*6) {
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
        if (!accelerationSensor.isSensorListening())
            accelerationSensor.startListening();
        if (accelerationSensor.isSensorListening())
            accelerationSensor.stopListening();
        assertFalse(accelerationSensor.isSensorListening());

    }


    @Timeout( value = 7,
            unit = TimeUnit.DAYS,
            threadMode = Timeout.ThreadMode.SEPARATE_THREAD
    )
    @Test
    public void onAccuracyChanged() {
        boolean[] hasTriggered = {false};
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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertTrue(hasTriggered[0]);
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