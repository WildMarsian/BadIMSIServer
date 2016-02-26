package org.imsi.badimsibox.badimsiserver;

/**
 * 
 * @author AlisterWan
 */
public class NetworkOperator {

    private final String mcc;
    private final String mnc;

    /**
     *
     * @param mnc : Mobile Network Code
     * @param mcc : Mobile Country Code
     */
    public NetworkOperator(String mnc, String mcc) {
        this.mnc = mnc;
        this.mcc = mcc;
    }

    /**
     * Determine what network own the specified Mobile Network Code. 
     * Only France is supported for now.
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
     *
     * @return
     */
    public String getMnc() {
        return mnc;
    }
    
    /**
     *
     * @return
     */
    public String getMcc() {
        return mcc;
    }

    @Override
    public String toString() {
        return getNetworkName() + ": " + this.mnc + " " + this.mcc;
    }
}
