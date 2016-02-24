package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.Vertx;

public class BadIMSIServer {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        BadIMSIService badIMSIService = new BadIMSIService(vertx);
        vertx.deployVerticle(badIMSIService);
    }
}
