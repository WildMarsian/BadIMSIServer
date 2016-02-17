package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class NewSmsObserver implements Observer {
	private final Vertx vertx;
	
	
	public NewSmsObserver(Observable observable, Vertx vertx) {
		this.vertx = vertx;
		observable.attach(this);
	}


	@Override
	public void update() {
		vertx.eventBus()
		.publish("sms.new", new JsonObject().put("message", "new sms"));
	}
}