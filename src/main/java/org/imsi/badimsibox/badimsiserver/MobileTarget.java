package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.json.JsonObject;
import java.util.Objects;

/**
 * Class used to represent a Mobile identified on the network in the sniffing
 * process. All those informations are used to target it especialy the IMSI
 * field.
 *
 * @author AlisterWan WarenUT
 */
public class MobileTarget {

    private final String imsi;
    private final String tmsi;
    private final String imei;
    private final String auth;
    private final String created;
    private final String accessed;
    private final String tmsiAssigned;

    /**
     * Constructor of the class
     *
     * @param imsi : The unique identifier of a mobile phone SIM user
     * @param tmsi : TODO
     * @param imei : TODO
     * @param auth : TODO
     * @param created : Date of the connection of the Mobile Phone to the fake
     * BTS
     * @param accessed : TODO
     * @param tmsiAssigned : TODO
     */
    public MobileTarget(String imsi, String tmsi, String imei, String auth, String created, String accessed, String tmsiAssigned) {
        this.imsi = imsi;
        this.tmsi = tmsi;
        this.imei = imei;
        this.auth = auth;
        this.created = created;
        this.accessed = accessed;
        this.tmsiAssigned = tmsiAssigned;
    }

    /**
     * Used to create an instance of one Mobile Phone connected to the fake BTS
     *
     * @param values a String array with 7 positions containing data in the
     * correct order : IMSI, TMSI, IMEI, AUTH, CREATED, ACCESSED, TMSI ASSIGNED
     */
    public MobileTarget(String[] values) {
        this(values[0], values[1], values[2], values[3], values[4], values[5], values[6]);
    }

    /**
     * TODO
     *
     * @return
     */
    public String getImsi() {
        return imsi;
    }

    /**
     * TODO
     *
     * @return
     */
    public String getTmsi() {
        return tmsi;
    }

    /**
     * Used to export the IMEI number of the Mobile Phone.
     *
     * @return a String containing the IMEI number of the Mobile Phone
     */
    public String getImei() {
        return imei;
    }

    /**
     * TODO
     *
     * @return
     */
    public String getAuth() {
        return auth;
    }

    /**
     * Used to export the connection date of the Mobule Phone to the Fake BTS
     *
     * @return a String containing the date of the connection of the Mobile
     * Phone to the Fake BTS TODO display the date pattern
     */
    public String getCreated() {
        return created;
    }

    /**
     * TODO
     *
     * @return
     */
    public String getAccessed() {
        return accessed;
    }

    /**
     * TODO
     *
     * @return
     */
    public String getTmsiAssigned() {
        return tmsiAssigned;
    }

    /**
     * Used to export in JSON all data stored by the instance of this class
     *
     * @return a JSON object containing all stored data
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("IMSI", imsi);
        json.put("TMSI", tmsi);
        json.put("IMEI", imei);
        json.put("AUTH", auth);
        json.put("CREATED", created);
        json.put("ACCESSED", accessed);
        json.put("ASSIGNED", tmsiAssigned);
        return json;
    }

    @Override
    public String toString() {
        return "MobileTarget{" + "imsi=" + imsi + ", tmsi=" + tmsi + ", imei=" + imei + ", auth=" + auth + ", created=" + created + ", accessed=" + accessed + ", timsiAssigned=" + tmsiAssigned + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.imsi);
        hash = 61 * hash + Objects.hashCode(this.tmsi);
        hash = 61 * hash + Objects.hashCode(this.imei);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MobileTarget other = (MobileTarget) obj;
        if (!Objects.equals(this.imsi, other.imsi)) {
            return false;
        }
        if (!Objects.equals(this.tmsi, other.tmsi)) {
            return false;
        }
        if (!Objects.equals(this.imei, other.imei)) {
            return false;
        }
        return true;
    }
}
