package org.imsi.badimsibox.badimsiserver;

public interface Observable {
	public void notifyObservers();
	public void attach(Observer observer);
}
