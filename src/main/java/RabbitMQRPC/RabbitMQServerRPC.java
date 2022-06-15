package RabbitMQRPC;

import com.rabbitmq.client.*;
import io.vertx.core.json.JsonObject;

public class RabbitMQServerRPC {

  private static final String RPC_QUEUE_NAME = "rpc_queue";

  private static JsonObject calculator(JsonObject obj) {
    JsonObject reply = new JsonObject();
    Double a = obj.getDouble("a");
    Double b = obj.getDouble("b");
    String c = obj.getString("math");
    try{
      switch (c){
        case "+":{
          reply.put("result",a+b);
          break;
        }
        case "-":{
          reply.put("result",a-b);
          break;
        }
        case "*":{
          reply.put("result",a*b);
          break;
        }
        case "/":{
          reply.put("result",a/b);
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
    return reply;
  }

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("172.16.9.166");
    factory.setUsername("test");
    factory.setPassword("test");
    factory.setPort(5672);

    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
      channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
      channel.queuePurge(RPC_QUEUE_NAME);

      channel.basicQos(1);

      System.out.println(" [x] Awaiting RPC requests");

      Object monitor = new Object();
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
          .Builder()
          .correlationId(delivery.getProperties().getCorrelationId())
          .build();

        String response = "";

        try {

          String message = new String(delivery.getBody(), "UTF-8");
          JsonObject obj = new JsonObject(message);


          System.out.println(" [.] calculator(" + message + ")");
          JsonObject obj2 = calculator(obj);
          response += calculator(obj).toString();
          Thread.sleep(1000);
        } catch (RuntimeException e) {
          System.out.println(" [.] " + e.toString());
        }
        catch (InterruptedException e) {
          System.out.println("Error Sleep");
        }
        finally
        {
          channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          // RabbitMq consumer worker thread notifies the RPC server owner thread
          synchronized (monitor) {
            monitor.notify();
          }
        }
      };

      channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> { }));
      // Wait and be prepared to consume the message from RPC client.
      while (true) {
        synchronized (monitor) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
