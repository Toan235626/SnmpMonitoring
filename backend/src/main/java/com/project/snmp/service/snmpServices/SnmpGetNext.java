package com.project.snmp.service.snmpServices;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;


public class SnmpGetNext {
    private String address;
    private String community;
    private String currentOID;

    public SnmpGetNext(String address, String currentOID ,String community) {
        this.address = address;
        this.community = community;
        this.currentOID = currentOID;
    }

    public String getNextAsString() throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(address + "/161"));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(currentOID)));
        pdu.setType(PDU.GETNEXT);

        Snmp snmp = new Snmp(transport);
        ResponseEvent response = snmp.getNext(pdu, target);

        if (response != null && response.getResponse() != null) {
            return response.getResponse().getVariableBindings().toString();
        } else {
            throw new RuntimeException("SNMP GETNEXT request timed out or failed.");
        }
    }
    public static void main(String[] args) {
        try {
            SnmpGetNext snmpGetNext = new SnmpGetNext("127.0.0.1", "1.3.6.1.2.1.1.1.0" ,"public");
            String result = snmpGetNext.getNextAsString();
            System.out.println("SNMP GETNext Result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
