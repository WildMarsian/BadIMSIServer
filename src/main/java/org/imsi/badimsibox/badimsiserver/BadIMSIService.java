package org.imsi.badimsibox.badimsiserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author AisukoWasTaken AlisterWan TTAK WarenUT
 */
public class BadIMSIService extends AbstractVerticle {

    static String defaultHeaders = "Origin, X-Requested-With, Content-Type, Accept";
    static String defaultMethods = "GET, POST, OPTIONS, PUT, HEAD, DELETE, CONNECT";
    static String defaultIpAndPorts = "*";

    private final PythonManager pythonManager = new PythonManager();
    private final List<BTS> operatorList = new ArrayList<>();
    private Session currentSession = Session.init(vertx);

    /**
     *
     * @param params
     * @param reqJson
     * @param h
     */
    private void parseJsonParams(Map<String, String> params, Buffer h) {
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
     */
    public void getAllSms() throws IOException {
        /* TODO make objects to launch the SMSReader in another thread. 
            One route to launch and another to get the current status. The last to get all data readed*/

 /*
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
        }*/
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

        // Sessions
        router.get("/master/session/start/:password").handler(this::startSession);
        router.get("/master/session/state").handler(this::getSessionState);
        router.get("/master/session/set/state/:state").handler(this::setSessionState);

        // Sniffing
        router.post("/master/sniffing/selectOperator/").handler(this::selectOperatorToSniff);
        router.post("/master/sniffing/start/").handler(this::startSniffing);

        // OpenBTS
        router.post("/master/fakebts/start/").handler(this::startOpenBTS);
        router.post("/master/fakebts/selectOperator/").handler(this::selectOperatorToSpoof);
        router.post("/master/fakebts/stop/").handler(this::stopOpenBTS);

        // Jamming
        router.post("/master/jamming/start/").handler(this::startJamming);
        router.route("/master/jamming/operator/").handler(this::selectOperatorToJamm);
        router.get("/master/jamming/stop").handler(this::stopJamming);

        // SMS
        router.post("/master/attack/sms/send/").handler(this::sendSMS);

        // Creating routes
        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(new BridgeOptions().addOutboundPermitted(new PermittedOptions())));
        router.route().handler(StaticHandler.create());
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    /**
     *
     * @param rc
     */
    private void startSession(RoutingContext rc) {
        String password = rc.request().getParam("password");
        //System.out.println(new Date() + ": Starting a session");
        this.currentSession = new Session(password, vertx);
        this.currentSession.updateTimestamp();
        rc.response().putHeader("content-type", "application/json").end(
                new JsonObject().put("started", true).encode()
        );
    }

    /**
     *
     * @param rc
     */
    private void getSessionState(RoutingContext rc) {
        if (this.currentSession == null) {
            rc.response().putHeader("content-type", "application/json").end(
                    new JsonObject().put("state", -1).encode()
            );
        } else {
            rc.response().putHeader("content-type", "application/json").end(
                    new JsonObject()
                    .put("state", this.currentSession.getSessionState())
                    .put("password", this.currentSession.getPassword())
                    .put("timestamp", this.currentSession.getTimestamp())
                    .encode()
            );
        }
    }

    /**
     *
     * @param rc
     */
    private void setSessionState(RoutingContext rc) {
        String stateString = rc.request().getParam("state");
        int stateInt = Integer.parseInt(stateString);
        boolean stateChanged = (stateInt > -1) && (stateInt < 5)
                && (this.currentSession.getSessionState() < stateInt);
        if (stateChanged) {
            this.currentSession.nextSessionState();
        }
        rc.response().putHeader("content-type", "application/json").end(
                new JsonObject().put("stateChanged", stateChanged).encode()
        );
    }

    /**
     *
     * @param rc
     */
    private void selectOperatorToSniff(RoutingContext rc) {
        final JsonObject reqJson = new JsonObject();
        final Map<String, String> params = new HashMap<>();
        rc.request().bodyHandler(h -> {
            parseJsonParams(params, h);
            for (String key : params.keySet()) {
                reqJson.put(key, params.get(key));
            }
            String operator = reqJson.getString("operator");
            JsonObject json = new JsonObject();
            json.put("operator", operator);
            this.vertx.eventBus().publish("observer.new", json.encode());
            rc.response().putHeader("content-type", "application/json").end(
                    new JsonObject().put("selectReceived", true).encode()
            );
        });
    }

    /**
     *
     * @param rc
     */
    private void startSniffing(RoutingContext rc) {
        final JsonObject reqJson = new JsonObject();
        final Map<String, String> params = new HashMap<>();

        rc.request().bodyHandler(h -> {
            parseJsonParams(params, h);
            // Building the JSON on server side sent by client
            for (String key : params.keySet()) {
                reqJson.put(key, params.get(key));
            }

            // retrieving the operator name from HTML page
            String operator = reqJson.getString("operator");
            String command = "badimsicore_listen -o " + operator;

            // Blocking code
            vertx.executeBlocking(future -> {
                try {
                    Process p = pythonManager.run(command);
                    // Give the return Object to the treatment block code
                    p.waitFor(10, TimeUnit.MINUTES);
                    future.complete(p);
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(BadIMSIService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }, res -> {
                Process p = (Process) res.result();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                br.lines().filter(line -> line.startsWith("->")).map(line -> line.split(","))
                        .forEach((tab) -> {
                            List<String> arfcns = new ArrayList<>(tab.length - 4);
                            for (int i = 4; i < tab.length; i++) {
                                arfcns.add(tab[i]);
                            }
                            operatorList.add(
                                    new BTS(tab[0].split(" ")[1], tab[1], tab[2], tab[3], arfcns)
                            );
                        });
                JsonObject answer = new JsonObject();
                if (p.exitValue() == 0) {
                    answer.put("status", "data");
                    JsonArray array = new JsonArray();
                    operatorList.forEach(bts -> {
                        JsonObject tab = new JsonObject();
                        tab.put("Network", bts.getOperatorByMnc());
                        tab.put("MCC", bts.getOperator().getMcc());
                        tab.put("LAC", bts.getLac());
                        tab.put("CI", bts.getCi());
                        StringBuilder sb = new StringBuilder();
                        bts.getArfcn().forEach(arfcn -> {
                            sb.append(arfcn);
                            sb.append(", ");
                        });
                        sb.delete(sb.length() - 1, sb.length());
                        tab.put("ARFCNs", sb.toString());
                        array.add(tab);
                    });
                    answer.put("content", array);
                } else {
                    answer.put("status", "error");
                    BufferedReader bf = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    StringBuilder str = new StringBuilder();
                    bf.lines().forEach(line -> {
                        str.append(line);
                    });
                    answer.put("content", str.toString());
                }
                rc.response().putHeader("content-type", "application/json").end(
                        answer.encode()
                );
            });
        });
    }

    /**
     *
     * @param rc
     */
    private void startJamming(RoutingContext rc) {
        final JsonObject reqJson = new JsonObject();
        final Map<String, String> params = new HashMap<>();

        rc.request().bodyHandler(h -> {
            parseJsonParams(params, h);
            for (String key : params.keySet()) {
                reqJson.put(key, params.get(key));
            }
            String operator = reqJson.getString("operator");

            String command = "jamming start";

            vertx.executeBlocking(future -> {
                try {
                    Process p = pythonManager.run(command);
                    // Give the return Object to the treatment block code
                    p.waitFor(10, TimeUnit.MINUTES);
                    future.complete(p);
                } catch (InterruptedException | IOException ex) {
                    Logger.getLogger(BadIMSIService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }, res -> {
                Process p = (Process) res.result();
                JsonObject answer = new JsonObject();
                answer.put("started", (p.exitValue() == 0));
                rc.response().putHeader("content-type", "application/json").end(
                        answer.encode()
                );
            });
        });
    }

    /**
     *
     * @param rc
     */
    private void selectOperatorToJamm(RoutingContext rc) {
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
        rc.response().putHeader("content-type", "application/json").end(
                array.encode()
        );
    }

    /**
     *
     * @param rc
     */
    private void stopJamming(RoutingContext rc) {
        final JsonObject reqJson = new JsonObject();
        final Map<String, String> params = new HashMap<>();

        rc.request().bodyHandler(h -> {
            parseJsonParams(params, h);
            for (String key : params.keySet()) {
                reqJson.put(key, params.get(key));
            }
            String operator = reqJson.getString("operator");

            String command = "jamming stop";

            vertx.executeBlocking(future -> {
                try {
                    Process p = pythonManager.run(command);
                    // Give the return Object to the treatment block code
                    p.waitFor(10, TimeUnit.MINUTES);
                    future.complete(p);
                } catch (InterruptedException | IOException ex) {
                    Logger.getLogger(BadIMSIService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }, res -> {
                Process p = (Process) res.result();
                JsonObject answer = new JsonObject();
                answer.put("started", (p.exitValue() == 0));
                rc.response().putHeader("content-type", "application/json").end(
                        answer.encode()
                );
            });
        });
    }

    /**
     *
     * @param rc
     */
    private void startOpenBTS(RoutingContext rc) {
        final JsonObject reqJson = new JsonObject();
        final Map<String, String> params = new HashMap<>();

        rc.request().bodyHandler(h -> {
            parseJsonParams(params, h);
            // Building the JSON on server side sent by client
            for (String key : params.keySet()) {
                reqJson.put(key, params.get(key));
            }

            // Calling python script to launch the sniffing
            String command = "badimsicore_openbts start";

            vertx.executeBlocking(future -> {
                try {
                    Process p = pythonManager.run(command);
                    // Give the return Object to the treatment block code
                    p.waitFor(10, TimeUnit.MINUTES);
                    future.complete(p);
                } catch (InterruptedException | IOException ex) {
                    Logger.getLogger(BadIMSIService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }, res -> {
                Process p = (Process) res.result();
                JsonObject answer = new JsonObject();
                answer.put("started", (p.exitValue() == 0));
                rc.response().putHeader("content-type", "application/json").end(
                        answer.encode()
                );
            });
        });
    }

    /**
     *
     * @param rc
     */
    private void selectOperatorToSpoof(RoutingContext rc) {
        final JsonObject reqJson = new JsonObject();
        final Map<String, String> params = new HashMap<>();
        rc.request().bodyHandler(h -> {
            parseJsonParams(params, h);
            for (String key : params.keySet()) {
                reqJson.put(key, params.get(key));
            }
            String operator = reqJson.getString("operator");
            JsonObject json = new JsonObject();
            json.put("operator", operator);
            this.vertx.eventBus().publish("observer.new", json.encode());
            rc.response().putHeader("content-type", "application/json").end(
                    new JsonObject().put("selectReceived", true).encode()
            );
        });
    }

    /**
     *
     * @param rc
     */
    private void stopOpenBTS(RoutingContext rc) {
        final JsonObject reqJson = new JsonObject();
        final Map<String, String> params = new HashMap<>();

        rc.request().bodyHandler(h -> {
            parseJsonParams(params, h);
            // Building the JSON on server side sent by client
            for (String key : params.keySet()) {
                reqJson.put(key, params.get(key));
            }

            // Calling python script to launch the sniffing
            String command = "badimsicore_openbts stop";

            vertx.executeBlocking(future -> {
                try {
                    Process p = pythonManager.run(command);
                    // Give the return Object to the treatment block code
                    p.waitFor(10, TimeUnit.MINUTES);
                    future.complete(p);
                } catch (InterruptedException | IOException ex) {
                    Logger.getLogger(BadIMSIService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }, res -> {
                Process p = (Process) res.result();
                JsonObject answer = new JsonObject();
                answer.put("stopped", (p.exitValue() == 0));
                rc.response().putHeader("content-type", "application/json").end(
                        answer.encode()
                );
            });
        });
    }

    /**
     *
     * @param rc
     */
    private void sendSMS(RoutingContext rc) {
        final JsonObject reqJson = new JsonObject();
        final Map<String, String> params = new HashMap<>();

        rc.request().bodyHandler(h -> {
            parseJsonParams(params, h);
            for (String key : params.keySet()) {
                reqJson.put(key, params.get(key));
            }

            String sender = reqJson.getString("sender");
            String msg = reqJson.getString("message");
            String imsi = reqJson.getString("imsi");

            String command = "badimsicore_sns_sender -s " + imsi + " " + sender + " " + msg;

            vertx.executeBlocking(future -> {
                try {
                    Process p = pythonManager.run(command);
                    // Give the return Object to the treatment block code
                    p.waitFor(10, TimeUnit.MINUTES);
                    future.complete(p);
                } catch (InterruptedException | IOException ex) {
                    Logger.getLogger(BadIMSIService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }, res -> {
                Process p = (Process) res.result();

                InputStream processStream = null;
                if (p.exitValue() == 0) {
                    processStream = p.getInputStream();
                } else {
                    processStream = p.getErrorStream();
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(processStream));
                br.lines().forEach(l -> {
                    System.out.println(l);
                });

                JsonObject answer = new JsonObject();
                answer.put("stopped", (p.exitValue() == 0));
                rc.response().putHeader("content-type", "application/json").end(
                        answer.encode()
                );
            });
        });
    }
}
