
package org.imsi.badimsibox.badimsiserver;

import java.nio.file.Files;
import java.nio.file.Paths;

import io.vertx.core.Vertx;

public class BadIMSIServer {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();

		SmsManager smsManager = new SmsManager();
		BadIMSIService badIMSIService = new BadIMSIService(vertx);

		vertx.deployVerticle(badIMSIService);

		new NewSmsObserver(smsManager, vertx);
		
		try {
			while (!Thread.interrupted()) {
				// launch the service here
				Thread.sleep(5000);
				badIMSIService.getAllSms();
				Files.delete(Paths.get("./scripts/smqueue.txt.offset"));
				//badIMSIService.getTMSIs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
