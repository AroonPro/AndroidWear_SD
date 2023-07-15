package uk.org.openseizuredetector.aw;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.os.Build;


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
                PackageManager.FEATURE_SENSOR_HEART_RATE,
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
                PackageManager.FEATURE_SENSOR_HINGE_ANGLE,
                Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT,
                sensorDefaultSampleTimeUs,
                sensorDefaultMeasurementReportLatency);
    }

}



