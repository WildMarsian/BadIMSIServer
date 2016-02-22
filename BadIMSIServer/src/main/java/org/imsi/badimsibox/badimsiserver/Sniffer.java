/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.imsi.badimsibox.badimsiserver.BadIMSIService.operatorList;

public class Sniffer {

    final private Object lock = new Object();
    final private JsonArray array = new JsonArray();
    final private AtomicBoolean isOver = new AtomicBoolean(false);
    private Exception error = null;
    private Thread thread;
    
    private PythonCaller pc =null;

    public JsonArray getResult() {
        synchronized (lock) {
        	pc.process();
            return array;
        }
    }

    boolean start(JsonObject reqJson, String[] pythonLocationScript) {
        thread = new Thread(() -> {
            pc = new PythonCaller(pythonLocationScript, (in, out, err, returnCode) -> {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                    br.lines().filter(line -> line.startsWith("->")).map(line -> line.split(",")).forEach((tab) -> {
                        // System.out.println(tab);
                        List<String> arfcns = new ArrayList<>(tab.length - 4);
                        for (int i = 4; i < tab.length; i++) {
                            arfcns.add(tab[i]);
                        }
                        operatorList.add(new Bts(tab[0].split(" ")[1], tab[1], tab[2], tab[3], arfcns));
                    });
                } catch (IOException ex) {
                    Logger.getLogger(Sniffer.class.getName()).log(Level.SEVERE, null, ex);
                    error = ex;
                }
            });

            try {
                pc.exec();
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(Sniffer.class.getName()).log(Level.SEVERE, null, ex);
                isOver.set(true);
                error = ex;
            }

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
                synchronized (lock) {
                    array.add(jsonObject);
                }
            });
        });

        thread.start();
        return isOver.get();
    }

    public String status() {
        if (error != null) {
            return "error";
        }

        if (isOver.get()) {
            return "finished";
        } else {
            return "inProgress";
        }
    }

    public String getError() {
        synchronized (lock) {
            if (error == null) {
                return "";
            } else {
                return error.getLocalizedMessage();
            }
        }
    }
}
