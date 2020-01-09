package examples;

import com.admicro.vertxlet.core.Server;

public class ExampleRunner {

    public static void main(String[] args) {
        if (args.length > 0) {
            Server.start(args[0]);
        } else {
            Server.start();
        }
    }
}
