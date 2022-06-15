package eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class ProduccerVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception{
    JsonObject obj = new JsonObject();

    obj.put("a","3.4f");
    obj.put("b","5.6f");
    obj.put("math","-");
    vertx.eventBus().send("consumer",obj,handler->{
      if(handler.succeeded()) {
        System.out.println(handler.result().body());
      }else {
        System.out.println("Faild to deliver");
      }
    });
  }
}
