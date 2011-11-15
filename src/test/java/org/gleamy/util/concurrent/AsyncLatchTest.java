package org.gleamy.util.concurrent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.gleamy.util.Function;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

public class AsyncLatchTest {

    @Mocked Function release1;
    @Mocked Function release2;
    @Mocked Function release3;

    @Test
    public void testInitialCountIsZero() {
        AsyncLatch latch = new AsyncLatch();
        assertThat(latch.getCount(), is(0));

        latch.incr();
        assertThat(latch.getCount(), is(1));

        latch.decr();
        assertThat(latch.getCount(), is(0));

        try {
            // count is zero
            latch.decr();
            fail();
        }
        catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testAwait() throws Exception {
        new Expectations() {{
            release1.apply();
            release2.apply();
            release3.apply();
        }};

        // guard count: 2
        final AsyncLatch latch = new AsyncLatch(2);
        latch.await(release1);
        latch.await(release2);
        latch.await(release3);

        new Verifications() {{
            List<?> waiters = getField(latch, List.class);
            assertThat(waiters.size(), is(3));
        }};

        // guard count: 2 -> 1
        latch.decr();

        new Verifications() {{
            List<?> waiters = getField(latch, List.class);
            assertThat(waiters.size(), is(3));
        }};

        // guard count: 1 -> 0 ! fire
        latch.decr();

        new Verifications() {{
            List<?> waiters = getField(latch, List.class);
            assertThat(waiters.isEmpty(), is(true));
        }};
    }
}
