package org.imsi.badimsibox.badimsiserver;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class BadIMSIService extends AbstractVerticle {
	private Session currentSession = Session.init();
	static String defaultHeaders = "Origin, X-Requested-With, Content-Type, Accept";
    static String defaultMethods = "GET, POST, OPTIONS, PUT, HEAD, DELETE, CONNECT";
    static String defaultIpAndPorts = "*";
    
	private void parseJsonParams(Map<String,String> params, JsonObject reqJson, Buffer h) {
		String bufferMessage = h.toString();
		
		String[] paramSplits = bufferMessage.split("&");
		String[] valueSplits;
		
		for (String param : paramSplits) {
		        valueSplits = param.split("=");
		        if (valueSplits.length > 1) {
		            params.put((valueSplits[0]),(valueSplits[1]));
		        }
		}

	}
   
	@Override
	public void start() {
		Router router = Router.router(vertx);
		
		
		router.route().handler(rc -> {
            rc.response().putHeader("Access-Control-Allow-Headers", defaultHeaders);
            rc.response().putHeader("Access-Control-Allow-Methods", defaultMethods);
            rc.response().putHeader("Access-Control-Allow-Origin", defaultIpAndPorts);
            rc.next();
         });
		
		router.get("/master/session/start/:password").handler(rc -> {
			String password = rc.request().getParam("password");
			System.out.println(new Date() + ": Starting a session");
			this.currentSession = new Session(password);
			this.currentSession.updateTimestamp();
			//this.currentSession.nextSessionState();
			rc.response()
					.putHeader("content-type", "application/json")
					.end(new JsonObject().put("started", true).encode());
			
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
            						 .put("password",this.currentSession.getPassword())
            						 .put("timestamp", this.currentSession.getTimestamp())
            	.encode());
    		}
    	});
		
		router.get("/master/session/set/state/:state").handler(rc -> {
			String stateString = rc.request().getParam("state");
			int stateInt = Integer.parseInt(stateString);
			if((stateInt > -1) && (stateInt < 5) && (this.currentSession.getSessionState() < stateInt)) {
				this.currentSession.nextSessionState();
				rc.response().putHeader("content-type", "application/json")
				.end(new JsonObject().put("stateChanged", true).encode());
			} else {
			rc.response().putHeader("content-type", "application/json")
			.end(new JsonObject().put("stateChanged", false).encode());
			}
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
    
    	router.post("/master/sniffing/start/").handler(rc -> {
    		final JsonObject reqJson = new JsonObject();	
    		final Map<String,String> params = new HashMap<>();
    		
    		rc.request().bodyHandler(h -> {
    			parseJsonParams(params, reqJson, h);
    			for (String key : params.keySet()) {
    				reqJson.put(key, params.get(key));
    			}
    			
    			reqJson.put("state", "start");
    			
    			rc.response()
            	.putHeader("content-type", "application/json")
            	.end(reqJson.encode());
    		});
    		
    		/*
    			String data = rc.request().getFormAttribute("operator");
    			System.out.println(data);
    			System.out.println("Haha");
           		*/

    			// get BTS Objects here
    			/* .... */
    			
    			/* Mock datas */
    			/*
    			List<Bts> bouyguesList = new ArrayList<>();
    			
    			List<String> arfcns = new ArrayList<>();
    			arfcns.add("702");
    			arfcns.add("724");
    			arfcns.add("751");
    			
    			List<String> arfcns2 = new ArrayList<>();
    			arfcns2.add("980");
    			arfcns2.add("998");
    			arfcns2.add("1023");
    			
    			bouyguesList.add(new Bts("20", "208", "54", "56254", arfcns));
    			bouyguesList.add(new Bts("20", "208", "54", "56243", arfcns2));
    			bouyguesList.add(new Bts("20", "208", "56", "56265", arfcns));
    			bouyguesList.add(new Bts("20", "208", "56", "56212", arfcns2));
    			*/
    		
    		
    		/*
    		String[] pythonLocationScript = {PythonCaller.getContextPath()+"badimsicore","-l","start"};
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
    	
       	router.get("/master/sniffing/stop").handler(rc -> {
        		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("state", "stop")
            	.encode());
    		
    		/*
    		String[] pythonLocationScript = {PythonCaller.getContextPath()+"badimsicore","-l","stop"};
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
    	
    	router.post("/master/jamming/start/").handler(rc -> {
    		String operator = rc.request().getParam("operator");
    		if(operator != null) {
        		// We have to give the right response
        		rc.response()
                	.putHeader("content-type", "application/json")
                	.end(new JsonObject().put("state", "start").put("operator", operator)
                	.encode());    			
    		}
    		
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

    	router.get("/master/jamming/stop").handler(rc -> {
    		// We have to give the right response
    		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("state", "stop")
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

    	router.post("/master/fakebts/start/").handler(rc -> {    		
    		final JsonObject reqJson = new JsonObject();
    		final Map<String, String> params = new HashMap<String, String>();
    		
    		rc.request().bodyHandler(h -> {
    			parseJsonParams(params,reqJson, h);
    			for (String key : params.keySet()) {
    				reqJson.put(key, params.get(key));
    			}
    			
    			reqJson.put("state", "start");
    			rc.response()
            	.putHeader("content-type", "application/json")
            	.end(reqJson.encode());
    		});
    		
    		/*
    		String[] pythonLocationScript = {PythonCaller.getContextPath()+"badimsicore","-b","start"};
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

    	
    	router.get("/master/fakebts/stop").handler(rc -> {    		
    		// We have to give the right response
    		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("state", "stop")
            	.encode());
    		
    		/*
    		String[] pythonLocationScript = {PythonCaller.getContextPath()+"badimsicore","-b","stop"};
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


    	router.post("/master/attack/sms/send/").handler(rc -> {    		
    		// We have to give the right response
    		final JsonObject reqJson = new JsonObject();	
    		final Map<String,String> params = new HashMap<>();
    		
    		
    		rc.request().bodyHandler(h -> {
    			parseJsonParams(params, reqJson, h);
    			for (String key : params.keySet()) {
    				reqJson.put(key, params.get(key));
    			}
    			
    			System.out.println(reqJson);
    			
    			rc.response()
            	.putHeader("content-type", "application/json")
            	.end(reqJson.encode());
    		});
    		
    		/*
    		String[] pythonLocationScript = {PythonCaller.getContextPath()+"badimsicore","-b","stop"};
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
