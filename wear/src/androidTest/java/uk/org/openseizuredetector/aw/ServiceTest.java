/*
 * Copyright 2015, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.openseizuredetector.aw;

import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
/*
/**
 * JUnit4 test that uses a {@link ServiceTestRule} to interact with a bound service.
 * <p>
 * {@link ServiceTestRule} is a JUnit rule that provides a
 * simplified mechanism to start and shutdown your service before
 * and after the duration of your test. It also guarantees that the service is successfully
 * connected when starting (or binding to) a service. The service can be started
 * (or bound) using one of the helper methods. It will automatically be stopped (or unbound) after
 * the test completes and any methods annotated with
 * <a href="http://junit.sourceforge.net/javadoc/org/junit/After.html"><code>After</code></a> are
 * finished.
 * <p>
 * Note: This rule doesn't support {@link android.app.IntentService} because it's automatically
 * destroyed when {@link androidx.core.content.IntentCompat(androidx.core.content.IntentCompat)} finishes
 * all outstanding commands. So there is no guarantee to establish a successful connection
 * in a timely manner.
 */

public class ServiceTest {
    @Rule
    //public static final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public static void testWithBoundService() throws TimeoutException {
        // Create the service Intent.
        Intent serviceIntent =
                new Intent(getApplicationContext(), AWSdService.class);

        // Data can be passed to the service via the Intent.
        serviceIntent.setData(Uri.parse("Start"));

        // Bind the service and grab a reference to the binder.
        //IBinder binder = mServiceRule.bindService(serviceIntent);

        // Get the reference to the service, or you can call public methods on the binder directly.
        //AWSdService service = ((AWSdService.SdBinder) binder).getService();

        // Verify that the service is working correctly.
        //assertThat(service.getRandomInt(), is(any(Integer.class)));
    }
}
