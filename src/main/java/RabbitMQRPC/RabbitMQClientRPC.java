package RabbitMQRPC;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class RabbitMQClientRPC implements AutoCloseable {

  private Connection connection;
  private Channel channel;
  private String requestQueueName = "rpc_queue";
  private ArrayList<String> arrayId ;
  private ArrayList<String> arrayQueue ;

  public RabbitMQClientRPC() throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("172.16.9.166");
    factory.setUsername("test");
    factory.setPassword("test");
    factory.setPort(5672);
    arrayId = new ArrayList<>();
    connection = factory.newConnection();
    channel = connection.createChannel();
    arrayQueue = new ArrayList<>();
  }

  public static void main(String[] argv) {
    try (RabbitMQClientRPC calculatorRpc = new RabbitMQClientRPC()) {
      for (int i = 0; i < 32; i++) {
        JsonObject obj = new JsonObject();
        obj.put("a",Math.random());
        obj.put("b",Math.random());
        switch (i%4){
          case 0:{
            obj.put("math","+");
            break;
          }
          case 1:{
            obj.put("math","-");
            break;
          }
          case 2:{
            obj.put("math","*");
            break;
          }
          case 3:{
            obj.put("math","/");
            break;
          }
        }
        System.out.println(" [x] Requesting calculator(" + obj.toString()+" Time:"+System.currentTimeMillis()+ ")");
        calculatorRpc.call(obj.toString());
      }
      for (int i=0;i<calculatorRpc.arrayId.size();i++) {
        String id = calculatorRpc.arrayId.get(i);
        String queue = calculatorRpc.arrayQueue.get(i);
        String response = calculatorRpc.receive(id,queue);
        System.out.println(id + " [.] Got '" + response + "' at "+System.currentTimeMillis());
      }
    } catch (IOException | TimeoutException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void call(String message) throws IOException, InterruptedException {
    final String corrId = UUID.randomUUID().toString();
    System.out.println(corrId);
    arrayId.add(corrId);
    String replyQueueName = channel.queueDeclare().getQueue();
    System.out.println(replyQueueName);
    arrayQueue.add(replyQueueName);
    AMQP.BasicProperties props = new AMQP.BasicProperties
      .Builder()
      .correlationId(corrId)
      .replyTo(replyQueueName)
      .build();

    channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));
  }
  public String receive(String corrId,String queueName) throws IOException, InterruptedException {
    final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
    String ctag = channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
      if (delivery.getProperties().getCorrelationId().equals(corrId)) {
        response.offer(new String(delivery.getBody(), "UTF-8"));
      }
    }, consumerTag -> {
    });

    String result = response.take();
    channel.basicCancel(ctag);
    return result;
  }


  public void close() throws IOException {
    connection.close();
  }
}
