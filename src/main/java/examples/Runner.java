package examples;

import com.admicro.vertxlet.core.Server;
import com.admicro.vertxlet.core.VertxletException;

public class Runner {

    public static void main(String[] args) {
        try {
            Server.startNew();
        } catch (VertxletException e) {
            e.printStackTrace();
        }
    }
}
