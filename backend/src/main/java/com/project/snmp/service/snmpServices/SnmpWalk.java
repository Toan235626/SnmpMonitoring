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


public class SnmpWalk {

    public void performSnmpWalk(String ipAddress, String community, String oid) {
        try {
            // Create TransportMapping
            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
            transport.listen();

            // Create Target
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(new UdpAddress(ipAddress + "/161"));
            target.setRetries(2);
            target.setTimeout(1500);
            target.setVersion(SnmpConstants.version2c);

            // Create PDU
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GETNEXT);

            // Create Snmp instance
            Snmp snmp = new Snmp(transport);

            boolean finished = false;
            while (!finished) {
                ResponseEvent responseEvent = snmp.send(pdu, target);
                PDU responsePDU = responseEvent.getResponse();

                if (responsePDU == null) {
                    System.out.println("Response is null. Exiting...");
                    finished = true;
                } else {
                    VariableBinding vb = responsePDU.get(0);
                    if (vb.getOid() == null || !vb.getOid().startsWith(new OID(oid))) {
                        finished = true;
                    } else {
                        System.out.println(vb.getOid() + " = " + vb.getVariable());
                        pdu.setRequestID(new Integer32(0));
                        pdu.set(0, new VariableBinding(vb.getOid()));
                    }
                }
            }

            snmp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}