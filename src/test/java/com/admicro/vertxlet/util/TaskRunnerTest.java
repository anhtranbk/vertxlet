package com.admicro.vertxlet.util;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(VertxUnitRunner.class)
public class TaskRunnerTest {

    private Vertx vertx;

    @Before
    public void setup(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start(io.vertx.core.Future<Void> startFuture) throws Exception {
                startFuture.complete();
            }
        }, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testLoopParallel(TestContext context) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        Iterator<Integer> iterator = list.iterator();
        AtomicInteger ai = new AtomicInteger(0);

        TaskRunner.loopParallel(fut -> {
            int val = iterator.next();
            ai.set(ai.get() + val);
            fut.complete();
        }, list.size(), ar -> {
            final Async async = context.async();
            if (ar.failed()) {
                context.fail(ar.cause());
            } else {
                context.assertEquals(ai.get(), 6);
            }
            async.complete();
        });
    }

    @Test
    public void testExecuteParallel(TestContext context) {
        AtomicInteger ai = new AtomicInteger(0);
        List<RunnableFuture<Void>> rfs = new ArrayList<>();
        rfs.add(fut -> {
            ai.set(ai.get() + 3);
            fut.complete();
        });
        rfs.add(fut -> {
            ai.set(ai.get() + 4);
            fut.complete();
        });
        rfs.add(fut -> {
            ai.set(ai.get() + 5);
            fut.complete();
        });

        TaskRunner.executeParallel(rfs, ar -> {
            if (ar.failed()) {
                context.fail(ar.cause());
            } else {
                context.assertEquals(ai.get(), 12);
            }
        });
    }
}
