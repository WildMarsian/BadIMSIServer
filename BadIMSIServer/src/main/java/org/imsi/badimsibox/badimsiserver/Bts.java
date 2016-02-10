package org.imsi.badimsibox.badimsiserver;

import java.util.ArrayList;

public class Bts {

	private final NetworkOperator operator;
	private final String lac;
	private final String ci;
	private final ArrayList<String> arfcn;

	public Bts(String mnc, String mcc, String lac, String ci, ArrayList<String> arfcn) {
		this.operator = new NetworkOperator(mnc, mcc);
		this.lac = lac;
		this.ci = ci;
		this.arfcn = arfcn;
	}
	
	public Bts(NetworkOperator operator, String lac, String ci, ArrayList<String> arfcn) {
		this.operator = operator;
		this.lac = lac;
		this.ci = ci;
		this.arfcn = arfcn;
	}
	
	@Override
	public String toString() {
		return this.operator+" "+this.lac+" "+this.ci+" "+this.arfcn;
	}
	
}
