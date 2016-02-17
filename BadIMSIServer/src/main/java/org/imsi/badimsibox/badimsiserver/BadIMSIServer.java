
package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.Vertx;


public class BadIMSIServer {
	
    public static void main(String[] args) {
    	Vertx vertx = Vertx.vertx();
    	
    	SmsManager smsManager = new SmsManager();
    	BadIMSIService badIMSIService = new BadIMSIService(vertx);
    	
        vertx.deployVerticle(badIMSIService);
        
        new NewSmsObserver(smsManager, vertx);
        
        try {
        	Thread.sleep(5000);
			badIMSIService.getAllSms();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        try {
            while(!Thread.interrupted()) {
            	//launch the service here
            	
            }	
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
	}
}
