package org.gleamy.util.concurrent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gleamy.util.CancellableSink;
import org.gleamy.util.Function;
import org.junit.Test;

public class PromiseTest {

    @Test
    public void testInitialStatus() throws Exception {
        Promise<String> promise = new Promise<String>();
        assertThat(promise.isCancelled(), is(false));
        assertThat(promise.isDefined(), is(false));
    }

    @Test
    public void testCancellable() throws Exception {
        Promise<String> promise = new Promise<String>();
        assertThat(promise.isCancelled(), is(false));
        assertThat(promise.isDefined(), is(false));

        promise.cancel();
        assertThat(promise.isCancelled(), is(true));
        assertThat(promise.isDefined(), is(false));

        promise.cancel();
        assertThat(promise.isCancelled(), is(true));
        assertThat(promise.isDefined(), is(false));
    }

    @Test
    public void testSetValue() throws Exception {
        Promise<String> promise = new Promise<String>();

        promise.setValue("success");
        assertThat(promise.isDefined(), is(true));
        assertThat(promise.apply(), is("success"));

        try {
            promise.setValue("set again");
            fail("success to set");
        }
        catch (ImmutableRuntimeException e) {
            assertTrue(true);
        }

        assertThat(promise.apply(), is("success"));
    }

    @Test
    public void testSetException() throws Exception {
        Promise<String> promise = new Promise<String>();

        promise.setException(new RuntimeException("error"));
        assertThat(promise.isDefined(), is(true));

        try {
            promise.apply();
            fail("success to compute");
        }
        catch (RuntimeException e) {
            assertThat(e.getMessage(), is("error"));
        }

        try {
            promise.setException(new RuntimeException("again"));
            fail("success to set");
        }
        catch (ImmutableRuntimeException e) {
            assertTrue(true);
        }

        try {
            promise.apply();
            fail("success to compute");
        }
        catch (RuntimeException e) {
            assertThat(e.getMessage(), is("error"));
        }
    }

    @Test
    public void testComputeTimeout() throws Exception {
        Promise<String> promise = new Promise<String>();
        assertThat(promise.isDefined(), is(false));

        try {
            promise.get(1, TimeUnit.MILLISECONDS);
            fail("no timeout");
        }
        catch (TimeoutException e) {
            assertTrue(true);
        }

        try {
            promise.apply(1, TimeUnit.MILLISECONDS);
            fail("no timeout");
        }
        catch (TimeoutException e) {
            assertTrue(true);
        }

        assertThat(promise.isDefined(), is(false));
    }

    @Test
    public void testSuccessToComputeResult() throws Exception {
        final Promise<String> promise = new Promise<String>();
        final String successMark = "success";

        ExecutorService worker = Executors.newCachedThreadPool();

        Future<String> retriever = worker.submit(new Callable<String>() {
            public String call() throws Exception {
                return promise.apply();
            }
        });

        long retrieveStart = System.currentTimeMillis();
        worker.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                Thread.sleep(10); // wait ten millis

                promise.setValue(successMark);
                return true;
            }
        });

        String result = retriever.get();
        long retrieveStop = System.currentTimeMillis();

        assertThat(result, is(successMark));

        long timeElapsed = retrieveStop - retrieveStart;
        assertTrue(10 <= timeElapsed);

        worker.shutdown();
    }

    @Test
    public void testLinkCancelEvent() throws Exception {
        final AtomicBoolean canceled = new AtomicBoolean(false);

        Promise<Boolean> promise = new Promise<Boolean>();

        // promise link to the cancellable-sink object
        promise.linkTo(new CancellableSink(new Function() {
            public void apply() {
                canceled.compareAndSet(false, true);
            }
        }));

        assertThat(canceled.get(), is(false));

        promise.cancel();

        assertThat(canceled.get(), is(true));
    }

    @Test
    public void testToNativeFuture() throws Exception {
        final Promise<String> promise = new Promise<String>();
        Future<String> future = promise.toNativeFuture();

        assertThat(future.isCancelled(), is(false));
        assertThat(future.isDone(), is(false));

        try {
            future.get(1, TimeUnit.MILLISECONDS);
            fail("success to future get");
        }
        catch (TimeoutException e) {
            assertTrue(true);
        }

        final String successLabel = "success";

        ExecutorService worker = Executors.newCachedThreadPool();
        worker.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                Thread.sleep(10);

                promise.setValue(successLabel);
                return true;
            }
        });

        String result = future.get();
        assertThat(result, is(successLabel));
        assertThat(future.isDone(), is(true));

        assertThat(future.cancel(false), is(false));

        Promise<String> cancellable = new Promise<String>();
        Future<String> cancellableFuture = cancellable.toNativeFuture();

        cancellable.cancel();
        assertThat(cancellableFuture.isDone(), is(true));
        assertThat(cancellableFuture.isCancelled(), is(true));

        try {
            cancellableFuture.get();
            fail("success to get");
        }
        catch (CancellationException e) {
            assertTrue(true);
        }

        try {
            cancellableFuture.get(1, TimeUnit.MILLISECONDS);
            fail("success to get");
        }
        catch (CancellationException e) {
            assertTrue(true);
        }
    }
}
