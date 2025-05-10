package com.project.snmp.service.snmpServices;

import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SnmpGetBulk {

    private Snmp snmp;
    private Target target;

    public SnmpGetBulk(Snmp snmp, Target target) {
        this.snmp = snmp;
        this.target = target;
    }

    public List<String> getBulk(OID oid, int nonRepeaters, int maxRepetitions) throws IOException {
        List<String> results = new ArrayList<>();
        PDU pdu = new PDU();
        pdu.setType(PDU.GETBULK);
        pdu.setNonRepeaters(nonRepeaters);
        pdu.setMaxRepetitions(maxRepetitions);
        pdu.add(oid);

        ResponseEvent responseEvent = snmp.send(pdu, target);
        if (responseEvent != null && responseEvent.getResponse() != null) {
            responseEvent.getResponse().getVariableBindings().forEach(vb -> results.add(vb.toValueString()));
        }

        return results;
    }
}