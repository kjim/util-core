package org.gleamy.util.concurrent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.gleamy.util.Function0;
import org.gleamy.util.ScheduledThreadPoolTimer;
import org.gleamy.util.Timer;
import org.junit.Test;

public class RichFutureTest {

    @Test
    public void testWithin() throws Exception {
        Timer timer = new ScheduledThreadPoolTimer();
        try {
            // light task
            RichFuture<Boolean> light = RichFuture.newInstance(new Function0<Boolean>() {
                public Boolean apply() {
                    return true;
                }
            });
            RichFuture<Boolean> monitorLight = light.within(timer, 500, TimeUnit.MILLISECONDS);
            assertThat(light.apply(), is(true));
            try {
                monitorLight.apply(200, TimeUnit.MILLISECONDS);
                fail("monitor task is ended?");
            }
            catch (TimeoutException e) {
                // monitor is never end task
                assertTrue(true);
            }

            // heavy task
            Promise<Boolean> heavy = new Promise<Boolean>();

            RichFuture<Boolean> monitorHeavy = heavy.within(timer, 200, TimeUnit.MILLISECONDS);
            try {
                monitorHeavy.apply();
                fail("monitor task is ended?");
            }
            catch (TimeoutRuntimeException e) {
                assertThat(e.getCause(), is(instanceOf(TimeoutException.class)));
            }
            assertThat(heavy.isDefined(), is(false));
            assertThat(heavy.isCancelled(), is(false));
        }
        finally {
            timer.stop();
        }
    }
}
