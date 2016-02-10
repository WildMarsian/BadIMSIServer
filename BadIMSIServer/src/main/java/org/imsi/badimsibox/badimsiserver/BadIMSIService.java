package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class BadIMSIService extends AbstractVerticle {

	@Override
	public void start() {
		Router router = Router.router(vertx);
		
		router.get("/master/session/:state").handler(rc -> {
			String name = rc.request().getParam("state");
    		if(name.equals("start")) {
    			/* Code to start session */
    		}
    		
    		if(name.equals("stop")) {
    			/* Code to stop session */
    		}
    		
    		// We have to give the right response
    		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("state", name)
            	.put("key", "value") // in the js, object.key -> value
            	.encode());
    	});  	
    	
		router.get("/master/map/:locate").handler(rc -> {	
    		// Actually, this is an example: Remove me when you put the right code
			String name = "48.839915,2.5842899,17z";
    		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("location", name)
            	.encode());
		
			/*	
			// Example to call python files from Java
			String[] pythonLocationScript = {"/path/to/script/python/locate_me.py","-i","input","-o", "output"};
    		PythonCaller pc = new PythonCaller(pythonLocationScript);
    		int exitValue = -1;
    		try {
				exitValue = pc.process();
			} catch (Exception e) {
				e.printStackTrace();
			}
    		
    		if(exitValue == 0) {
        		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("location", pc.getResultSb())
            	.encode());
    		}	
    		*/
    	});
    	
    	router.get("/master/sniffing/:state").handler(rc -> {
    		
    		String name = rc.request().getParam("state");
    		rc.response()
        	.putHeader("content-type", "application/json")
        	.end(new JsonObject().put("state", name)
        	.encode());
    		
    		/*
    		String[] pythonLocationScript = {PythonCaller.getContextPath()+"badimsicore","-l",name};
    		PythonCaller pc = new PythonCaller(pythonLocationScript);
    		int exitValue = -1;
    		try {
				exitValue = pc.process();
			} catch (Exception e) {
				e.printStackTrace();
			}
    		
    		if(exitValue == 0) {
        		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("sniffing", pc.getResultSb())
            	.encode());
    		}	
    		*/

    	});
    	
    	router.get("/master/jamming/:state").handler(rc -> {
    		String name = rc.request().getParam("state");
    		
    		// We have to give the right response
    		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("state", name)
            	.encode());
    		
    		/*
    		String[] pythonLocationScript = {PythonCaller.getContextPath()+"badimsicore","-j",name};
    		PythonCaller pc = new PythonCaller(pythonLocationScript);
    		int exitValue = -1;
    		try {
				exitValue = pc.process();
			} catch (Exception e) {
				e.printStackTrace();
			}
    		
    		if(exitValue == 0) {
        		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("jamming", pc.getResultSb())
            	.encode());
    		}	
    		*/
    	});
    	
    	router.get("/master/fakebts/:state").handler(rc -> {
    		String name = rc.request().getParam("state");
    		
    		// We have to give the right response
    		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("state", name)
            	.encode());
    		
    		/*
    		String[] pythonLocationScript = {PythonCaller.getContextPath()+"badimsicore","-b",name};
    		PythonCaller pc = new PythonCaller(pythonLocationScript);
    		int exitValue = -1;
    		try {
				exitValue = pc.process();
			} catch (Exception e) {
				e.printStackTrace();
			}
    		
    		if(exitValue == 0) {
        		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("fake bts", pc.getResultSb())
            	.encode());
    		}	
    		*/
    	});

		
		router.route().handler(StaticHandler.create());
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}
}
