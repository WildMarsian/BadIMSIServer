package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.json.JsonObject;

/**
 *
 * @author
 */
public class SMS {

    private final String date;
    private final String message;

    /**
     *
     * @param date
     * @param message
     */
    public SMS(String date, String message) {
        this.date = date;
        this.message = message;
    }

    /**
     *
     * @return
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("date", date);
        json.put("message", message);
        return json;
    }

    /**
     *
     * @return
     */
    public String getDate() {
        return date;
    }

    /**
     *
     * @return
     */
    public String getMessage() {
        return message;
    }
}
