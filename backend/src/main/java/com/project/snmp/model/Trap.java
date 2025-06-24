package com.project.snmp.model;

import org.snmp4j.smi.VariableBinding;

import java.util.HashMap;
import java.util.List;

public class Trap {
    private String rawPdu;
    private List<HashMap<String, String>> variables;
    private String snmpVersion;

    public Trap(String rawPdu, List<HashMap<String, String>> variables, String snmpVersion) {
        this.rawPdu = rawPdu;
        this.variables = variables;
        this.snmpVersion = snmpVersion;
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
}
