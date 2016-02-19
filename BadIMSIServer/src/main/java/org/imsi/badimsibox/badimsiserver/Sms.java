package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.json.JsonObject;

public class Sms {

	private final String date;
	private final String message;

	public Sms(String date, String message) {
		this.date = date;
		this.message = message;
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("date", date);
		json.put("message", message);
		return json;
	}

	public String getDate() {
		return date;
	}

	public String getMessage() {
		return message;
	}
}
