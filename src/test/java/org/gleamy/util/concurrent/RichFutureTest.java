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
            RichFuture<Boolean> timeoutEnabledLight = light.within(timer, 500, TimeUnit.MILLISECONDS);
            assertThat(timeoutEnabledLight.apply(), is(true));

            // heavy task
            Promise<Boolean> heavy = new Promise<Boolean>();

            RichFuture<Boolean> timeoutEnabledHeavy = heavy.within(timer, 200, TimeUnit.MILLISECONDS);
            try {
                timeoutEnabledHeavy.apply();
                fail("monitor task is ended?");
            }
            catch (TimeoutRuntimeException e) {
                assertThat(e.getCause(), is(instanceOf(TimeoutException.class)));
            }
            assertThat(heavy.isDefined(), is(false));
            assertThat(heavy.isCancelled(), is(false));

            assertThat(timeoutEnabledHeavy.isDefined(), is(true));
            assertThat(timeoutEnabledHeavy.isCancelled(), is(false));

            timeoutEnabledHeavy.cancel();

            assertThat(timeoutEnabledHeavy.isDefined(), is(true));
            assertThat(timeoutEnabledHeavy.isCancelled(), is(true));

            assertThat(heavy.isDefined(), is(false));
            assertThat(heavy.isCancelled(), is(false));

            heavy.cancel();

            assertThat(heavy.isDefined(), is(false));
            assertThat(heavy.isCancelled(), is(true));
        }
        finally {
            timer.stop();
        }
    }
}
