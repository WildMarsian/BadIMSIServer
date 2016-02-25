package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.json.JsonObject;

/**
 *
 * @author thibaut
 */
public class Target {

    private final String IMSI;
    private final String TMSI;
    private final String IMEI;

    /**
     *
     * @param IMSI
     * @param TMSI
     * @param IMEI
     */
    public Target(String IMSI, String TMSI, String IMEI) {
        this.IMSI = IMSI;
        this.TMSI = TMSI;
        this.IMEI = IMEI;
    }

    /**
     *
     * @return
     */
    public String getIMEI() {
        return IMEI;
    }

    /**
     *
     * @return
     */
    public String getIMSI() {
        return IMSI;
    }

    /**
     *
     * @return
     */
    public String getTMSI() {
        return TMSI;
    }

    /**
     *
     * @return
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("IMSI", IMSI);
        json.put("TMSI", TMSI);
        json.put("IMEI", IMEI);
        return json;
    }
}
