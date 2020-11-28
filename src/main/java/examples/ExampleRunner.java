package examples;

import com.github.vertxlet.core.Server;
import com.github.vertxlet.util.Config;

public class ExampleRunner {

    public static void main(String[] args) {
        Server server = Server.builder()
                .applicationConfig(new Config())
                .build();
        server.start();
    }
}
