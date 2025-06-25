package com.project.snmp.model;

import org.snmp4j.smi.VariableBinding;

import java.util.HashMap;
import java.util.List;

public class Trap {
    private String rawPdu;
    private List<HashMap<String, String>> variables;
    private String snmpVersion;
    private String senderIp;

    public Trap(String rawPdu, List<HashMap<String, String>> variables, String snmpVersion, String senderIp) {
        this.rawPdu = rawPdu;
        this.variables = variables;
        this.snmpVersion = snmpVersion;
        this.senderIp = senderIp;
    }

    // Getter và Setter

    public String getRawPdu() {
        return rawPdu;
    }

    public void setRawPdu(String rawPdu) {
        this.rawPdu = rawPdu;
    }

    public List<HashMap<String, String>> getVariables() {
        return variables;
    }

    public void setVariables(List<HashMap<String, String>> variables) {
        this.variables = variables;
    }

    public String getSnmpVersion() {
        return snmpVersion;
    }

    public void setSnmpVersion(String snmpVersion) {
        this.snmpVersion = snmpVersion;
    }

    @Override
    public String toString() {
        return "Trap{" +
                "snmpVersion='" + snmpVersion + '\'' +
                ", rawPdu='" + rawPdu + '\'' +
                ", variables=" + variables +
                '}';
    }

    public String getSenderIp() {
        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }
}
