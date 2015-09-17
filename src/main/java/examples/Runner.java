package examples;

import com.admicro.vertxlet.core.HttpServer;
import com.admicro.vertxlet.core.VertxletException;

public class Runner {

    public static void main(String[] args) {
        try {
            HttpServer.startNew();
        } catch (VertxletException e) {
            e.printStackTrace();
        }
    }
}
