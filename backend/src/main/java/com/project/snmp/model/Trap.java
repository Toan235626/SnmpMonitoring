package com.project.snmp.model;

import java.util.List;
import org.snmp4j.smi.VariableBinding;

public class Trap {
    private String pduString;
    private List<VariableBinding> variableBindings;

    public Trap(String pduString, List<VariableBinding> variableBindings) {
        this.pduString = pduString;
        this.variableBindings = variableBindings;
    }

    public String getPduString() {
        return pduString;
    }

    public List<VariableBinding> getVariableBindings() {
        return variableBindings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Trap received: ").append(pduString).append("\n");
        for (VariableBinding vb : variableBindings) {
            sb.append("OID: ").append(vb.getOid()).append(", Value: ").append(vb.getVariable()).append("\n");
        }
        return sb.toString();
    }
}
