package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.json.JsonObject;
import java.util.Objects;

/**
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
    private final String timsiAssigned;

    public MobileTarget(String imsi, String tmsi, String imei, String auth, String created, String accessed, String timsiAssigned) {
        this.imsi = imsi;
        this.tmsi = tmsi;
        this.imei = imei;
        this.auth = auth;
        this.created = created;
        this.accessed = accessed;
        this.timsiAssigned = timsiAssigned;
    }

    public MobileTarget(String[] values) {
        this(values[0], values[1], values[2], values[3], values[4], values[5], values[6]);
    }

    public String getImsi() {
        return imsi;
    }

    public String getTmsi() {
        return tmsi;
    }

    public String getImei() {
        return imei;
    }

    public String getAuth() {
        return auth;
    }

    public String getCreated() {
        return created;
    }

    public String getAccessed() {
        return accessed;
    }

    public String getTimsiAssigned() {
        return timsiAssigned;
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
        json.put("AUTH", auth);
        json.put("CREATED", created);
        json.put("ACCESSED", accessed);
        json.put("ASSIGNED", timsiAssigned);
        return json;
    }

    @Override
    public String toString() {
        return "MobileTarget{" + "imsi=" + imsi + ", tmsi=" + tmsi + ", imei=" + imei + ", auth=" + auth + ", created=" + created + ", accessed=" + accessed + ", timsiAssigned=" + timsiAssigned + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.imsi);
        hash = 23 * hash + Objects.hashCode(this.tmsi);
        hash = 23 * hash + Objects.hashCode(this.imei);
        hash = 23 * hash + Objects.hashCode(this.auth);
        hash = 23 * hash + Objects.hashCode(this.created);
        hash = 23 * hash + Objects.hashCode(this.accessed);
        hash = 23 * hash + Objects.hashCode(this.timsiAssigned);
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
        if (!Objects.equals(this.auth, other.auth)) {
            return false;
        }
        if (!Objects.equals(this.created, other.created)) {
            return false;
        }
        if (!Objects.equals(this.accessed, other.accessed)) {
            return false;
        }
        if (!Objects.equals(this.timsiAssigned, other.timsiAssigned)) {
            return false;
        }
        return true;
    }
}
