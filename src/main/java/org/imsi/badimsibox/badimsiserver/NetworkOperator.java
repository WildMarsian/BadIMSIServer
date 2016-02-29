package org.imsi.badimsibox.badimsiserver;

import java.util.Objects;

/**
 * Class to store the data used to represent a single network operator
 *
 * @author AlisterWan
 */
public class NetworkOperator {

    private final String mcc;
    private final String mnc;

    /**
     * Used to create an instance of a network operator
     *
     * @param mnc : the Mobile Network Code
     * @param mcc : the Mobile Country Code
     */
    public NetworkOperator(String mnc, String mcc) {
        this.mnc = mnc;
        this.mcc = mcc;
    }

    /**
     * Determine what network own the specified Mobile Network Code. Only France
     * is supported for now.
     *
     * @return the network name of the MNC
     */
    public String getNetworkName() {
        switch (mnc) {
            case "01":
                return "Orange";
            case "02":
                return "Orange";
            case "09":
                return "SFR";
            case "10":
                return "SFR";
            case "11":
                return "SFR";
            case "15":
                return "Free mobile";
            case "16":
                return "Free mobile";
            case "20":
                return "Bouygues Telecom";
            case "21":
                return "Bouygues Telecom";
            default:
                throw new IllegalStateException("Does not contain the right network code!!");
        }
    }

    /**
     * Used to export the MNC of the network operator
     *
     * @return s String to represent the MNC of the network operator
     */
    public String getMnc() {
        return mnc;
    }

    /**
     * Used to export the MCC of the network operator
     *
     * @return a String to represent the MCC of the network operator
     */
    public String getMcc() {
        return mcc;
    }

    @Override
    public String toString() {
        return "NetworkOperator{" + "mcc=" + mcc + ", mnc=" + mnc + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.mcc);
        hash = 97 * hash + Objects.hashCode(this.mnc);
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
        final NetworkOperator other = (NetworkOperator) obj;
        if (!Objects.equals(this.mcc, other.mcc)) {
            return false;
        }
        if (!Objects.equals(this.mnc, other.mnc)) {
            return false;
        }
        return true;
    }
}
