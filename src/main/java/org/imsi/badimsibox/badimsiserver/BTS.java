package org.imsi.badimsibox.badimsiserver;

import java.util.List;

/**
 *
 * @author
 */
public class BTS {

    private final NetworkOperator operator;
    private final String lac;
    private final String ci;
    private final List<String> arfcn;

    /**
     *
     * @param mnc
     * @param mcc
     * @param lac
     * @param ci
     * @param arfcn
     */
    public BTS(String mnc, String mcc, String lac, String ci, List<String> arfcn) {
        this.operator = new NetworkOperator(mnc, mcc);
        this.lac = lac;
        this.ci = ci;
        this.arfcn = arfcn;
    }

    /**
     *
     * @param operator
     * @param lac
     * @param ci
     * @param arfcn
     */
    public BTS(NetworkOperator operator, String lac, String ci, List<String> arfcn) {
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
