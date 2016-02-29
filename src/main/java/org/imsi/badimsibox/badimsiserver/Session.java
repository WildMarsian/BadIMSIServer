package org.imsi.badimsibox.badimsiserver;

import java.util.Date;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Class used to store data of one BadIMSI Session
 * @author AisukoWasTaken
 */
public class Session {

    private final Vertx vertx;
    private final String password;

    private int state;
    private int timestamp;

    /**
     * Constructor of one session object
     * @param password
     * @param vertx
     */
    public Session(String password, Vertx vertx) {
        this.password = password;
        this.state = 0;
        this.timestamp = 0;
        this.vertx = vertx;
    }

    /**
     * 
     * @param vertx
     * @return
     */
    public static Session init(Vertx vertx) {
        Session init = new Session("", vertx);
        return init;
    }

    /**
     *
     * @param inputPassword
     * @return
     */
    public boolean checkPassword(String inputPassword) {
        return inputPassword.equals(this.password);
    }

    /**
     * Get the current session state
     * @return An int of the state of the session
     */
    public int getSessionState() {
        return this.state;
    }

    /**
     *
     */
    public void nextSessionState() {
        this.state++;
        System.out.println(new Date() + ": Next sesssion state: " + this.state);
        JsonObject json = new JsonObject();
        json.put("state", new String(Integer.toString(this.state)));
        System.out.println("Data sent to observer: " + json.encode());
        this.vertx.eventBus().publish("session.new", json.encode());
    }

    /**
     * 
     * @return 
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * 
     */
    public void updateTimestamp() {
        System.out.println(new Date() + ": Updated timestamp");
        this.timestamp = (int) (System.currentTimeMillis() / 1000L);
    }

    /**
     * 
     * @return 
     */
    public int getTimestamp() {
        return this.timestamp;
    }
}