package RabbitMQ;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;

public class VertXRabbitMQReceive  extends AbstractVerticle {
  public static void main(String[] args) {
    Launcher.executeCommand("run", VertXRabbitMQReceive.class.getName());
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
    client.start(asyncResult -> {
      if (asyncResult.succeeded()) {
        System.out.println("RabbitMQ successfully connected!");
        client.basicConsumer("Phat_duong1", rabbitMQConsumerAsyncResult -> {
          if (rabbitMQConsumerAsyncResult.succeeded()) {
            System.out.println("RabbitMQ consumer created !");
            RabbitMQConsumer mqConsumer = rabbitMQConsumerAsyncResult.result();

            mqConsumer.handler(message -> {
              System.out.println("Got message: " + message.body().toString());
              JsonObject reply = new JsonObject();
              JsonObject receive = new JsonObject(message.body());
              String a = receive.getString("a");
              String b = receive.getString("b");
              String c = receive.getString("math");
              try{
                switch (c){
                  case "+":{
                    reply.put("result",Float.valueOf(a)+Float.valueOf(b));
                    break;
                  }
                  case "-":{
                    reply.put("result",Float.valueOf(a)-Float.valueOf(b));
                    break;
                  }
                  case "*":{
                    reply.put("result",Float.valueOf(a)*Float.valueOf(b));
                    break;
                  }
                  case "/":{
                    reply.put("result",Float.valueOf(a)/Float.valueOf(b));
                    break;
                  }
                  default:{
                    reply.put("Error","Cant regconize math");
                    break;
                  }
                }
              }catch(Exception e){
                reply.put("Error","Cant convert to float");
              }
              System.out.println(reply.toString());
            });
          } else {
            rabbitMQConsumerAsyncResult.cause().printStackTrace();
          }

        });
      } else {
        System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
      }
    });
  }
}
