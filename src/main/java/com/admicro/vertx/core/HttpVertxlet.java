package com.admicro.vertx.core;

import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;

/**
 * @author anhtn
 */
public class HttpVertxlet implements Vertxlet {

    private Vertx vertx;
    private Verticle verticle;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void setContext(Vertx vertx, Verticle verticle) {
        this.vertx = vertx;
        this.verticle = verticle;
    }

    @Override
    public void init(Future<Void> future) {
        init();
        future.complete();
    }

    @Override
    public void destroy(Future<Void> future)  {
        destroy();
        future.complete();
    }

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/server_load";
    static final String USER = "root";
    static final String PASS = "root";

    @Override
    public void handle(RoutingContext routingContext) {
        if (getClass().getAnnotation(VertxServlet.class).usingDatabase()) {
            JsonObject config = new JsonObject()
                    .put("url", DB_URL)
                    .put("driver_class", JDBC_DRIVER)
                    .put("user", USER)
                    .put("password", PASS)
                    .put("max_pool_size", 50);

            JDBCClient client = JDBCClient.createShared(vertx, config);

            client.getConnection(result -> {
                if (result.failed()) {
                    routingContext.fail(result.cause());
                } else {
                    SQLConnection con = result.result();
                    routingContext.put("db", con);
                    routingContext.addHeadersEndHandler(future -> con.close(v -> {
                        if (v.failed()) {
                            future.fail(v.cause());
                        } else {
                            future.complete();
                        }
                    }));
                    routeByMethod(routingContext);
                }
            });
        } else {
            routeByMethod(routingContext);
        }
    }

    @Override
    public final Vertx getVertx() {
        return vertx;
    }

    public final Verticle getVerticle() {
        return verticle;
    }

    /**
     * Like {@link #init(Future<Void>)} but simple and safely for synchronous tasks
     */
    protected void init() {}

    /**
     * Like {@link #destroy(Future<Void>)} but simple and safely for synchronous tasks
     */
    protected void destroy() {}

    /**
     * Specify to handle Http GET request
     * @param routingContext the Http routing context
     */
    protected void doGet(RoutingContext routingContext) {
        routingContext.response().end();
    }

    /**
     * Specify to handle Http POST request
     * @param routingContext the Http routing context
     */
    protected void doPost(RoutingContext routingContext) {
        routingContext.response().end();
    }

    /**
     * Simple and safely to execute heavy tasks (blocking codes).<p>
     * Executes the blocking code in the handler {@code task} using a thread from the worker pool.<p>
     * When the code is complete the handler {@code handler} will be called<p>
     * @param task heavy task representing the blocking code to run
     * @param handler handle will be called when the heavy task execute done
     * @param <T> the type of the result
     */
    protected <T> void executingHeavyTask(AsyncTask<T> task, Handler<AsyncResult<T>> handler) {
        executingHeavyTask(task, handler, false);
    }

    /**
     * Simple and safely to execute heavy tasks (blocking codes).<p>
     * Executes the blocking code in the handler {@code task} using a thread from the worker pool.<p>
     * When the code is complete the handler {@code handler} will be called<p>
     * @param task heavy task representing the blocking code to run
     * @param handler handle will be called when the heavy task execute done
     * @param ordered if true then if executeBlocking is called several times on the same context,
     *                the executions for that context will be executed serially, not in parallel.
     *                if false then they will be no ordering guarantees
     * @param <T> the type of the result
     */
    protected <T> void executingHeavyTask(AsyncTask<T> task, Handler<AsyncResult<T>> handler, boolean ordered) {
        vertx.executeBlocking(future -> {
            try {
                T result = task.run();
                future.complete(result);
            } catch (Exception e) {
                future.fail(e);
            }
        }, ordered, handler);
    }

    /**
     * Executes some codes in event loop at time in the future
     * @param runnable representing the code to run
     */
    protected void post(Runnable runnable) {
        postDelay(runnable, 0);
    }

    /**
     * Executes some codes in event loop after {@code delay} milliseconds
     * @param runnable representing the code to run
     * @param delay the delay in milliseconds, after which the runnable will execute
     */
    protected void postDelay(Runnable runnable, long delay) {
        vertx.setTimer(delay, id -> runnable.run());
    }

    /**
     * Get SqlConnection instance if the vertxlet is declared with
     * {@link com.admicro.vertx.core.Vertxlet} is true
     * @param routingContext the current Http routing context instance
     * @return the Sql connection
     * @throws UnsupportedOperationException
     */
    protected final SQLConnection getSqlConnection(RoutingContext routingContext)
            throws UnsupportedOperationException {

        SQLConnection con = routingContext.get("db");
        if (con == null) {
            UnsupportedOperationException e = new UnsupportedOperationException(
                    "Vertxlet was not declared using database");
            logger.error(e.getMessage(), e);
            throw e;
        }

        return con;
    }

    private void routeByMethod(RoutingContext routingContext) {
        if (routingContext.request().method() == HttpMethod.GET) {
            doGet(routingContext);
        } else if (routingContext.request().method() == HttpMethod.POST) {
            doPost(routingContext);
        } else {
            UnsupportedOperationException e = new UnsupportedOperationException("Method not support");
            logger.error(e.getMessage(), e);
            throw e;
        }
    }
}