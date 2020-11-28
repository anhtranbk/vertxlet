package examples;

import com.vertxlet.core.Server;
import com.vertxlet.util.Config;

public class ExampleRunner {

    public static void main(String[] args) {
        Server server = Server.builder()
                .applicationConfig(new Config())
                .build();
        server.start();
    }
}
