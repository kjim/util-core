package org.gleamy.util.concurrent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class NamedPoolThreadFactoryTest {

    private class NOP implements Runnable {
        public void run() {
        }
    }

    @Test
    public void testNoDaemonByDefault() throws Exception {
        NamedPoolThreadFactory factory = new NamedPoolThreadFactory("timer");
        Thread thread1 = factory.newThread(new NOP());
        Thread thread2 = factory.newThread(new NOP());
        Thread thread3 = factory.newThread(new NOP());

        assertThat(thread1.getName(), is("timer-1"));
        assertThat(thread2.getName(), is("timer-2"));
        assertThat(thread3.getName(), is("timer-3"));

        assertThat(thread1.isDaemon(), is(false));
        assertThat(thread2.isDaemon(), is(false));
        assertThat(thread3.isDaemon(), is(false));
    }

    @Test
    public void testMakeDaemonThread() throws Exception {
        boolean makeDaemonThread = true;
        NamedPoolThreadFactory factory = new NamedPoolThreadFactory("timer", makeDaemonThread);
        Thread thread = factory.newThread(new NOP());

        assertThat(thread.getName(), is("timer-1"));
        assertThat(thread.isDaemon(), is(true));
    }
}
