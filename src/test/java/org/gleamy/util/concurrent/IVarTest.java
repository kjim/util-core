package org.gleamy.util.concurrent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.gleamy.util.Function1;
import org.gleamy.util.Unit;
import org.junit.Test;

public class IVarTest {

    @Test
    public void testBasicOps() {
        IVar<String> ivar = new IVar<String>();
        assertThat(ivar.isDefined(), is(false));

        final Map<String, String> ran = new HashMap<String, String>();
        ivar.get(new Function1<String, Unit>() {
            @Override
            public Unit apply(String in) {
                ran.put("fun1", in);
                return Unit.getInstance();
            }});
        ivar.get(new Function1<String, Unit>() {
            @Override
            public Unit apply(String in) {
                ran.put("fun2", in);
                return Unit.getInstance();
            }});
        ivar.get(new Function1<String, Unit>() {
            @Override
            public Unit apply(String in) {
                ran.put("fun3", in);
                return Unit.getInstance();
            }});

        assertThat(ran.size(), is(0));

        assertThat(ivar.set("foo"), is(true));
        assertThat(ran.size(), is(3));
        assertThat(ran.get("fun1"), is("foo"));
        assertThat(ran.get("fun2"), is("foo"));
        assertThat(ran.get("fun3"), is("foo"));

        assertThat(ivar.set("bar"), is(false));
        assertThat(ran.size(), is(3));
        assertThat(ran.get("fun1"), is("foo"));
        assertThat(ran.get("fun2"), is("foo"));
        assertThat(ran.get("fun3"), is("foo"));

        ivar.get(new Function1<String, Unit>() {
            @Override
            public Unit apply(String in) {
                ran.put("fun4", in);
                return Unit.getInstance();
            }});
        assertThat(ran.size(), is(4));
        assertThat(ran.get("fun4"), is("foo"));
    }

    private static class Get implements Function1<String, Unit> {
        private String id;
        private Map<String, String> container;

        public Get(String id, Map<String, String> container) {
            this.id = id;
            this.container = container;
        }

        @Override
        public Unit apply(String in) {
            container.put(id, in);
            return Unit.getInstance();
        }
    }

    private static class Set implements Callable<Unit> {
        private String setval;
        private IVar<String> ivar;

        public Set(String setval, IVar<String> ivar) {
            this.setval = setval;
            this.ivar = ivar;
        }

        @Override
        public Unit call() throws Exception {
            Thread.sleep(10);
            ivar.set(setval);
            return Unit.getInstance();
        }
    }

    @Test
    public void testConcurrent() throws Exception {
        int count = 30;

        IVar<String> ivar = new IVar<String>();
        Map<String, String> ran = new HashMap<String, String>();

        for (int i = 0; i < count; i++) {
            ivar.get(new Get(String.format("%02d", i), ran));
        }

        ExecutorService executors = Executors.newFixedThreadPool(count);

        List<Set> sets = new ArrayList<Set>();
        for (int i = 0; i < count; i++) {
            sets.add(new Set(String.format("val%02d", i), ivar));
        }

        List<Future<Unit>> setFutures = executors.invokeAll(sets);
        for (Future<Unit> f : setFutures) {
            f.get();
        }

        String expected = ran.entrySet().iterator().next().getValue();
        for (Map.Entry<String, String> e : ran.entrySet()) {
            assertThat(e.getValue(), is(expected));
        }

        executors.shutdown();
    }

    @Test
    public void testApplyIsBlockingOperation() throws Exception {
        final IVar<String> ivar = new IVar<String>();

        ExecutorService executors = Executors.newCachedThreadPool();
        Future<String> neverStop = executors.submit(new Callable<String>() {
            public String call() throws Exception {
                ivar.apply();
                return "success";
            }
        });

        try {
            neverStop.get(1, TimeUnit.MILLISECONDS);
            fail("future finished");
        }
        catch (TimeoutException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testTimedout() throws Exception {
        IVar<String> ivar = new IVar<String>();

        String value = ivar.apply(1, TimeUnit.MILLISECONDS);
        assertThat(value, is(nullValue()));

        ivar.set("result");

        value = ivar.apply(1, TimeUnit.MILLISECONDS);
        assertThat(value, is("result"));
    }

}
