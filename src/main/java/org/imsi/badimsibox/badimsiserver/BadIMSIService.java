package org.imsi.badimsibox.badimsiserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class BadIMSIService extends AbstractVerticle {

    
    static String defaultHeaders = "Origin, X-Requested-With, Content-Type, Accept";
    static String defaultMethods = "GET, POST, OPTIONS, PUT, HEAD, DELETE, CONNECT";
    static String defaultIpAndPorts = "*";
    static List<Bts> operatorList = new ArrayList<>();
    private Vertx vertx;
    private Session currentSession = Session.init(vertx);

    private Sniffer snifferHandler = null;

    public BadIMSIService(Vertx vertx) {
        this.vertx = vertx;
    }

    private void parseJsonParams(Map<String, String> params, JsonObject reqJson, Buffer h) {
        String bufferMessage = h.toString();
        String[] paramSplits = bufferMessage.split("&");
        String[] valueSplits;

        for (String param : paramSplits) {
            valueSplits = param.split("=");
            if (valueSplits.length > 1) {
                String msg = valueSplits[1].replace("+", " ");
                params.put((valueSplits[0]), msg);
            }
        }
    }

    /*
    public void getTMSIs() {
        final List<Target> targets = new ArrayList<>();

        Process proc;
        String[] pythonLocationScript = {PythonCaller.getContextPath() + "badimsicore_openbts.py", "--list-tmsis"};
        PythonCaller pc = new PythonCaller(pythonLocationScript);
        try {
            proc = pc.process();
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                buffer.lines().forEach(l -> {
                    String[] words = l.split(" ");
                    targets.add(new Target(words[0], words[1], words[2]));
                });
            }

            if (proc.exitValue() == 0) {
                if (targets.size() > 0) {
                    for (Target target : targets) {
                        vertx.eventBus().publish("imsi.new", target.toJson());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     */
    public void getAllSms() throws IOException {

        String[] pythonLocationScript = {"./scripts/badimsicore_sms_interceptor.py", "-i", "./scripts/smqueue.txt"};
        PythonCaller pc = new PythonCaller(pythonLocationScript, (in, out, err, returnCode) -> {
            if (returnCode == 0) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                bufferedReader.lines().forEach(l -> {
                    String removeParenthesis = l.replaceAll("[()]", "");
                    String[] splitted = removeParenthesis.split(",");
                    String first = splitted[0].replaceAll("['']", "");
                    String second = splitted[1].replaceAll("['']", "");
                    vertx.eventBus().publish("sms.new", new Sms(first, second).toJson());
                });
            }
        });
        try {
            pc.exec();
        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
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
            this.currentSession = new Session(password, vertx);
            this.currentSession.updateTimestamp();
            rc.response().putHeader("content-type", "application/json").end(new JsonObject().put("started", true).encode());

        });

        router.get("/master/session/state").handler(rc -> {

            if (this.currentSession == null) {
                rc.response().putHeader("content-type", "application/json").end(new JsonObject().put("state", -1).encode());
            } else {
                rc.response().putHeader("content-type", "application/json").end(new JsonObject().put("state", this.currentSession.getSessionState()).put("password", this.currentSession.getPassword()).put("timestamp", this.currentSession.getTimestamp()).encode());
            }
        });

        router.get("/master/session/set/state/:state").handler(rc -> {
            String stateString = rc.request().getParam("state");
            int stateInt = Integer.parseInt(stateString);
            if ((stateInt > -1) && (stateInt < 5) && (this.currentSession.getSessionState() < stateInt)) {
                this.currentSession.nextSessionState();
                rc.response().putHeader("content-type", "application/json").end(new JsonObject().put("stateChanged", true).encode());
            } else {
                rc.response().putHeader("content-type", "application/json").end(new JsonObject().put("stateChanged", false).encode());
            }
        });
        
        router.post("/master/sniffing/selectOperator/").handler(rc -> {
        	final JsonObject reqJson = new JsonObject();
        	final Map<String, String> params = new HashMap<>();
        	rc.request().bodyHandler(h -> {
        		parseJsonParams(params, reqJson, h);
        		for(String key : params.keySet()) {
        			reqJson.put(key, params.get(key));
        		}
        		String operator = reqJson.getString("operator");
        		JsonObject json = new JsonObject();
        		json.put("operator", operator);
        		this.vertx.eventBus().publish("observer.new", json.encode());
        		rc.response().putHeader("content-type", "application/json").end(new JsonObject().put("selectReceived", true).encode());
        	});
        });

        router.post("/master/sniffing/start/").handler(rc -> {
            final JsonObject reqJson = new JsonObject();
            final Map<String, String> params = new HashMap<>();

            rc.request().bodyHandler(h -> {
                parseJsonParams(params, reqJson, h);
                // Building the JSON on server side sent by client
                for (String key : params.keySet()) {
                    reqJson.put(key, params.get(key));
                }

                // retrieving the operator name from HTML page
                String operator = reqJson.getString("operator");

                // Calling python script to launch the sniffing
                String[] pythonLocationScript = {"./scripts/badimsicore_listen.py", "-o", operator};

                snifferHandler = new Sniffer();

                boolean isOver = snifferHandler.start(reqJson, pythonLocationScript);
                JsonObject answer = new JsonObject();
                answer.put("started", !isOver);
                JsonObject observer = new JsonObject().put("startedSniffing", true);
                this.vertx.eventBus().publish("observer.new", observer.encode());
                rc.response().putHeader("content-type", "application/json").end(answer.encode());
            });
        });

        router.route("/master/sniffing/status/").handler(rc -> {
            String status = snifferHandler.status();
            JsonObject answer = new JsonObject();
            answer.put("status", status);
            answer.put("error", snifferHandler.getError());
            this.vertx.eventBus().publish("observer.new", answer.encode());
            rc.response().putHeader("content-type", "application/json").end(answer.encode());
        });

        router.route("/master/sniffing/getData/").handler(rc -> {
            JsonArray array = snifferHandler.getResult();
            rc.response().putHeader("content-type", "application/json").end(array.encode());
        });

        router.post("/master/fakebts/start/").handler(rc -> {
            final JsonObject reqJson = new JsonObject();
            final Map<String, String> params = new HashMap<>();

            rc.request().bodyHandler(h -> {
                parseJsonParams(params, reqJson, h);
                // Building the JSON on server side sent by client
                for (String key : params.keySet()) {
                    reqJson.put(key, params.get(key));
                }

                // retrieving the operator name from HTML page
                String info = reqJson.getString("info");

                // Calling python script to launch the sniffing
                String[] pythonLocationScript = {"./badimsicore_openbts.py", "start"};

                Thread thread = new Thread(() -> {
                    PythonCaller pc = new PythonCaller(pythonLocationScript, (in, out, err, returnCode) -> {
                        JsonObject answer = new JsonObject();
                        answer.put("started", "started");
                        rc.response().putHeader("content-type", "application/json").end(answer.encode());
                    });

                    try {
                        pc.exec();
                    } catch (IOException | InterruptedException ex) {
                        Logger.getLogger(BadIMSIService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                thread.start();
            });
        });

        router.get("/master/fakebts/stop").handler(rc -> {
            rc.request().bodyHandler(h -> {

                // Calling python script to launch the sniffing
                String[] pythonLocationScript = {"./badimsicore_openbts.py", "stop"};

                Thread thread = new Thread(() -> {
                    PythonCaller pc = new PythonCaller(pythonLocationScript, (in, out, err, returnCode) -> {
                        JsonObject answer = new JsonObject();
                        answer.put("started", "started");
                        rc.response().putHeader("content-type", "application/json").end(answer.encode());
                    });

                    try {
                        pc.exec();
                    } catch (IOException | InterruptedException ex) {
                        Logger.getLogger(BadIMSIService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                thread.start();
            });
        });

        // return the list of all BTS discovered in the sniffing step
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
                sb.delete(sb.length() - 1, sb.length());
                jsonObject.put("ARFCNs", sb.toString());
                array.add(jsonObject);
            });
            rc.response().putHeader("content-type", "application/json").end(array.encode());
        });

        router.post("/master/jamming/start/").handler(rc -> {
            final JsonObject reqJson = new JsonObject();
            final Map<String, String> params = new HashMap<>();

            rc.request().bodyHandler(h -> {
                parseJsonParams(params, reqJson, h);
                // Building the JSON on server side sent by client
                for (String key : params.keySet()) {
                    reqJson.put(key, params.get(key));
                }
                String operator = reqJson.getString("operator");
                if (operator != null) {
                    // We have to give the right response
                    rc.response().putHeader("content-type", "application/json").end(new JsonObject().put("state", "start").put("operator", operator).encode());
                }
            });
        });

        router.get("/master/jamming/stop").handler(rc -> {
            // We have to give the right response
            rc.response().putHeader("content-type", "application/json").end(new JsonObject().put("state", "stop").encode());

            /*
			 * String[] pythonLocationScript =
			 * {PythonCaller.getContextPath()+"badimsicore","-j",name};
			 * PythonCaller pc = new PythonCaller(pythonLocationScript); int
			 * exitValue = -1; try { exitValue = pc.process(); } catch
			 * (Exception e) { e.printStackTrace(); }
			 * 
			 * if(exitValue == 0) { rc.response() .putHeader("content-type",
			 * "application/json") .end(new JsonObject().put("jamming",
			 * pc.getResultSb()) .encode()); }
             */
        });

        router.post("/master/attack/sms/send/").handler(rc -> {
            // We have to give the right response
            final JsonObject reqJson = new JsonObject();
            final Map<String, String> params = new HashMap<>();

            rc.request().bodyHandler(h -> {
                parseJsonParams(params, reqJson, h);
                for (String key : params.keySet()) {
                    reqJson.put(key, params.get(key));
                }

                String sender = reqJson.getString("sender");
                String msg = reqJson.getString("message");
                String imsi = reqJson.getString("imsi");

                String[] cmd = {PythonCaller.getContextPath() + "badimsicore_sms_sender.py", "-s", imsi, sender, msg};
                PythonCaller pythonCaller = new PythonCaller(cmd, (input, output, error, returnCode) -> {
                    if (returnCode == 0) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
                        bufferedReader.lines().forEach(l -> {
                            System.out.println(l);
                        });
                    } else {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(error));
                        bufferedReader.lines().forEach(l -> {
                            System.out.println(l);
                        });
                    }
                });

                try {
                    pythonCaller.exec();
                } catch (IOException | InterruptedException ie) {
                    System.out.println("File not found");
                }
            });

        });

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(new BridgeOptions().addOutboundPermitted(new PermittedOptions())));

        router.route().handler(StaticHandler.create());
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
