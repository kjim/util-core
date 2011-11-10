package org.gleamy.util.concurrent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AsyncSemaphoreTest {

    ExecutorService executor;

    @Before
    public void before() throws Exception {
        this.executor = Executors.newCachedThreadPool();
    }

    @After
    public void after() throws Exception {
        this.executor.shutdownNow();
    }

    @Test
    public void test() throws Exception {
        final AsyncSemaphore semaphore = new AsyncSemaphore(3);

        // acquire is non-blocking
        final RichFuture<Permit> permitFuture1 = semaphore.acquire();
        final RichFuture<Permit> permitFuture2 = semaphore.acquire();
        final RichFuture<Permit> permitFuture3 = semaphore.acquire();
        final RichFuture<Permit> permitFuture4 = semaphore.acquire();
        final RichFuture<Permit> permitFuture5 = semaphore.acquire();

        assertThat(semaphore.getNumPermitsAvailable(), is(0));
        assertThat(semaphore.getNumWaiters(), is(2));

        Permit permit1 = permitFuture1.apply();
        Permit permit2 = permitFuture2.apply();
        Permit permit3 = permitFuture3.apply();

        Future<Permit> waitPermit4 = executor.submit(new Callable<Permit>() {
            public Permit call() throws Exception {
                return permitFuture4.apply();
            }
        });
        Future<Permit> waitPermit5 = executor.submit(new Callable<Permit>() {
            public Permit call() throws Exception {
                return permitFuture5.apply();
            }
        });

        // available permits is nothing. then future get will timeout.
        try {
            waitPermit4.get(10, TimeUnit.MILLISECONDS);
            fail("permit 4");
        }
        catch (TimeoutException e) {
            assertTrue(true);
        }
        try {
            waitPermit5.get(10, TimeUnit.MILLISECONDS);
            fail("permit 5");
        }
        catch (TimeoutException e) {
            assertTrue(true);
        }

        assertThat(semaphore.getNumPermitsAvailable(), is(0));
        assertThat(semaphore.getNumWaiters(), is(2));

        // release 2, then 2 available, then waiter acquire semaphore.
        permit1.release();
        permit2.release();

        assertThat(semaphore.getNumPermitsAvailable(), is(0));
        assertThat(semaphore.getNumWaiters(), is(0));

        Permit permit4 = waitPermit4.get(10, TimeUnit.MILLISECONDS);
        Permit permit5 = waitPermit5.get(10, TimeUnit.MILLISECONDS);

        assertThat(semaphore.getNumPermitsAvailable(), is(0));
        assertThat(semaphore.getNumWaiters(), is(0));

        permit3.release();
        permit4.release();
        permit5.release();

        assertThat(semaphore.getNumPermitsAvailable(), is(3));
        assertThat(semaphore.getNumWaiters(), is(0));
    }
}
