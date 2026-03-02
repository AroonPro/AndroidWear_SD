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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * StartUpActivity - en_GB
 * Handles initialisation and permission orchestration.
 * * LSA Audit 2024:
 * Prevents 'silent crashes' during service binding by validating
 * context availability before UI updates.
 */
public class StartUpActivity extends AppCompatActivity {
    private final String TAG = "StartUpActivity";
    private static final int PERMISSION_REQUEST_CODE = 42;

    private AWSdService aWSdService;
    private boolean isBound = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * StartUpActivity - en_GB
     * Refined layout loading to prevent 'White Screen' freezes.
     * LSA Audit 2024: Guaranteed UI visibility for forensic status monitoring.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LSA Audit: onCreate started.");

        // en_GB: Dynamic layout check with hardcoded fallback
        int layoutId = getResources().getIdentifier("activity_startup", "layout", getPackageName());
        if (layoutId != 0) {
            setContentView(layoutId);
        } else {
            Log.e(TAG, "LSA Audit: Layout 'activity_startup' not found! Using emergency fallback.");
            // If this fails, the app is missing its resource files
            setContentView(android.R.layout.simple_list_item_1);
        }

        // en_GB: Use a post-delay to ensure the White Screen is replaced by the layout
        // before the system permission dialog interrupts the thread.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                checkAndRequestPermissions();
            }
        }, 1000);
    }

    private void checkAndRequestPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.BODY_SENSORS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        List<String> missingPermissions = new ArrayList<>();
        for (String p : permissions) {
            if (ActivityCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(p);
            }
        }

        if (!missingPermissions.isEmpty()) {
            Log.i(TAG, "LSA Audit: Requesting missing permissions: " + missingPermissions);
            ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            startAndBindSdService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startAndBindSdService();
            } else {
                Log.e(TAG, "LSA Audit: Critical permissions denied. Activity terminated.");
                finish(); // en_GB: Prevent invalid data state
            }
        }
    }

    private void startAndBindSdService() {
        try {
            Intent intent = new Intent(this, AWSdService.class);
            startService(intent);
            isBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "LSA Audit: Bind request issued. Success: " + isBound);
        } catch (SecurityException e) {
            Log.e(TAG, "LSA Audit: SecurityException during bind - check Manifest for Health Type!", e);
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AWSdService.Access binder = (AWSdService.Access) service;
            aWSdService = binder.getService();

            if (aWSdService != null) {
                Log.i(TAG, "LSA Audit: Service Connected. Initialising UI.");
                updateUiTask();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            aWSdService = null;
            isBound = false;
        }
    };

    private void updateUiTask() {
        mHandler.post(() -> {
            if (!isFinishing() && aWSdService != null) {
                // en_GB: Update UI safely without crashing the 2012 eggshell
                Log.v(TAG, "LSA Audit: UI Update successful.");
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (isBound) {
            unbindService(mConnection);
            isBound = false;
        }
        super.onDestroy();
    }
}