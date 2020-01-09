## What is vertxlet

Micro-library for easy, fast convert JEE Servlet/Spring-based application to [Vert.x](https://vertx.io) application

## Installation

Clone project:

`git clone https://github.com/tjeubaoit/vertxlet`

Make sure Maven has been installed on your workspace. Run the following command to install vertxlet to your Maven local repositories:

`mvn clean install`

## How to use

Define your request mapping in one of the following styles:

### Spring style Controllers

```java

@Controller("/user")
public class ControllerExamples {

    private static final Logger logger = LoggerFactory.getLogger(ControllerExamples.class);

    // Controller object created only once per Vertx context so you can keep
    // session in Controller's object properties
    private final Map<String, Long> users = new HashMap<>();

    @RequestMapping(path = {"/login/:user", "/authenticate/:user"}, method = HttpMethod.GET)
    public void login(RoutingContext rc) {
        String user = rc.request().getParam("user");
        Long ts = users.get(user);
        if (ts != null) {
            // demo return json response
            rc.response()
                    .putHeader("Content-type", "application/json")
                    .end(new JsonObject().put("user", user).put("created_at", ts).toString());
        } else {
            rc.response().setStatusCode(404).end("Login failed. User not found");
        }
    }

    @RequestMapping(path = "/register/:user", method = HttpMethod.GET)
    public void register(RoutingContext rc) {
        String user = rc.request().getParam("user");
        if (user == null || user.isEmpty()) {
            rc.response().setStatusCode(400).end("Invalid input");
            return;
        }
        if (!users.containsKey(user)) {
            users.put(user, System.currentTimeMillis());
            rc.response().end("Register success");
        } else {
            rc.response().setStatusCode(409).end("User exists");
        }
    }
}


```

### Servlet style for traditional JEE Web application

```java

@VertxletMapping(url = {"/id/:id", "/id"})
public class HelloExamples extends AbstractVertxlet {

    @Override
    protected void doGet(RoutingContext rc) throws Exception {
        String id = rc.request().getParam("id");
        if (id != null) {
            JsonObject json = new JsonObject()
                    .put("id", id)
                    .put("ts", System.currentTimeMillis());
            rc.response()
                    .putHeader("Content-Type", "application/json")
                    .end(json.toString());
        } else {
            rc.response().end("Hello, current path: " + rc.request().path());
        }
    }

    @Override
    protected void doPost(RoutingContext routingContext) throws Exception {
        super.doPost(routingContext);
    }
}

```

### Start Vertxlet server:

```java

public class ExampleRunner {

    public static void main(String[] args) {
        String confPath = args[0];
        Server.start(confPath);
    }
}

```