package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.json.JsonObject;

public class Target {

	private final String IMSI;
	private final String TMSI;
	private final String IMEI;

	public Target(String IMSI, String TMSI, String IMEI) {
		this.IMSI = IMSI;
		this.TMSI = TMSI;
		this.IMEI = IMEI;
	}
	
	public String getIMEI() {
		return IMEI;
	}
	
	public String getIMSI() {
		return IMSI;
	}
	
	public String getTMSI() {
		return TMSI;
	}
	
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("IMSI",IMSI);
		json.put("TMSI", TMSI);
		json.put("IMEI", IMEI);
		return json;
	}
	
}
