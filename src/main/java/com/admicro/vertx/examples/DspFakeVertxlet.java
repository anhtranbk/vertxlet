package com.admicro.vertx.examples;

import com.admicro.vertx.core.HttpVertxlet;
import com.admicro.vertx.core.VertxletRequestMapping;
import com.vccorp.ssp.adaptor.json.JSONTokener;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import ssp.vccorp.ssp.adaptor.openrtbModel.BidRequest;

import java.nio.charset.Charset;

@VertxletRequestMapping(url = {"/dspFake/dsp"})
public class DspFakeVertxlet extends HttpVertxlet {

    @Override
    protected void init() throws Exception {
//        Thread.sleep(1000);
    }

    @Override
    protected void destroy() throws Exception {
//        Thread.sleep(1000);
    }

    @Override
    protected void doGet(RoutingContext routingContext) throws Exception {
        routingContext.response().end("Hello World!");
    }

    @Override
    protected void doPost(RoutingContext routingContext) throws Exception {
        final HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");
        response.putHeader("charset", "utf-8");

        routingContext.request().bodyHandler(buffer -> {
            String body = new String(buffer.getBytes(), Charset.forName("utf-8"));
            executingHeavyTask(() -> {
                BidRequest br = new BidRequest(new JSONTokener(body));
                return "{\"id\":\"1234567890\",\"seatbid\":[{\"bid\":[{\"id\":\"f528fff6-b107-4df3-a018-ec78a7901a7e-1399644861923-0\",\"impid\":\"" + br.imp.get(0).id
                        + "\",\"price\":0.43,\"adid\":\"314\",\"adm\":\"%3C!DOCTYPE%20html%20PUBLIC%20%5C%22-%2F%2FW3C%2F%2FDTD%20XHTML%201.0%20Transitional%2F%2FEN%5C%22%20%5C%22http%3A%2F%2Fwww.w3.org%2FTR%2Fxhtml1%2FDTD%2Fxhtml1-transitional.dtd%5C%22%3E%3Chtml%20xmlns%3D%5C%22http%3A%2F%2Fwww.w3.org%2F1999%2Fxhtml%5C%22%20xml%3Alang%3D%5C%22en%5C%22%20lang%3D%5C%22en%5C%22%3E...%3C%2Fhtml%3E\",\"adomain\":[\"advertiserdomain.com\"],\"iurl\":\"http://adserver.com/pathtosampleimage\",\"cid\":\"campaign111\"}],\"seat\":\"512\"}],\"bidid\":\"abc1123\",\"cur\":\"USD\"}";
            }, result -> {
                if (result.succeeded()) {
                    response.end(result.result());
                } else {
                    routingContext.fail(result.cause());
                }
            });
        });
    }
}
