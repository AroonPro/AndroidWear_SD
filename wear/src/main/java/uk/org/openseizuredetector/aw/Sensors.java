package uk.org.openseizuredetector.aw;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.os.Build;
import android.system.Os;
import android.util.Log;


abstract class AccelerationSensor extends AndroidSensor {
    AccelerationSensor(Context context,
                       int sensorDefaultSampleTimeUs,
                       int sensorDefaultMeasurementReportLatency) {
        super(context,
                PackageManager.FEATURE_SENSOR_ACCELEROMETER,
                Sensor.TYPE_ACCELEROMETER,
                sensorDefaultSampleTimeUs,
                sensorDefaultMeasurementReportLatency);

    }
}

abstract class HeartRateSensor extends AndroidSensor {
    HeartRateSensor(Context context,
                    int sensorDefaultSampleTimeUs,
                    int sensorDefaultMeasurementReportLatency) {
        super(context,
                PackageManager.FEATURE_SENSOR_HEART_RATE
                ,
                new String[]{
                        Manifest.permission.BODY_SENSORS,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU?Manifest.permission.BODY_SENSORS_BACKGROUND:"",
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && sensorDefaultSampleTimeUs <6e3)?Manifest.permission.HIGH_SAMPLING_RATE_SENSORS:"",
                        ""
                },
                Sensor.TYPE_HEART_RATE,
                sensorDefaultSampleTimeUs,
                sensorDefaultMeasurementReportLatency);
    }


}

abstract class HeartBeatSensor extends AndroidSensor {
    HeartBeatSensor(Context context,
                    int sensorSamplingPeriodUs,
                    int sensorDefaultMeasurementReportLatency) {
        super(context,
                PackageManager.FEATURE_SENSOR_HEART_RATE_ECG,
                new String[]{
                    Manifest.permission.BODY_SENSORS,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU?Manifest.permission.BODY_SENSORS_BACKGROUND:"",
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && sensorSamplingPeriodUs <6e3)?Manifest.permission.HIGH_SAMPLING_RATE_SENSORS:"",
                        ""
                },

                Sensor.TYPE_HEART_BEAT,
                sensorSamplingPeriodUs,
                sensorDefaultMeasurementReportLatency);
    }



}
abstract class SamsungWearSpO2Sensor extends AndroidSensor {
    SamsungWearSpO2Sensor (Context context,
                    int sensorSamplingPeriodUs,
                    int sensorDefaultMeasurementReportLatency) {
        super(context,
                "com.samsung.sensor.spo2continuous",
                new String[]{
                        "com.samsung.permission.SSENSOR",
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU?Manifest.permission.BODY_SENSORS_BACKGROUND:"",
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && sensorSamplingPeriodUs <6e3)?Manifest.permission.HIGH_SAMPLING_RATE_SENSORS:"",
                        ""
                },

                Constants.GLOBAL_CONSTANTS.COM_SAMSUNG_WEAR_SENSOR_CONTINUOUS_SPO2, // Samsung SPO2
                sensorSamplingPeriodUs,
                sensorDefaultMeasurementReportLatency);
    }

}

abstract class MotionDetectSensor extends AndroidSensor {
    MotionDetectSensor(Context context,
                       int sensorDefaultSampleTimeUs,
                       int sensorDefaultMeasurementReportLatency) {
        super(context,
                PackageManager.FEATURE_SENSOR_GYROSCOPE,
                Sensor.TYPE_MOTION_DETECT,
                sensorDefaultSampleTimeUs,
                sensorDefaultMeasurementReportLatency);
    }

}

abstract class OffBodyDetectSensor extends AndroidSensor {
    OffBodyDetectSensor(Context context,
                       int sensorDefaultSampleTimeUs,
                       int sensorDefaultMeasurementReportLatency) {
        super(context,
                PackageManager.FEATURE_SENSOR_PROXIMITY,
                Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT,
                sensorDefaultSampleTimeUs,
                sensorDefaultMeasurementReportLatency);
    }



}

abstract class ProximitySensor extends AndroidSensor {
    ProximitySensor(Context context,
                    int sensorDefaultSampleTimeUs,
                    int sensorDefaultMeasurementReportLatency) {

        super(context,
                PackageManager.FEATURE_SENSOR_PROXIMITY,
                Sensor.TYPE_PROXIMITY,
                sensorDefaultSampleTimeUs,
                sensorDefaultMeasurementReportLatency);

    }
}

abstract class LightSensor extends AndroidSensor {
    LightSensor (Context context,
                 int sensorDefaultSampleTimeUs,
                 int sensorDefaultMeasurementReportLatency){
        super(context,
                PackageManager.FEATURE_SENSOR_LIGHT,
                Sensor.TYPE_LIGHT,
                sensorDefaultSampleTimeUs,
                sensorDefaultMeasurementReportLatency);
    }
}
abstract class AmbientTemperatureSensor extends AndroidSensor {
    AmbientTemperatureSensor (Context context,
                 int sensorDefaultSampleTimeUs,
                 int sensorDefaultMeasurementReportLatency){
        super(context,
                PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE,
                Sensor.TYPE_AMBIENT_TEMPERATURE,
                sensorDefaultSampleTimeUs,
                sensorDefaultMeasurementReportLatency);
    }
}


abstract class StationaryDetectSensor extends AndroidSensor {
    StationaryDetectSensor (Context context,
                 int sensorDefaultSampleTimeUs,
                 int sensorDefaultMeasurementReportLatency){
        super(context,
                PackageManager.FEATURE_SENSOR_ACCELEROMETER,
                Sensor.TYPE_STATIONARY_DETECT,
                sensorDefaultSampleTimeUs,
                sensorDefaultMeasurementReportLatency);
    }
}

abstract class EnvironmentalPressureSensor extends AndroidSensor {
    EnvironmentalPressureSensor (Context context,
                                 int sensorDefaultSampleTimeUs,
                                 int sensorDefaultMeasurementReportLatency) {
        super(context,
                PackageManager.FEATURE_SENSOR_BAROMETER,
                Sensor.TYPE_PRESSURE,
                sensorDefaultSampleTimeUs,
                sensorDefaultMeasurementReportLatency);
    }
}