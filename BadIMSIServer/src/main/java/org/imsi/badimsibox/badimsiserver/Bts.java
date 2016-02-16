package org.imsi.badimsibox.badimsiserver;

import java.util.List;

public class Bts {

	private final NetworkOperator operator;
	private final String lac;
	private final String ci;
	private final List<String> arfcn;

	public Bts(String mnc, String mcc, String lac, String ci, List<String> arfcn) {
		this.operator = new NetworkOperator(mnc, mcc);
		this.lac = lac;
		this.ci = ci;
		this.arfcn = arfcn;
	}
	
	public Bts(NetworkOperator operator, String lac, String ci, List<String> arfcn) {
		this.operator = operator;
		this.lac = lac;
		this.ci = ci;
		this.arfcn = arfcn;
	}
	
	public String getOperatorByMnc() {
		return operator.getNetworkName();
	}
	
	
	
	public NetworkOperator getOperator() {
		return operator;
	}

	public String getLac() {
		return lac;
	}

	public String getCi() {
		return ci;
	}

	public List<String> getArfcn() {
		return arfcn;
	}

	@Override
	public String toString() {
		return this.operator+" "+this.lac+" "+this.ci+" "+this.arfcn;
	}
}
