package com.admicro.vertxlet.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskRunner {

    private static Logger _logger = LoggerFactory.getLogger(TaskRunner.class);

    public static <T> void loopParallel(RunnableFuture<T> rf, int count, Handler<AsyncResult<T>> handler) {
        if (count <= 0) {
            _logger.warn("No tasks to run");
            handler.handle(Future.succeededFuture());
            return;
        }

        List<RunnableFuture<T>> rfs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rfs.add(rf);
        }
        executeParallel(rfs, handler);
    }

    public static <T> void loopSequence(RunnableFuture<T> rf, int count, Handler<AsyncResult<T>> handler) {
        if (count <= 0) {
            _logger.warn("No tasks to run");
            handler.handle(Future.succeededFuture());
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
                    TaskRunner.loopSequence(rf, count - 1, handler);
                }
            }
        });
        rf.run(fut);
    }

    public static <T> void executeParallel(List<RunnableFuture<T>> rfs, Handler<AsyncResult<T>> handler) {
        if (rfs.isEmpty()) {
            _logger.warn("No tasks to run");
            handler.handle(Future.succeededFuture());
            return;
        }

        AtomicInteger remainTasks = new AtomicInteger(rfs.size());

        Future<T> future = Future.future();
        future.setHandler(handler);

        for (RunnableFuture<T> rf : rfs) {
            Future<T> fut = Future.future();
            fut.setHandler(ar -> {
                if (!future.isComplete()) {
                    if (ar.failed()) {
                        future.fail(ar.cause());
                    } else if (remainTasks.decrementAndGet() == 0) {
                        future.complete();
                    }
                }
            });
            rf.run(fut);
        }
    }
}
