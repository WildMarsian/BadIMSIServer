package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.json.JsonObject;

/**
 *
 * @author AlisterWan
 */
public class Sms {

    private final String date;
    private final String message;

    /**
     * Create a new SMS with a specified date and a custom message
     * @param date : The date used to know when the SMS was send
     * @param message : String containing the message of the SMS
     */
    public Sms(String date, String message) {
        this.date = date;
        this.message = message;
    }

    /**
     * Export a SMS class into JSON format for exchange purpose
     * @return : A Json object containing the data of the created SMS
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("date", date);
        json.put("message", message);
        return json;
    }

    /**
     * Getting the date used to create a SMS. It will be render as the 
     * sending date by the mobile phone which receive it
     * @return A string containing the creation date of the SMS
     */
    public String getDate() {
        return date;
    }

    /**
     * Getting the message contained in the created SMS
     * @return the message contained in the SMS
     */
    public String getMessage() {
        return message;
    }
}