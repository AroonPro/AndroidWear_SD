package uk.org.openseizuredetector.aw;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import uk.org.openseizuredetector.SdData;
import uk.org.openseizuredetector.SdServiceConnection;
import uk.org.openseizuredetector.aw.R;

/**
 * StartUpActivity - Main UI for Wear OS
 * Handles the connection to AWSdService and observes LiveData updates.
 */
public class StartUpActivity extends AppCompatActivity {
    private static final String TAG = "StartUpActivity";

    // The connection object that holds the reference to our Service
    public SdServiceConnection mConnection;
    private TextView mStatusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up); // Ensure this layout exists in wear/res/layout

        mStatusTv = findViewById(R.id.statusTv);
        mConnection = new SdServiceConnection(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Bind to the service and start observing
        mConnection.doBindService();
        setupServiceObserver();
    }

    /**
     * Sets up the LiveData observer.
     * This replaces the old 'signalChangedData' manual triggers.
     */
    private void setupServiceObserver() {
        if (mConnection != null && mConnection.mSdService != null) {
            // Using the standard AndroidX Observer interface
            mConnection.mSdService.serviceLiveData.observe(this, new Observer<SdData>() {
                @Override
                public void onChanged(SdData data) {
                    onChangedObserver(data);
                }
            });
        }
    }

    /**
     * Logic to update the UI when SdData changes.
     */
    private void onChangedObserver(SdData data) {
        if (data == null) return;

        runOnUiThread(() -> {
            StringBuilder sb = new StringBuilder();
            // CAST naar (int) om de 'lossy conversion' error op te lossen
            sb.append("Status: ").append(getAlarmStateName((int) data.alarmState));
            sb.append("\nHR: ").append(data.mHR).append(" bpm");

            if (mConnection != null && mConnection.mSdService != null) {
                sb.append("\n📱 Bat: ").append(mConnection.mSdService.serverBatteryPct).append("%");
            }

            mStatusTv.setText(sb.toString());
        });
    }

    private String getAlarmStateName(int state) {
        switch (state) {
            case 0: return "OK";
            case 1: return "WARNING";
            case 2: return "ALARM";
            default: return "UNKNOWN";
        }
    }

    /* --- UI Button Handlers (Fixes 'cannot find symbol' errors) --- */

    public void onClearAlarmClicked() {
        if (mConnection.mBound && mConnection.mSdService != null) {
            mConnection.mSdService.ClearAlarmCount();
        }
    }

    public void onHelpClicked() {
        if (mConnection.mBound && mConnection.mSdService != null) {
            mConnection.mSdService.handleSendingHelp();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Optional: unbind if you don't want background updates while activity is hidden
        // mConnection.doUnbindService();
    }
}