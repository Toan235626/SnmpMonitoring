package com.project.snmp.service.snmpServices;

import com.project.snmp.model.SnmpRecord;
import com.project.snmp.utils.SnmpStringToJson;
import org.json.JSONObject;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Component;

@Component
public class SnmpGetNext {
    public SnmpRecord getNextAsRecord(String deviceIp, String community, String oid) throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(deviceIp + "/161"));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GETNEXT);

        Snmp snmp = new Snmp(transport);
        ResponseEvent response = snmp.getNext(pdu, target);

        if (response != null && response.getResponse() != null) {
            String vbString = response.getResponse().getVariableBindings().toString();
            if (vbString.startsWith("[") && vbString.endsWith("]")) {
                vbString = vbString.substring(1, vbString.length() - 1);
            }
            SnmpStringToJson snmpStringToJson = new SnmpStringToJson(vbString);
            JSONObject jsonObject = snmpStringToJson.toJson().getJSONObject(0);
            SnmpRecord snmpRecord = new SnmpRecord();
            snmpRecord.setDeviceIp(deviceIp);
            snmpRecord.setOid(jsonObject.getString("oid"));
            snmpRecord.setValue(jsonObject.getString("value"));
            snmpRecord.setCommunity(community);
            return snmpRecord;
        } else {
            throw new RuntimeException("SNMP GETNEXT request timed out or failed.");
        }
    }

    // public static void main(String[] args) {
    //     SnmpGetNext snmpGetNext = new SnmpGetNext();
    //     try {
    //         JSONObject result = snmpGetNext.getNextAsJson("127.0.0.1", "public", "1.3.6.1.2.1.1.1.0");
    //         System.out.println(result.toString());
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
}
