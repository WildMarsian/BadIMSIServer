package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class BadIMSIService extends AbstractVerticle {
	private Session currentSession = new Session("test");
	@Override
	public void start() {
		Router router = Router.router(vertx);
		
		router.get("/master/map/:locate").handler(rc -> {
    		String name = "48.839915,2.5842899,17z";
    		// We have to give the right response
    		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("location", name)
            	.encode());
    	});
    	
    	router.get("/master/sniffing/:state").handler(rc -> {
    		String name = rc.request().getParam("state");

    		// We have to give the right response
    		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("state", name)
            	.encode());
    	});
    	
    	router.get("/master/jamming/:state").handler(rc -> {
    		String name = rc.request().getParam("state");

    		// We have to give the right response
    		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("state", name)
            	.encode());
    	});
    	
    	router.get("/master/fakebts/:state").handler(rc -> {
    		String name = rc.request().getParam("state");
    		
    		// We have to give the right response
    		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("state", name)
            	.encode());
    	});

		router.get("/master/session/state").handler(rc -> {
    		//String name = rc.request().getParam("state");
    		
    		if(this.currentSession == null) {
    			rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("state", -1)
            	.encode());
    		} else {
    			rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("state", this.currentSession.getSessionState())
            						 .put("","")
            	.encode());
    		}
    	});
		
		router.get("/master/session/start/:password").handler(rc -> {
			String password = rc.request().getParam("password");
			System.out.println("Starting a session");
			this.currentSession = new Session(password);
			rc.response()
					.putHeader("content-type", "application/json")
					.end(new JsonObject().put("started", true).encode());
			
		});

		router.route().handler(StaticHandler.create());
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}
}
