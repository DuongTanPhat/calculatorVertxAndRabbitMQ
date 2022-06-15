package RabbitMQ;

import com.example.starter.MainVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;

public class VertXRabbitMQ extends AbstractVerticle {
  public static void main(String[] args) {
    Launcher.executeCommand("run", VertXRabbitMQ.class.getName());
  }
  @Override
  public void start() throws Exception {

    RabbitMQOptions config = new RabbitMQOptions();
// Each parameter is optional
// The default parameter with be used if the parameter is not set
    config.setUser("test");
    config.setPassword("test");
    config.setHost("172.16.9.166");
    config.setPort(5672);
    config.setAutomaticRecoveryEnabled(true);

    RabbitMQClient client = RabbitMQClient.create(vertx, config);
// Connect

    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    router.post("/calculator").handler(BodyHandler.create()).handler(context->{
      JsonObject obj = context.getBodyAsJson();
      JsonObject reply = new JsonObject();
      String a = obj.getString("a");
      String b = obj.getString("b");
      String c = obj.getString("math");
      client.start(asyncResult -> {
        if (asyncResult.succeeded()) {
          System.out.println("RabbitMQ successfully connected!");
          JsonObject k = new JsonObject();
          k.put("body",context.getBodyAsJson().toString());
          client.basicPublish("", "Phat_duong1",k , pubResult -> {
            if (pubResult.succeeded()) {
              System.out.println("Message published !");
            } else {
              pubResult.cause().printStackTrace();
            }
          });
        } else {
          System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
        }
      });
      context.response().end(obj.toString());
    });
    server.requestHandler(router).listen(8080);
  }
}
