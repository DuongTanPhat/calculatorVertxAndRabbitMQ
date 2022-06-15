package com.example.starter;

import eventbus.ConfigVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
  public static void main(String[] args) {
    Launcher.executeCommand("run", MainVerticle.class.getName());
  }
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    System.out.println("hello");
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    router.post("/calculator").handler(BodyHandler.create()).handler(context->{
      JsonObject objPost = context.getBodyAsJson();
      String a = objPost.getString("a");
      String b = objPost.getString("b");
      String math2 = objPost.getString("math");
      JsonObject obj = new JsonObject();
      try{
        switch (math2){
          case "+":{
            obj.put("result",Float.valueOf(a)+Float.valueOf(b));
            break;
          }
          case "-":{
            obj.put("result",Float.valueOf(a)-Float.valueOf(b));
            break;
          }
          case "*":{
            obj.put("result",Float.valueOf(a)*Float.valueOf(b));
            break;
          }
          case "/":{
            obj.put("result",Float.valueOf(a)/Float.valueOf(b));
            break;
          }
          default:{
            obj.put("Code","002");
            obj.put("Message","Cant recognize math");
          }
        }
      }catch (Exception e){
        obj.put("Code","001");
        obj.put("Message","Cant convert to string");
      }

      context.response().end(obj.encodePrettily());
    });
    server.requestHandler(router).listen(8080);
  }
}
