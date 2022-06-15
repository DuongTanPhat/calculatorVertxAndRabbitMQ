package eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class EventBusVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer("consumer",mes->{
      JsonObject obj = new JsonObject(mes.body().toString());
      JsonObject rep = new JsonObject();
      String a = obj.getString("a");
      String b = obj.getString("b");
      try{
        switch (obj.getString("math")){
          case "+":{
            rep.put("result",Float.valueOf(a)+Float.valueOf(b));
            break;
          }
          case "-":{
            rep.put("result",Float.valueOf(a)- Float.valueOf(b));
            break;
          }
          case "*":{
            rep.put("result",Float.valueOf(a)*Float.valueOf(b));
            break;
          }
          case "/":{
            rep.put("result",Float.valueOf(a)/Float.valueOf(b));
            break;
          }
          default:{
            rep.put("error","");
            break;
          }
        }
      }catch (Exception e){

        rep.put("error","");
      }
      mes.reply(rep);
    });
    System.out.println("Ready!");
  }
}
