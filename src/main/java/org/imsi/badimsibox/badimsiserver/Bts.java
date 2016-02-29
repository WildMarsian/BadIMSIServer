package org.imsi.badimsibox.badimsiserver;

import java.util.List;
import java.util.Objects;

/**
 * Class to represent a BTS station
 *
 * Used to store data to represent a BTS station identified during the sniffing
 * process. Those data are used by the Fake BTS to broadcast valid informations
 * to the Mobile Phone near it resulting of their connection.
 *
 * @author AlisterWan WarenUT
 */
public class Bts {

    private final NetworkOperator operator;
    private final String lac;
    private final String ci;
    private final List<String> arfcn;

    /**
     * Used to create an instance of one BTS storage data
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
     * Used to create an instance of one BTS storage data
     *
     * @param operator : The operator using the BTS identified
     * @param lac : Local Area Code
     * @param ci : Cellule ID
     * @param arfcn : List of Absolute Radio Frequency Channel Number
     */
    public Bts(NetworkOperator operator, String lac, String ci, List<String> arfcn) {
        this.operator = operator;
        this.lac = lac;
        this.ci = ci;
        this.arfcn = arfcn;
    }

    /**
     * Used to export the network name handled by the BTS
     *
     * @return a String to represent the name of the network handled
     */
    public String getOperatorByMnc() {
        return operator.getNetworkName();
    }

    /**
     * Used to export the name of the operator, owner of the BTS
     *
     * @return the Network Operator instance of this network operator BTS
     */
    public NetworkOperator getOperator() {
        return operator;
    }

    /**
     * Used to export the Local Area Code of this BTS
     *
     * @return a String containeing the LAC Code
     */
    public String getLac() {
        return lac;
    }

    /**
     * Used to export the Cellule ID of the BTS
     *
     * @return a String containing the Cellule ID of the BTS
     */
    public String getCi() {
        return ci;
    }

    /**
     * Used to export the ARFCN list of this BTS
     *
     * @return a List interface of String containing all ARFCN detected from
     * this BTS
     */
    public List<String> getArfcn() {
        return arfcn;
    }

    @Override
    public String toString() {
        return "Bts{" + "operator=" + operator + ", lac=" + lac + ", ci=" + ci + ", arfcn=" + arfcn + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.ci);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Bts other = (Bts) obj;
        if (!Objects.equals(this.ci, other.ci)) {
            return false;
        }
        return true;
    }
}
