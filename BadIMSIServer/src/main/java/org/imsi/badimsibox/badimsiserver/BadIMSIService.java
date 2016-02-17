package org.imsi.badimsibox.badimsiserver;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class BadIMSIService extends AbstractVerticle {
	private Session currentSession = Session.init();
	static String defaultHeaders = "Origin, X-Requested-With, Content-Type, Accept";
    static String defaultMethods = "GET, POST, OPTIONS, PUT, HEAD, DELETE, CONNECT";
    static String defaultIpAndPorts = "*";
    static List<Bts> operatorList = new ArrayList<>();
    
	private void parseJsonParams(Map<String,String> params, JsonObject reqJson, Buffer h) {
		String bufferMessage = h.toString();		
		String[] paramSplits = bufferMessage.split("&");
		String[] valueSplits;
		
		for (String param : paramSplits) {
		        valueSplits = param.split("=");
		        if (valueSplits.length > 1) {
		        	String msg = valueSplits[1].replace("+", " ");
		            params.put((valueSplits[0]),msg);
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
                        // Building the JSON on server side sent by client
    			for (String key : params.keySet()) {
    				reqJson.put(key, params.get(key));
    			}
                        
    			// retrieving the operator name from HTML page
                        String operator = reqJson.getString("operator");
                        
                        // Calling python script to launch the sniffing
                        String[] pythonLocationScript = {PythonCaller.getContextPath()+"badimsicore-listen","-a",operator};
                        PythonCaller pc = new PythonCaller(pythonLocationScript);
                        
                        try {
                            // getting the sdtout of the python script
                            Stream<String> streamBTS = (Stream<String>) pc.process().getInputStream();
                            streamBTS.filter(line -> !line.startsWith("#")).map(line -> line.split(",")).forEach((tab) -> {
                                // just to debug by display because WE ARE WARRIORS !!!!
                                System.out.println("Line : " + tab);
                                List<String> arfcns = new ArrayList<>(tab.length-4);
                                for (int i = 4; i < tab.length; i++) {
                                    arfcns.add(tab[i]);
                                }
                                operatorList.add(new Bts(tab[0], tab[1], tab[2], tab[3], arfcns));
                            });
                        } catch (IOException ex) {
                            Logger.getLogger(BadIMSIService.class.getName()).log(Level.SEVERE, null, ex);
                        }

    			JsonArray array = new JsonArray();
    			operatorList.forEach(item -> {
    				JsonObject jsonObject = new JsonObject();
    				jsonObject.put("Network", item.getOperatorByMnc());
    				jsonObject.put("MCC", item.getOperator().getMcc());
    				jsonObject.put("LAC", item.getLac());
    				jsonObject.put("CI", item.getCi());
    				StringBuilder sb = new StringBuilder();
    				item.getArfcn().forEach(arfcn -> {
    					sb.append(arfcn);
    					sb.append(", ");
    				});
    				sb.delete(sb.length()-1	, sb.length());
    				jsonObject.put("ARFCNs", sb.toString());
    				array.add(jsonObject);
    			});
    			
    			rc.response().putHeader("content-type", "application/json")
    			.end(array.encode());
    		});
    	});
        
        router.route("/master/jamming/operator/").handler(rc -> {
    		JsonArray array = new JsonArray();
                operatorList.forEach(item -> {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.put("Network", item.getOperatorByMnc());
                        jsonObject.put("MCC", item.getOperator().getMcc());
                        jsonObject.put("LAC", item.getLac());
                        jsonObject.put("CI", item.getCi());
                        StringBuilder sb = new StringBuilder();
                        item.getArfcn().forEach(arfcn -> {
                                sb.append(arfcn);
                                sb.append(", ");
                        });
                        sb.delete(sb.length()-1	, sb.length());
                        jsonObject.put("ARFCNs", sb.toString());
                        array.add(jsonObject);
                });
                rc.response().putHeader("content-type", "application/json")
                .end(array.encode());
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
    			
    			String sender = reqJson.getString("sender");
    			String msg = reqJson.getString("message");
    			String imsi = reqJson.getString("imsi");
    			
        		String[] pythonLocationScript = {PythonCaller.getContextPath()+"badimsicore","-s", imsi, sender, msg};
        		
        		for (String arg : pythonLocationScript) {
        			System.out.print(arg+" ");
				}
        		
        		/*
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
                	.end(new JsonObject().put("Message sent", pc.getResultSb())
                	.encode());
        		}	
    			
    			
    			rc.response()
            	.putHeader("content-type", "application/json")
            	.end(reqJson.encode());
            	*/
    		});

    	});
    	
    	router.route("/eventbus/*").handler(SockJSHandler.create(vertx)
    			.bridge(new BridgeOptions()
    					.addOutboundPermitted(
    							new PermittedOptions().setAddress("sms.new"))
    					));
    	
		router.route().handler(StaticHandler.create());
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}
}
