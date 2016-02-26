package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.json.JsonObject;

/**
 *
 * @author AlisterWan WarenUT
 */
public class MobileTarget {

    private final String imsi;
    private final String tmsi;
    private final String imei;

    /**
     *
     * @param imsi
     * @param tmsi
     * @param imei
     */
    public MobileTarget(String imsi, String tmsi, String imei) {
        this.imsi = imsi;
        this.tmsi = tmsi;
        this.imei = imei;
    }

    /**
     *
     * @return
     */
    public String getIMEI() {
        return imei;
    }

    /**
     *
     * @return
     */
    public String getIMSI() {
        return imsi;
    }

    /**
     *
     * @return
     */
    public String getTMSI() {
        return tmsi;
    }

    /**
     *
     * @return
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("IMSI", imsi);
        json.put("TMSI", tmsi);
        json.put("IMEI", imei);
        return json;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("imsi : ");
        str.append(imsi);
        str.append(", tmsi :");
        str.append(tmsi);
        str.append(", imei : ");
        str.append(imei);
        return str.toString();
    }   
}
