package org.imsi.badimsibox.badimsiserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Sniffer {

    final private Object lock = new Object();
    final private List<Bts> operatorList = new ArrayList<>();
    private PythonHandler handler = null;

    /**
     *
     * @param pythonLocationScript
     */
    public void start(String[] pythonLocationScript) {
        handler = new PythonHandler(pythonLocationScript, (in, out, err) -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            br.lines().filter(line -> line.startsWith("->")).map(line -> line.split(",")).forEach((tab) -> {
                List<String> arfcns = new ArrayList<>(tab.length - 4);
                for (int i = 4; i < tab.length; i++) {
                    arfcns.add(tab[i]);
                }
                synchronized (lock) {
                    operatorList.add(new Bts(tab[0].split(" ")[1], tab[1], tab[2], tab[3], arfcns));
                }
            });
        });
    }

    /**
     *
     * @return
     */
    public List<Bts> getResult() {
        if (handler.getExitValue() == PythonHandler.PROCESSRUNNING) {
            return null;
        }
        return operatorList;
    }

    /**
     *
     * @return
     */
    public boolean isRunning() {
        return handler.getExitValue() == PythonHandler.PROCESSRUNNING;
    }

    /**
     *
     * @return
     */
    public boolean hasFailed() {
        return handler.getState() == PythonHandler.HandlerState.error;
    }

    /**
     *
     * @return
     */
    public String getError() {
        return handler.getError().getMessage();
    }

    /**
     *
     * @return
     */
    public String getStatusType() {
        return handler.getState().toString();
    }
}
