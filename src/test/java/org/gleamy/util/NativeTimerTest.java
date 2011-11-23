package org.gleamy.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NativeTimerTest {

    NativeTimer timer;

    @Before
    public void before() throws Exception {
        this.timer = new NativeTimer();
    }

    @After
    public void after() throws Exception {
        this.timer.stop();
    }

    @Test
    public void testScheduleAt() throws Exception {
        Date after100msec = new Date(System.currentTimeMillis() + 100);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Long> executedAt = new AtomicReference<Long>();
        timer.schedule(after100msec, new Function() {
            public void apply() {
                executedAt.set(System.currentTimeMillis());
                latch.countDown();
            }
        });

        latch.await();
        assertTrue(after100msec.getTime() <= executedAt.get());
    }

    @Test
    public void testScheduleAtFixedRate() throws Exception {
        Date after1sec = new Date(System.currentTimeMillis() + 1000);
        int millis100 = 100;

        final CountDownLatch latch = new CountDownLatch(3);
        final List<Long> executedAt = new ArrayList<Long>();
        timer.schedule(after1sec, millis100, TimeUnit.MILLISECONDS, new Function() {
            public void apply() {
                executedAt.add(System.currentTimeMillis());
                latch.countDown();
            }
        });

        latch.await();

        assertThat(executedAt.size(), is(3));
        assertTrue(after1sec.getTime() +   0 <= executedAt.get(0));
        assertTrue(after1sec.getTime() + 100 <= executedAt.get(1));
        assertTrue(after1sec.getTime() + 200 <= executedAt.get(2));
    }
}
