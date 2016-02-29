package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Class used to store data of one BadIMSI Session. A session state represent
 * the position in the path to access final functionalities of the server
 * application.
 *
 * @author AisukoWasTaken
 */
public class Session {

    private final Vertx vertx;
    private final String password;

    private int state;
    private int timestamp;

    /**
     * Constructor of one session object
     *
     * @param password : the password used to connect into the session
     * @param vertx : the vertx server instance, used to access to EventBus
     */
    public Session(String password, Vertx vertx) {
        this.password = password;
        this.state = 0;
        this.timestamp = 0;
        this.vertx = vertx;
    }

    /**
     * Used to create a default session without passwords to instantiate the
     * server
     *
     * @param vertx : the vertx server instalce
     * @return the Session created without password
     */
    public static Session init(Vertx vertx) {
        Session init = new Session("", vertx);
        return init;
    }

    /**
     * Used to compare a given password with the session password. Validating
     * the password entered by one user trying to connect
     *
     * @param inputPassword : The password to check
     * @return : True if session and given password are equals else False
     */
    public boolean checkPassword(String inputPassword) {
        return inputPassword.equals(this.password);
    }

    /**
     * Get the current session state
     *
     * @return a int value to represent the session state
     */
    public int getSessionState() {
        return this.state;
    }

    /**
     * Used to change the session state
     */
    public void nextSessionState() {
        this.state++;
        JsonObject json = new JsonObject();
        json.put("state", Integer.toString(this.state));
        this.vertx.eventBus().publish("session.new", json.encode());
    }

    /**
     * Used to export the password of the current session
     *
     * @return
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Used to update the timestamp of the current session
     */
    public void updateTimestamp() {
        this.timestamp = (int) (System.currentTimeMillis() / 1000L);
    }

    /**
     * Used to export the timestamp of the current session
     *
     * @return
     */
    public int getTimestamp() {
        return this.timestamp;
    }
}
