package org.imsi.badimsibox.badimsiserver;

import java.util.ArrayList;

public class SmsManager implements Observable {

	private final ArrayList<Observer> observers = new ArrayList<>();
	private final ArrayList<Sms> smsList = new ArrayList<>();

	@Override
	public void notifyObservers() {
		for (Observer observer : observers) {
			observer.update();
		}
	}

	@Override
	public void attach(Observer observer) {
		observers.add(observer);
	}
}
