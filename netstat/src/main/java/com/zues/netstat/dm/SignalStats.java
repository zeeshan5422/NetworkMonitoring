package com.zues.netstat.dm;

import java.io.Serializable;

public class SignalStats implements Serializable {

    private final int cType;
    private final int cSubType;
    private final String cTypeIdentifier;
    private final String cSubTypeIdentifier;


    public SignalStats(int cType, int cSubType, String cTypeIdentifier, String cSubTypeIdentifier) {
        this.cType = cType;
        this.cSubType = cSubType;
        this.cTypeIdentifier = cTypeIdentifier;
        this.cSubTypeIdentifier = cSubTypeIdentifier;
    }

    public int getcType() {
        return cType;
    }

    public int getcSubType() {
        return cSubType;
    }

    public String getcTypeIdentifier() {
        return cTypeIdentifier;
    }

    public String getcSubTypeIdentifier() {
        return cSubTypeIdentifier;
    }

    @Override
    public String toString() {
        return "SignalStats{" +
                "cType=" + cType +
                ", cSubType=" + cSubType +
                ", cTypeIdentifier='" + cTypeIdentifier + '\'' +
                ", cSubTypeIdentifier='" + cSubTypeIdentifier + '\'' +
                '}';
    }
}
