package com.admicro.vertxlet.utils;

import com.admicro.vertxlet.core.RunnableFuture;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskRunner {

    public static <T> void loopParallelTasks(RunnableFuture<T> rf, int count,
                                             Handler<AsyncResult<Void>> handler) {
        if (count <= 0) {
            handler.handle(Future.failedFuture("No tasks to run"));
            return;
        }

        List<RunnableFuture<T>> rfs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rfs.add(rf);
        }
        runListParallelTasks(rfs, handler);
    }

    public static <T> void loopSequenceTasks(RunnableFuture<T> rf, int count,
                                             Handler<AsyncResult<T>> handler) {
        if (count <= 0) {
            handler.handle(Future.failedFuture("No tasks to run"));
            return;
        }

        Future<T> fut = Future.future();
        fut.setHandler(ar -> {
            if (ar.failed()) {
                handler.handle(Future.failedFuture(ar.cause()));
            } else {
                if (count == 1) {
                    handler.handle(Future.succeededFuture());
                } else {
                    TaskRunner.loopSequenceTasks(rf, count - 1, handler);
                }
            }
        });
        rf.run(fut);
    }

    public static <T> void runListParallelTasks(List<RunnableFuture<T>> rfs,
                                                Handler<AsyncResult<Void>> handler) {
        if (rfs.isEmpty()) {
            handler.handle(Future.failedFuture("No tasks to run"));
            return;
        }

        AtomicInteger remainTasks = new AtomicInteger(rfs.size());
        AtomicBoolean finish = new AtomicBoolean(false);

        for (RunnableFuture<T> rf : rfs) {
            Future<T> fut = Future.future();
            fut.setHandler(ar -> {
                if (!finish.get()) {
                    if (ar.failed()) {
                        finish.set(true);
                        handler.handle(Future.failedFuture(ar.cause()));
                    } else {
                        if (remainTasks.decrementAndGet() == 0) {
                            finish.set(true);
                            handler.handle(Future.succeededFuture());
                        }
                    }
                }
            });
            rf.run(fut);
        }
    }
}
