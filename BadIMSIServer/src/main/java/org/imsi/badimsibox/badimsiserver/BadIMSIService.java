package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;


public class BadIMSIService extends AbstractVerticle {
	
    @Override
    public void start() {
        Router router = Router.router(vertx);
        
        /*
        Ici on definit les differentes URI et comment elles seront gerrés par vertx a l'aide du Handler. 
        Ici il s'agit d'une méthode get car il n'y a pas de modifications des donnés au niveau du service.
        Les parties de l'URI contenant des ":" sont des paramètres.
        */
        
        router.get("/hello/world/:name/").handler(rc -> {
            //Recuperation du parametre name
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

        /*
        Ici on définit la methode appelee lors de l'ecoute et sur quel port 
        cette écoute s'effectue
        */
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
