package org.gleamy.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class CancellableSinkTest {

    private static class Nop implements Function {
        public void apply() {
            // NOP
        }
    }

    private static class CountCall implements Function {
        private AtomicInteger count = new AtomicInteger();
        public void apply() {
            count.incrementAndGet();
        }
        public int get() {
            return count.get();
        }
    }

    @Test
    public void testCancel() throws Exception {
        CountCall counter = new CountCall();

        CancellableSink cancellable = new CancellableSink(counter);
        assertThat(cancellable.isCancelled(), is(false));
        assertThat(counter.get(), is(0));

        cancellable.cancel();

        assertThat(cancellable.isCancelled(), is(true));
        assertThat(counter.get(), is(1));

        // cancel once more
        cancellable.cancel();

        // but counter is not called
        assertThat(cancellable.isCancelled(), is(true));
        assertThat(counter.get(), is(1));
    }

    @Test
    public void testLinkToNotSupported() throws Exception {
        CancellableSink front = new CancellableSink(new Nop());
        CancellableSink back = new CancellableSink(new Nop());

        try {
            front.linkTo(back);
            fail("linkTo is supported?");
        }
        catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
    }
}
