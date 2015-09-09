package examples;

import com.admicro.vertx.core.HttpServer;
import com.admicro.vertx.core.VertxletException;

public class Runner {

    public static void main(String[] args) {
        try {
            HttpServer.startNew();
        } catch (VertxletException e) {
            e.printStackTrace();
        }
    }
}
