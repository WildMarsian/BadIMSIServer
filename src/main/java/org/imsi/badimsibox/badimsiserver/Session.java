package org.imsi.badimsibox.badimsiserver;

import java.util.Date;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class Session {
	private int state;
	private final String password;
	private int timestamp;
	private Vertx vertx;

	public Session(String password, Vertx vertx) {
		this.password = password;
		this.state = 0;
		this.timestamp = 0;
		this.vertx = vertx;
	}

	public static Session init(Vertx vertx) {
		Session init = new Session("", vertx);
		return init;
	}

	public boolean checkPassword(String inputPassword) {
		return inputPassword.equals(this.password);
	}

	public int getSessionState() {
		return this.state;
	}

	public void nextSessionState() {
		this.state++;
		System.out.println(new Date() + ": Next sesssion state: " + this.state);
		JsonObject json = new JsonObject();
		json.put("state", new String(Integer.toString(this.state)));
		System.out.println("Data sent to observer: " + json.encode());
		this.vertx.eventBus().publish("session.new", json.encode());
	}

	public String getPassword() {
		return this.password;
	}

	public void updateTimestamp() {
		System.out.println(new Date() + ": Updated timestamp");
		this.timestamp = (int) (System.currentTimeMillis() / 1000L);
	}

	public int getTimestamp() {
		return this.timestamp;
	}
}