package org.imsi.badimsibox.badimsiserver;

import java.util.List;

/**
 * Class to represent a BTS station
 * @author AlisterWan WarenUT
 */
public class Bts {

    private final NetworkOperator operator;
    private final String lac;
    private final String ci;
    private final List<String> arfcn;

    /**
     * 
     * @param mnc : Mobile Network Code
     * @param mcc : Mobile Country Code
     * @param lac : Local Area Code
     * @param ci : Cellule ID
     * @param arfcn : List of Absolute Radio Frequency Channel Number
     */
    public Bts(String mnc, String mcc, String lac, String ci, List<String> arfcn) {
        this.operator = new NetworkOperator(mnc, mcc);
        this.lac = lac;
        this.ci = ci;
        this.arfcn = arfcn;
    }

    /**
     *
     * @param operator : The operator using the BTS identified
     * @param lac : Local Area Code
     * @param ci : Cellule ID
     * @param arfcn :  List of Absolute Radio Frequency Channel Number
     */
    public Bts(NetworkOperator operator, String lac, String ci, List<String> arfcn) {
        this.operator = operator;
        this.lac = lac;
        this.ci = ci;
        this.arfcn = arfcn;
    }

    /**
     *
     * @return
     */
    public String getOperatorByMnc() {
        return operator.getNetworkName();
    }

    /**
     *
     * @return
     */
    public NetworkOperator getOperator() {
        return operator;
    }

    /**
     *
     * @return
     */
    public String getLac() {
        return lac;
    }

    /**
     *
     * @return
     */
    public String getCi() {
        return ci;
    }

    /**
     *
     * @return
     */
    public List<String> getArfcn() {
        return arfcn;
    }

    @Override
    public String toString() {
        return this.operator + " " + this.lac + " " + this.ci + " " + this.arfcn;
    }
}
