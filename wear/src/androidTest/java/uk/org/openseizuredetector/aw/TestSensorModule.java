package uk.org.openseizuredetector.aw;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.runner.RunWith;


@ExtendWith(Extension.class)
public class TestSensorModule {
    public TestSensorModule(){

    }
    Context context;
    Application application;
    Looper looper;
    Handler handler;
    OsdUtil util;
    AWSdService aWsdService;
    Intent sdServerIntent;
    //ServiceController<AWSdService> controller;
    SdServiceConnection sdServiceConnection;

    @Before
    public void initOsdUtil() {
        application = ApplicationProvider.getApplicationContext();
        context = application.getApplicationContext();
        looper = context.getMainLooper();
        handler = new Handler(looper);
        util = new OsdUtil(context,handler);
        sdServerIntent = new Intent(context,AWSdService.class)
                .setData(Constants.GLOBAL_CONSTANTS.mStartUri);
        sdServiceConnection = new SdServiceConnection(context);
    }

    @After
    public void removeUtil(){
        sdServiceConnection = null;
        util = null;
        handler = null;
        looper = null;
        context = null;
    }

}