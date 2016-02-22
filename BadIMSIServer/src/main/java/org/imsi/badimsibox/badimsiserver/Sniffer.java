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
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.imsi.badimsibox.badimsiserver.BadIMSIService.operatorList;

public class Sniffer {

    JsonArray launch(JsonObject reqJson) throws InterruptedException, IOException {

        // retrieving the operator name from HTML page
        String operator = reqJson.getString("operator");

        // Calling python script to launch the sniffing
        String[] pythonLocationScript = {"badimsicore-listen.py", "-o", operator};
        // -b => frequency band ex : GSM800
        // -t => time to scan each frequency
        // -n => number of cycle to scan frequencies
        
        PythonCaller pc = new PythonCaller(pythonLocationScript, (in, out, err, returnCode) -> {
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
            }
        });
        
        pc.exec();

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
        return array;
    }
}
