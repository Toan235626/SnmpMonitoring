package com.project.snmp.service.snmpServices;

import com.project.snmp.utils.SnmpStringToJson;
import org.json.JSONObject;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Component;

@Component
public class SnmpGetBulk {
    public JSONObject getBulkAsJson(String address, String community, String oid, int nonRepeaters, int maxRepetitions) throws Exception {
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
        pdu.setType(PDU.GETBULK);
        pdu.setNonRepeaters(nonRepeaters);
        pdu.setMaxRepetitions(maxRepetitions);

        Snmp snmp = new Snmp(transport);
        ResponseEvent response = snmp.getBulk(pdu, target);

        if (response != null && response.getResponse() != null) {
            // Chuyển danh sách VariableBindings thành chuỗi rồi parse sang JSON
            SnmpStringToJson snmpStringToJson = new SnmpStringToJson(response.getResponse().getVariableBindings().toString());
            return snmpStringToJson.toJson();
        } else {
            throw new RuntimeException("SNMP GETBULK request timed out or failed.");
        }
    }
}
