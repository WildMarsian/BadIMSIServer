
package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.Vertx;


public class BadIMSIServer {
	
    public static void main(String[] args) {
    	Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new BadIMSIService());
	}
}
