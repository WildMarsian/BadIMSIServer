package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.Vertx;

/**
 * Main class used to launch the VertX server
 * @author AisukoWasTaken TTAK WarenUT
 */
public class BadIMSIServer {

    /**
     * Main used to launch the VertX server
     * @param args : command line arguments
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        BadIMSIService badIMSIService = new BadIMSIService();
        vertx.deployVerticle(badIMSIService);
    }
}