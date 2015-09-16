package com.admicro.vertx.utils;

import com.admicro.vertx.core.RunnableFuture;
import io.vertx.core.Future;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskRunner {

    public static <T> void loopParallelTasks(RunnableFuture<T> rf, int count, Future<Void> future) {
        if (count <= 0) {
            future.complete();
            return;
        }

        AtomicInteger ai = new AtomicInteger(count);

        for (int i = 0; i < ai.get(); i++) {
            Future<T> fut = Future.future();
            fut.setHandler(ar -> {
                if (!future.isComplete()) {
                    if (ar.failed()) {
                        future.fail(ar.cause());
                    } else {
                        if (ai.decrementAndGet() == 0) {
                            future.complete();
                        }
                    }
                }
            });
            rf.run(fut);
        }
    }

    public static <T> void loopSequenceTasks(RunnableFuture<T> rf, int count, Future<Void> future) {
        if (count <= 0) {
            future.complete();
            return;
        }

        Future<T> fut = Future.future();
        fut.setHandler(ar -> {
            if (!future.isComplete()) {
                if (ar.failed()) {
                    future.fail(ar.cause());
                } else {
                    if (count == 1) future.complete();
                    else {
                        TaskRunner.loopSequenceTasks(rf, count - 1, future);
                    }
                }
            }
        });
        rf.run(fut);
    }

    public static <T> void runParallelTasks(List<RunnableFuture<T>> rfs, Future<Void> future) {
        final int size = rfs.size();
        if (size == 0) {
            future.complete();
        } else {
            AtomicInteger ai = new AtomicInteger(size);
            for (RunnableFuture<T> rf : rfs) {
                Future<T> fut = Future.future();
                fut.setHandler(ar -> {
                    if (! future.isComplete()) {
                        if (ar.failed()) {
                            future.fail(ar.cause());
                        } else {
                            if (ai.decrementAndGet() == 0) {
                                future.complete();
                            }
                        }
                    }
                });
                rf.run(fut);
            }
        }
    }
}
