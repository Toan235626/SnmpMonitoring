package com.project.snmp.service.snmpServices;

import com.project.snmp.service.SnmpMainService;
import com.project.snmp.utils.SnmpStringToJson;

import org.json.JSONObject;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Component;


@Component
public class SnmpGet{

    public JSONObject getAsJson(String address, String community, String oid) throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(address + "/161"));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);

        Snmp snmp = new Snmp(transport);
        ResponseEvent response = snmp.get(pdu, target);

        if (response != null && response.getResponse() != null) {
            SnmpStringToJson snmpStringToJson = new SnmpStringToJson(response.getResponse().getVariableBindings().toString());
            return snmpStringToJson.toJson();
        } else {
            throw new RuntimeException("SNMP GET request timed out or failed.");
        }
    }
}