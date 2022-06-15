package RabbitMQ;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.nio.charset.StandardCharsets;

public class Main {
  private final static String QUEUE_NAME = "Phat_duong1";
  public static void main(String[] argv) throws Exception {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost("172.16.9.166");
      factory.setUsername("test");
      factory.setPassword("test");
      factory.setPort(5672);
      try (Connection connection = factory.newConnection();
      Channel channel = connection.createChannel()){
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String message = "Hello World!";
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
        System.out.println(" [x] Sent '" + message + "'");
      }
  }
}
