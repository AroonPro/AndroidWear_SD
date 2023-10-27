package uk.org.openseizuredetector.aw;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import androidx.work.multiprocess.ListenableCallback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.jvm.functions.Function1;

public interface MeasurableSensor {


    abstract void onSensorValuesChanged(SensorEvent event);

    abstract void onSensorAccuracyValueChanged(Sensor sensor, int accuracy);

    boolean doesSensorExist = false;
    abstract boolean getDoesSensorExist();
    abstract boolean getHasSensorPermissionGranted();
    abstract boolean isSensorListening();
    abstract void startListening();
    abstract void stopListening();

    public void setOnSensorValuesChangedListener(@NotNull Function1 listener) ;


}
