package uk.org.openseizuredetector.aw;

import static org.junit.jupiter.api.Assertions.*;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import android.os.Handler;
import android.util.Log;

import kotlin.jvm.functions.Function1;


class SamsungWearSpO2SensorTest {
    @Test
    void testSensor() throws InterruptedException {
        Application application = ApplicationProvider.getApplicationContext();
        Context context = application.getApplicationContext();
        Looper looper = context.getMainLooper();
        Handler handler = new Handler(looper);
        SamsungWearSpO2Sensor samsungWearSpO2Sensor = new SamsungWearSpO2Sensor(context,
                (int) Constants.GLOBAL_CONSTANTS.getMaxHeartRefreshRate,
                (int) Constants.GLOBAL_CONSTANTS.getMaxHeartRefreshRate * 4) {
            @Nullable
            @Override
            public void onSensorValuesChanged(SensorEvent event) {
                Log.d(context.getClass().getName(),"Got SensorEvent: " + event);
            }

            @Override
            public void onSensorAccuracyValueChanged(Sensor sensor, int accuracy) {

            }

            @Override
            public void setOnSensorValuesChangedListener(@NonNull Function1 listener) {

            }
        };
        assertNotNull(samsungWearSpO2Sensor);
        assertFalse(samsungWearSpO2Sensor.isSensorListening());
        handler.postDelayed(() -> {
            samsungWearSpO2Sensor.startListening();
            handler.postDelayed(()->assertTrue(samsungWearSpO2Sensor.isSensorListening()),300);
        },100);
        Thread.sleep(TimeUnit.HOURS.toMillis(2));
    }
}