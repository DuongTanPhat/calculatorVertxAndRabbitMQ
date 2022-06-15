package eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
public class ConfigVerticle extends AbstractVerticle {
  public static void main(String[] args) {
    Launcher.executeCommand("run", ConfigVerticle.class.getName());
  }
  @Override
  public void start() throws Exception {

    ConfigStoreOptions fileStore = new ConfigStoreOptions()
      .setType("file")
      .setOptional(true)
      .setConfig(new JsonObject().put("path", "my-config.hocon"));
    ConfigStoreOptions sysPropsStore = new ConfigStoreOptions().setType("sys");
    ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore).addStore(sysPropsStore);
    ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
    retriever.getConfig(json -> {
      JsonObject configs = json.result();
      int numInstances = configs.getInteger("verticle-instances");
      DeploymentOptions doptions =  new DeploymentOptions();
      doptions.setInstances(numInstances);
      doptions.setWorker(false);
      vertx.deployVerticle(EventBusVerticle.class.getName(),doptions);
      vertx.deployVerticle(ProduccerVerticle.class.getName(),doptions);
    });


  }
}
