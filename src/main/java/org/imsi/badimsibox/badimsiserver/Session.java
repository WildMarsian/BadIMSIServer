package org.imsi.badimsibox.badimsiserver;

import java.util.Date;

public class Session {
	private int state;
	private final String password;
	private int timestamp;

	public Session(String password) {
		this.password = password;
		this.state = 0;
		this.timestamp = 0;
	}

	public static Session init() {
		Session init = new Session("");
		return init;
	}

	public boolean checkPassword(String inputPassword) {
		return inputPassword.equals(this.password);
	}

	public int getSessionState() {
		return this.state;
	}

	public void nextSessionState() {
		this.state++;
		System.out.println(new Date() + ": Next sesssion state: " + this.state);
	}

	public String getPassword() {
		return this.password;
	}

	public void updateTimestamp() {
		System.out.println(new Date() + ": Updated timestamp");
		this.timestamp = (int) (System.currentTimeMillis() / 1000L);
	}

	public int getTimestamp() {
		return this.timestamp;
	}
}