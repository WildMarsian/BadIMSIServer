package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;


public class BadIMSIService extends AbstractVerticle {
	
    @Override
    public void start() {
        Router router = Router.router(vertx);
        
        // Just us me for tests. Remove me at the end. Thank you :)
        router.get("/hello/world/:name/").handler(rc -> {
            String name = rc.request().getParam("name");
            rc.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("key", "Bonjour " + name).encode());
        });
        
        router.get("/user/:username/password/:salted").handler(rc -> {
        	JsonObject authInfo = new JsonObject().put("username", "zak").put("password", "mypassword");
			
        	AuthProvider authProvider = new AuthProvider() {
    			
    			@Override
    			public void authenticate(JsonObject arg0, Handler<AsyncResult<User>> arg1) {
    				// TODO Auto-generated method stub
    				
    			}
    	};

        	
        authProvider.authenticate(authInfo, res -> {
        		  if (res.succeeded()) {

        		    User user = res.result();

        		    System.out.println("User " + user.principal() + " is now authenticated");

        		  } else {
        		    res.cause().printStackTrace();
        		  }
        		});
        });

        // Creating the static route towards the webroot folder 
        router.route().handler(StaticHandler.create());
        
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
