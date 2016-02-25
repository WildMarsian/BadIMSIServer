package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.Vertx;

/**
 *
 * @author
 */
public class BadIMSIServer {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        BadIMSIService badIMSIService = new BadIMSIService();
        vertx.deployVerticle(badIMSIService);
    }
}
