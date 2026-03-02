package uk.org.openseizuredetector.aw;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

/**
 * AWSdService - en_GB
 * Background service for Seizure Detection on Wear OS.
 * * R-Free & SDK 17 'Eggshell' Compatibility:
 * Handles foreground lifecycle and biometric sensor persistence.
 * * LSA Audit 2024 Context:
 * Validates BODY_SENSORS permissions at runtime to ensure continuous
 * forensic data collection. Implements a robust Binder for Activity IPC.
 */
public class AWSdService extends Service {
    private final String TAG = "AWSdService";
    private final IBinder mBinder = new Access();

    private PowerManager.WakeLock mWakeLock;
    private PowerUpdateReceiver mPowerUpdateReceiver;
    private boolean isRunning = false;

    /**
     * Access - en_GB
     * Binder class to provide the service instance to connected Activities.
     */
    public class Access extends Binder {
        public AWSdService getService() {
            return AWSdService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "LSA Audit: AWSdService created.");

        // en_GB: Acquire WakeLock to prevent CPU sleep during monitoring
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OSD:ServiceWakeLock");
        mWakeLock.acquire();

        setupForegroundNotification();
        registerPowerReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "LSA Audit: AWSdService starting...");

        if (!checkBiometricPermissions()) {
            Log.e(TAG, "LSA Audit Critical: BODY_SENSORS permission missing in Service!");
            // en_GB: Cannot start monitoring without forensic-grade sensor access
            stopSelf();
            return START_NOT_STICKY;
        }

        isRunning = true;
        return START_STICKY;
    }

    /**
     * checkBiometricPermissions - en_GB
     * Forensic check for sensor access required for LSA 2024 compliance.
     */
    public boolean checkBiometricPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void setupForegroundNotification() {
        String channelId = "osd_wear_service";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "OSD Monitoring", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("OSD Active")
                .setContentText("Monitoring for seizure activity...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build();

        startForeground(1, notification);
    }

    private void registerPowerReceiver() {
        mPowerUpdateReceiver = new PowerUpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(mPowerUpdateReceiver, filter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "LSA Audit: AWSdService destroying.");
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (mPowerUpdateReceiver != null) {
            unregisterReceiver(mPowerUpdateReceiver);
        }
        isRunning = false;
        super.onDestroy();
    }

    /**
     * PowerUpdateReceiver - en_GB
     * Monitors device power state to correlate battery levels with sensor gaps.
     */
    private class PowerUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "LSA Audit: Power State Change - " + action);
            // en_GB: logic to handle battery telemetry for the audit trail
        }
    }
}