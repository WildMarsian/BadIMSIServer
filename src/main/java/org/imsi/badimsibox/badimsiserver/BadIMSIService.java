package org.imsi.badimsibox.badimsiserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;

/**
 * Class containing all treatment methods with access by the Web interface using
 * routes
 *
 * @author AisukoWasTaken AlisterWan TTAK WarenUT Andrebreton
 */
public class BadIMSIService extends AbstractVerticle {

    static String defaultHeaders = "x-requested-with, Content-Type, origin, authorization, accept, client-security-token";
    static String defaultMethods = "GET, POST, OPTIONS, PUT, HEAD, DELETE, CONNECT";
    static String defaultIpAndPorts = "*";

    private final PythonManager pythonManager = new PythonManager();
    private final ArrayList<Bts> operatorList = new ArrayList<>();
    private final HashSet<MobileTarget> targetList = new HashSet<>();
    private final ArrayList<String> command = new ArrayList<>();

    private Session currentSession = Session.init(vertx);
    private SynchronousThreadManager smsThreadManager = null;
    private SynchronousThreadManager tmsiThreadManager = null;

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
        router.route("/master/session/destroy/").handler(this::destroySession);

        // Sniffing
        router.post("/master/sniffing/selectOperator/").handler(this::selectOperatorToSniff);
        router.post("/master/sniffing/selectBand/").handler(this::selectBandToSniff);
        router.post("/master/sniffing/start/").handler(this::startSniffing);

        // OpenBTS
        router.post("/master/fakebts/start/").handler(this::startOpenBTS);
        router.post("/master/fakebts/selectOperator/").handler(this::selectOperatorToSpoof);
        router.route("/master/fakebts/getBTSList/").handler(this::getBTSList);
        router.post("/master/fakebts/stop").handler(this::stopOpenBTS);

        // Jamming
        router.post("/master/jamming/start/").handler(this::startJamming);
        router.route("/master/jamming/operator/").handler(this::selectOperatorToJamm);
        router.get("/master/jamming/stop/").handler(this::stopJamming);

        // SMS
        router.post("/master/attack/sms/send/").handler(this::sendSMS);
        router.route("/master/attack/sms/receive/").handler(this::launchSmsReceptor);
        router.route("/master/attack/tmsis/").handler(this::getTmsiList);

        // Handle EventBus
        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(new BridgeOptions().addOutboundPermitted(new PermittedOptions())));
        router.route().handler(StaticHandler.create());
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    /**
     * Used to initialise a session for all Observers of the Master
     *
     * @param rc : The routing context handling the VertX data
     */
    private void startSession(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Starting a new session");
        String password = rc.request().getParam("password");
        this.currentSession = new Session(password, vertx);
        this.currentSession.updateTimestamp();
        rc.response().putHeader("content-type", "application/json").end(
                new JsonObject().put("started", true).encode()
        );
    }

    /**
     * Used to check the status of the current session
     *
     * @param rc : The routing context handling the VertX data
     */
    private void getSessionState(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Sending interface the current session state");
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
     * Used to update to state of the current session
     *
     * @param rc : The routing context handling the VertX data
     */
    private void setSessionState(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Updating the current session state");
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
     * Used to delete the current session
     *
     * @param rc : The routing context handling the VertX data
     */
    private void destroySession(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Trying to destroy the current session");
        currentSession = Session.init(vertx);
        if (smsThreadManager != null) {
            smsThreadManager.stop();
            BadIMSILogger.getLogger().log(Level.FINE, "Stopping SMS Manager");
        }
        if (tmsiThreadManager != null) {
            tmsiThreadManager.stop();
            BadIMSILogger.getLogger().log(Level.FINE, "Stopping TMSI Manager");
        }
    }

    /**
     * Used to select an operator for the sniffing process
     *
     * @param rc : The routing context handling the VertX data
     */
    private void selectOperatorToSniff(RoutingContext rc) {
        rc.request().bodyHandler(h -> {
            JsonObject reqJson = formatJsonParams(h);
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
     * Used to select the ARFCN band to sniff
     *
     * @param rc : The routing context handling the VertX data
     */
    private void selectBandToSniff(RoutingContext rc) {
        rc.request().bodyHandler(h -> {
            JsonObject reqJson = formatJsonParams(h);

            String operator = reqJson.getString("band");
            JsonObject json = new JsonObject();
            json.put("band", operator);
            this.vertx.eventBus().publish("observer.new", json.encode());
            rc.response().putHeader("content-type", "application/json").end(
                    new JsonObject().put("selectReceived", true).encode()
            );
        });
    }

    /**
     * Used to launch the sniffing attack
     *
     * @param rc : The routing context handling the VertX data
     */
    private void startSniffing(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Starting sniffing process");
        if (smsThreadManager != null) {
            smsThreadManager.stop();
        }

        rc.request().bodyHandler(h -> {
            JsonObject reqJson = formatJsonParams(h);
            signalObserverForStartingLongTreatment();

            operatorList.clear();

            // Setting command to execute
            command.clear();
            command.add("badimsicore_listen");
            command.add("-o");
            command.add(reqJson.getString("operator"));
            String band = reqJson.getString("band");
            if (band != null && !band.equalsIgnoreCase("")) {
                command.add("-b");
                command.add(band);
            }

            // Blocking code to execute in another thread until it answer
            vertx.executeBlocking(future -> {
                launchAndWait(future);
            }, res -> {
                JsonObject answer = new JsonObject();
                if (res.succeeded()) {
                    Process p = (Process) res.result();
                    BufferedReader br
                            = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    br.lines().filter(line -> line.startsWith("->"))
                            .map(line -> line.split(","))
                            .forEach((tab) -> {
                                List<String> arfcns = new ArrayList<>(tab.length - 4);
                                for (int i = 4; i < tab.length; i++) {
                                    arfcns.add(tab[i]);
                                }
                                operatorList.add(new Bts(tab[0].split(" ")[1], tab[1], tab[2], tab[3], arfcns));
                            });
                    answer.put("status", "data");
                    JsonArray array = extractAndFormatBtsList();
                    answer.put("content", array);
                    BadIMSILogger.getLogger().log(Level.FINE, "Sniffing process end with success");
                } else {
                    answer.put("status", "error");
                    answer.put("content", res.cause().getMessage());
                    BadIMSILogger.getLogger().log(Level.SEVERE, "There was a problem with the sniffing process", res.cause());
                }
                this.vertx.eventBus().publish("observer.new", answer.encode());
                rc.response().putHeader("content-type", "application/json").end(
                        answer.encode()
                );
            });
        });
    }

    /**
     * Used to start the jamming process
     *
     * @param rc : The routing context handling the VertX data
     */
    private void startJamming(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Starting the jammer process");
        command.clear();
        command.add("jamming");
        command.add("start");

        vertx.executeBlocking(future -> {
            launchAndWait(future);
        }, res -> {
            JsonObject answer = new JsonObject();
            answer.put("started", res.succeeded());
            if (res.failed()) {
                BadIMSILogger.getLogger().log(Level.SEVERE, "Starting jamming process failed");
                answer.put("error", res.cause().getMessage());
            } else {
                BadIMSILogger.getLogger().log(Level.FINE, "Starting jamming process with success");
            }
            rc.response().putHeader("content-type", "application/json").end(
                    answer.encode()
            );
        });
    }

    /**
     * Used to select the jamming target inside the sniffing listening list
     *
     * @param rc : The routing context handling the VertX data
     */
    private void selectOperatorToJamm(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Selecting operator to jam");
        JsonArray array = extractAndFormatBtsList();
        rc.response().putHeader("content-type", "application/json").end(
                array.encode()
        );
    }

    /**
     * Used to stop the jamming operation
     *
     * @param rc : The routing context handling the VertX data
     */
    private void stopJamming(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Stopping the jammer process");
        command.clear();
        command.add("jamming");
        command.add("stop");

        vertx.executeBlocking(future -> {
            launchAndWait(future);
        }, res -> {
            JsonObject answer = new JsonObject();
            answer.put("stopped", res.succeeded());
            if (res.failed()) {
                BadIMSILogger.getLogger().log(Level.SEVERE, "Stopping jamming process failed");
                answer.put("error", res.cause().getMessage());
            } else {
                BadIMSILogger.getLogger().log(Level.FINE, "Stopping jamming process with success");
            }
            rc.response().putHeader("content-type", "application/json").end(
                    answer.encode()
            );
        });
    }

    /**
     * Used to launch the OpenBTS service
     *
     * @param rc : The routing context handling the VertX data
     */
    private void startOpenBTS(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.FINE, "Starting OpenBTS process");
        rc.request().bodyHandler(h -> {
            JsonObject reqJson = formatJsonParams(h);
            String ci = reqJson.getString("CI");
            Bts selectedOperator = null;

            for (Bts operator : operatorList) {
                if (operator.getCi().equals(ci)) {
                    selectedOperator = operator;
                    break;
                }
            }

            if (selectedOperator == null) {
                BadIMSILogger.getLogger().log(Level.SEVERE, "There was no choice selected into the sniffing operation list");
            }

            command.clear();
            command.add("badimsicore_openbts");
            command.add("start");
            command.add("-i");
            command.add(selectedOperator.getCi());
            command.add("-l");
            command.add(selectedOperator.getLac());
            command.add("-n");
            command.add(selectedOperator.getOperator().getMnc());
            command.add("-c");
            command.add(selectedOperator.getOperator().getMcc());
            command.add("-p");
            command.add("\".*\"");

            vertx.executeBlocking(future -> {
                launchAndWait(future);
            }, res -> {
                JsonObject answer = new JsonObject();
                answer.put("started", res.succeeded());
                if (res.failed()) {
                    answer.put("error", res.cause().getMessage());
                    BadIMSILogger.getLogger().log(Level.SEVERE, "Starting OpenBTS service failed", res.cause());
                } else {
                    BadIMSILogger.getLogger().log(Level.FINE, "Starting OpenBTS service with success, launching TMSI receptor thread");
                    launchTmsiReceptor(rc);
                }
                rc.response().putHeader("content-type", "application/json").end(
                        answer.encode()
                );
            });
        });
    }

    /**
     * Used to export the BTS sniffing list
     *
     * @param rc : The routing context handling the VertX data
     */
    private void getBTSList(RoutingContext rc) {
        JsonArray array = extractAndFormatBtsList();
        rc.response().putHeader("content-type", "application/json").end(
                array.encode()
        );
    }

    /**
     * Used to select the operator to spoof
     *
     * @param rc : The routing context handling the VertX data
     */
    private void selectOperatorToSpoof(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Selecting operator to spoof");
        rc.request().bodyHandler(h -> {
            JsonObject reqJson = formatJsonParams(h);
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
     * Used to stop the OpenBTS service
     *
     * @param rc : The routing context handling the VertX data
     */
    private void stopOpenBTS(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Stopping OpenBTS service");
        command.clear();
        command.add("badimsicore_openbts");
        command.add("stop");

        vertx.executeBlocking(future -> {
            launchAndWait(future);
        }, res -> {
            JsonObject answer = new JsonObject();
            answer.put("stopped", res.succeeded());
            if (res.failed()) {
                BadIMSILogger.getLogger().log(Level.SEVERE, "Stopping OpenBTS service failed", res.cause());
                answer.put("error", res.cause().getMessage());
            } else {
                BadIMSILogger.getLogger().log(Level.FINE, "Stopping OpenBTS service with success");
            }
            rc.response().putHeader("content-type", "application/json").end(
                    answer.encode()
            );
        });
    }

    /**
     * Used to prepare and send SMS to identified Mobile Targets
     *
     * @param rc : The routing context handling the VertX data
     */
    private void sendSMS(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Sending new SMS to Mobile station");
        if (tmsiThreadManager != null) {
            tmsiThreadManager.stop();
        }
        rc.request().bodyHandler(h -> {
            JsonObject reqJson = formatJsonParams(h);

            String msisdnSender = reqJson.getString("sender");
            String msg = reqJson.getString("message");
            String imsi = reqJson.getString("imsi");

            command.clear();
            command.add("badimsicore_sms_sender");

            if (imsi != null && !imsi.isEmpty()) {
                command.add("-r");
                command.add(imsi);
            }

            if (msisdnSender != null && !msisdnSender.isEmpty()) {
                command.add("-s");
                command.add(msisdnSender);
            }

            if (msg != null && !msg.isEmpty()) {
                command.add("-m");
                command.add(msg);
            }

            vertx.executeBlocking(future -> {
                launchAndWait(future);
            }, res -> {
                JsonObject answer = new JsonObject();
                answer.put("sended", res.succeeded());
                if (res.succeeded()) {
                    BadIMSILogger.getLogger().log(Level.FINE, "Sending SMS with success");
                    Sms sms = new Sms(Date.from(Instant.now()).toString(), msg);
                    vertx.eventBus().publish("sms.sent", sms.toJson());
                } else {
                    BadIMSILogger.getLogger().log(Level.SEVERE, "Sending SMS failed", res.cause());
                    answer.put("error", res.cause().getMessage());
                }

                rc.response().putHeader("content-type", "application/json").end(
                        answer.encode()
                );
            });
        });
    }

    /**
     * Used to launch a new Thread in along the main process to receive SMS in
     * live time
     *
     * @param rc : The routing context handling the VertX data
     */
    private void launchSmsReceptor(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Launching the SMS reception service");
        command.clear();
        command.add("badimsicore_sms_interceptor");
        command.add("-i");
        command.add("/var/log/syslog");
        System.out.println("Launching SMS receptor...");

        smsThreadManager = SynchronousThreadManager.createSynchronousThread(command.stream().toArray(String[]::new), "SMS Receptor", 5000, 5);
        vertx.executeBlocking(future -> {
            smsThreadManager.start((in, out) -> {
                BadIMSILogger.getLogger().log(Level.INFO, "Extracting SMS");
                BufferedReader bf
                        = new BufferedReader(new InputStreamReader(in));
                bf.lines().forEach(line -> {
                    String removeParenthesis = line.replaceAll("[()]", "");
                    String[] splitted = removeParenthesis.split(",");
                    String first = splitted[0].replaceAll("['']", "");
                    String second = splitted[1].replaceAll("['']", "");
                    BadIMSILogger.getLogger().log(Level.INFO, "Sending SMS content to the Web interface");
                    vertx.eventBus().publish("sms.new", new Sms(first, second).toJson());
                });
            });
        }, res -> {
            // Do nothing : never used
        });
    }

    /**
     * Used to launch a new Thread in along the main process to receive TMSI in
     * live time
     *
     * @param rc : The routing context handling the VertX data
     */
    private void launchTmsiReceptor(RoutingContext rc) {
        BadIMSILogger.getLogger().log(Level.INFO, "Launching the TMSI reception service");
        command.clear();

        command.add("badimsicore_tmsis");
        tmsiThreadManager = SynchronousThreadManager.createSynchronousThread(command.stream().toArray(String[]::new), "TMSI Receptor", 5000, 5);
        vertx.executeBlocking(future -> {
            tmsiThreadManager.start((in, out) -> {
                targetList.clear();
                BufferedReader bf = new BufferedReader(new InputStreamReader(in));
                bf.lines().skip(2).filter(line -> !line.equals("None")).forEach(line -> {
                    if (!line.isEmpty()) {
                        String[] words = line.split(" ");
                        MobileTarget target
                                = new MobileTarget(words[0], words[1], words[2], "", "", "", "");
                        targetList.add(target);
                    }
                });
                BadIMSILogger.getLogger().log(Level.INFO, "Creating JSON Object with TMSI received");
                JsonArray answer = new JsonArray();
                targetList.forEach(target -> {
                    answer.add(target.toJson());
                });
                vertx.eventBus().publish("imsi.new", answer);
            });
        }, res -> {
            // Do nothing : never used
        });
    }

    /**
     * Used to answer the Web interface by sending it all TMSI number idenfitied
     * during the sniffing process
     *
     * @param rc : The routing context handling the VertX data
     */
    private void getTmsiList(RoutingContext rc) {
        JsonArray answer = extractTmsiListInJson();
        rc.response().putHeader("content-type", "application/json").end(answer.encode()
        );
    }

    /**
     * Blocking code executed by another thread handle by VertX
     *
     * @param future : The future which will be executed
     */
    private void launchAndWait(Future<Object> future) {
        try {
            Process p = pythonManager.run(command.stream().toArray(String[]::new));
            p.waitFor();
            future.complete(p);
        } catch (InterruptedException | IOException ex) {
            BadIMSILogger.getLogger().log(Level.SEVERE, null, ex);
            future.fail(ex);
        }
    }

    /**
     * Used to extract correctly all TMSI identified from the tmsi list stored
     * by the tmsi receptor thread.
     *
     * @return a JsonArray contained all tmsi objects in JSON representation
     */
    private JsonArray extractTmsiListInJson() {
        JsonArray answer = new JsonArray();
        targetList.forEach(target -> {
            answer.add(target.toJson());
        });
        System.out.println(answer);
        return answer;
    }

    /**
     * Extract and format in JSON all Bts classes stored in the operator list
     * during the sniffing step
     *
     * @return a JsonArray containing all Bts as JsonObjects in the following
     * order : Network, MCC, LAC, CI and a list of ARFCN
     */
    private JsonArray extractAndFormatBtsList() {
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
            sb.delete(sb.length() - 2, sb.length());
            tab.put("ARFCNs", sb.toString());
            array.add(tab);
        });
        return array;
    }

    /**
     * Used to Map all post/get arguments sent by the Web interface to the
     * server and contained in the buffer
     *
     * @param params : The Map to fill with data
     * @param h : The buffer containing all data
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

    /**
     * Used to format the JSON objects received from the web interface and
     * containing post/get arguments
     *
     * @param h : The buffer containing the data
     */
    private JsonObject formatJsonParams(Buffer h) {
        JsonObject reqJson = new JsonObject();
        HashMap<String, String> params = new HashMap<>();
        parseJsonParams(params, h);
        params.keySet().stream().forEach((key) -> {
            reqJson.put(key, params.get(key));
        });
        return reqJson;
    }

    /**
     * Signal to the observer a long time treatment as begin
     */
    private void signalObserverForStartingLongTreatment() {
        JsonObject json = new JsonObject().put("started", true);
        this.vertx.eventBus().publish("observer.new", json.encode());
    }
}
