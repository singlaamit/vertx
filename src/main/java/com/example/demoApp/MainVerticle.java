package com.example.demoApp;

import io.vertx.core.AbstractVerticle;

import io.vertx.core.AsyncResult;
import io.vertx.core.Launcher;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.unit.TestOptions;
import io.vertx.ext.unit.TestSuite;
import io.vertx.ext.unit.report.ReportOptions;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.core.http.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.vertx.ext.web.templ.HandlebarsTemplateEngine;


public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);





        router.get("/").handler(this::testCaseOne);
        router.get("/getCategory").handler(ctx -> {
            HttpServerResponse response = ctx.response();
            final HttpClient httpClient = vertx.createHttpClient();
            final String url = "http://www.apnikheti.com/apnikheti-version9/getCategory.php?lang=en";

            httpClient.getAbs(url, (HttpClientResponse res) -> {
                if (res.statusCode() != 200) {
                    System.err.println("fail");
                } else {
                    engine.render(ctx, "templates/category.hbs", (AsyncResult<Buffer> res1) -> {
                        if (res1.succeeded()) {
                            response.setChunked(true);
                            ctx.response().write(res1.result());
                            ctx.put("body",
                                res.bodyHandler(b -> response
                                    .putHeader("content-type",
                                        "application/json")
                                    .end(b)));
                        } else {
                            ctx.fail(res1.cause());
                        }
                    });
                    //res.bodyHandler(b -> response.putHeader("content-type", "application/json").end(b));
                }
            }).end();
        });
        server.requestHandler(router::accept).listen(8080);
    }




    public void testCaseOne(RoutingContext r){
        HttpServerResponse response = r.response();
         JsonObject mySQLClientConfig = new JsonObject()
             .put("host", "*******")
             .put("database", "*******")
             .put("username", "*******")
             .put("password", "*******");
        SQLClient mySQLClient = MySQLClient.createShared(vertx, mySQLClientConfig);


        mySQLClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();

                connection.query("SELECT * FROM tbl_users limit 100", handle->{
                    ResultSet rs=new ResultSet();
                    rs = handle.result();
                    response.putHeader("content-type","application/json").end(rs.getResults().toString());
                });

            } else {
                System.err.println(res.cause().getMessage());
                return;
            }
        });

       /* HttpServerResponse res=r.response();
        TestSuite suite = TestSuite.create("the_test_suite");
        suite.test("my_test_case", context -> {
            String s = "value";
            context.assertEquals("value", s);
        });

        suite.run(new TestOptions().addReporter(new ReportOptions().setTo("console")));

        res.end();*/
    }



    public void routing(RoutingContext context) {

        HttpServerResponse response = context.response();
        response.end("Hello Amit");
    }
}

