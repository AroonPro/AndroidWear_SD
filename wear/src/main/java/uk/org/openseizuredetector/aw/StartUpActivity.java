package uk.org.openseizuredetector.aw;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

/**
 * StartUpActivity - en_GB
 * Main entry point for the OSD Wearable application.
 * * R-Free & SDK 17 Compatibility:
 * Dynamically handles permission requests and service binding.
 * * LSA Audit 2024 Context:
 * Ensures all required biometric and system permissions are granted
 * before data collection starts to maintain forensic validity.
 */
public class StartUpActivity extends AppCompatActivity {
    private final String TAG = "StartUpActivity";
    private static final int PERMISSION_REQUEST_CODE = 42;

    private AWSdService aWSdService;
    private boolean isBound = false;

    // en_GB: List of required permissions for the 2024 Audit baseline
    private final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.BODY_SENSORS,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.POST_NOTIFICATIONS : null
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // en_GB: R-Free layout loading
        setContentView(getResources().getIdentifier("activity_startup", "layout", getPackageName()));

        if (checkAndRequestPermissions()) {
            startAndBindSdService();
        }
    }

    /**
     * checkAndRequestPermissions - en_GB
     * Verifies that the user has granted all forensic-essential permissions.
     * Required for LSA 2024 compliance.
     */
    private boolean checkAndRequestPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (permission != null && ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "LSA Audit: Permission missing - " + permission);
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "LSA Audit: Permissions granted by user.");
                startAndBindSdService();
            } else {
                Log.e(TAG, "LSA Audit Critical: Permissions denied. Forensic logging disabled.");
                // en_GB: Handle denial (e.g., show explanation UI)
            }
        }
    }

    /**
     * startAndBindSdService - en_GB
     * Initialises the background service for seizure monitoring.
     */
    private void startAndBindSdService() {
        Intent intent = new Intent(this, AWSdService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Connection - en_GB
     * Clean implementation of ServiceConnection.
     * Fixed from LLM-generated recursive loop errors.
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AWSdService.Access access = (AWSdService.Access) iBinder;
            aWSdService = access.getService();
            isBound = true;
            Log.i(TAG, "LSA Audit: AWSdService bound and active.");
            updateUiTask();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            aWSdService = null;
            isBound = false;
            Log.w(TAG, "LSA Audit: AWSdService disconnected.");
        }
    };

    private void updateUiTask() {
        new Handler(Looper.getMainLooper()).post(() -> {
            // en_GB: Update UI elements (status, battery, etc.)
            Log.v(TAG, "UI Update sequence triggered.");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(mConnection);
            isBound = false;
        }
    }
}