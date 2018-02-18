package com.example.demoApp;

import io.vertx.core.AbstractVerticle;

import io.vertx.core.Launcher;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
// import io.vertx.example.util.Runner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;

import io.vertx.ext.mongo.MongoClient;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Test extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        // Runner.runExample(SimpleREST.class);
        Launcher.executeCommand("run", MainVerticle.class.getName());
    }

    private Map<String, JsonObject> products = new HashMap<>();

    MongoClient mongoClient;
    HttpClient httpClient;

    @Override
    public void start() {

        mongoClient = MongoClient.createShared(vertx, new JsonObject()
            .put("connection_string", "mongodb://localhost:27017")
            .put("db_name", "demoproducts"));

        setUpInitialData();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.get("/products/:productID").handler(this::handleGetProduct);
        router.put("/products/:productID").handler(this::handleAddProduct);
        router.get("/products").handler(this::handleListProducts);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void handleGetProduct(RoutingContext routingContext) {
        String productID = routingContext.request().getParam("productID");
        HttpServerResponse response = routingContext.response();
        if (productID == null) {
            sendError(400, response);
        } else {
            JsonObject product = products.get(productID);
            if (product == null) {
                sendError(404, response);
            } else {
                response.putHeader("content-type", "application/json").end(product.encodePrettily());

                mongoClient.find("demoproducts", new JsonObject().put("productID", productID), res -> {
                    System.out.println("aa gya oye result : " + res.result().toString());
                });

            }
        }
    }

    private void handleAddProduct(RoutingContext routingContext) {
        String productID = routingContext.request().getParam("productID");
        String name = routingContext.request().getParam("name");
        HttpServerResponse response = routingContext.response();
        if (productID == null) {
            sendError(400, response);
        } else {
            JsonObject product = routingContext.getBodyAsJson();
            if (product == null) {
                sendError(400, response);
            } else {
                // products.put(productID, product);

                JsonObject product1 = new JsonObject().put("productID", productID).put("name", name);
                mongoClient.save("demoproducts", product1, id -> {
                    System.out.println("Inserted id: " + id.result());
                });

                response.end();
            }
        }
    }

    private void handleListProducts(RoutingContext routingContext) {
        JsonArray arr = new JsonArray();
        products.forEach((k, v) -> arr.add(v));
        routingContext.response().putHeader("content-type", "application/json").end(arr.encodePrettily());
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    private void setUpInitialData() {


        final String url = "http://www.apnikheti.com/apnikheti-version9/getAkNewsDetails.php?" + getParams();

        System.out.println(url);
        httpClient = vertx.createHttpClient();
        httpClient.getAbs(url, response -> {
            if (response.statusCode() != 200) {
                System.err.println("fail");
            } else {
                response.bodyHandler(b -> System.out.println(b.toString()));
            }
        }).end();




      /*  addProduct(new JsonObject().put("id", "prod3568").put("name", "Egg Whisk").put("price", 3.99).put("weight", 150));
        addProduct(new JsonObject().put("id", "prod7340").put("name", "Tea Cosy").put("price", 5.99).put("weight", 100));
        addProduct(new JsonObject().put("id", "prod8643").put("name", "Spatula").put("price", 1.00).put("weight", 80));
        addProduct(new JsonObject().put("id", "prod8649").put("name", "Prantha").put("price", 7.00).put("weight", 100));
   */
    }

    private String getParams() {
        StringBuilder sb = new StringBuilder();

        sb.append("news_id=1019")
            .append("&user_id=797")
            .append("&lang=en");

        return sb.toString();
    }

    private void addProduct(JsonObject product) {
        products.put(product.getString("id"), product);
        String pro = String.valueOf(product);
        JsonObject product1 = new JsonObject().put("productID", pro);

        mongoClient.save("demoproducts", product1, id -> {
            System.out.println("Inserted id: " + id.result());

        });


    }


}


