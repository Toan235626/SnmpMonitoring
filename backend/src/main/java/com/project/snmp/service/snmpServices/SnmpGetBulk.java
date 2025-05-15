package com.project.snmp.service.snmpServices;

import com.project.snmp.model.SnmpRecord;
import com.project.snmp.utils.SnmpStringToJson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Component;

@Component
public class SnmpGetBulk {
    public SnmpRecord[] getBulkAsRecords(String deviceIp, String community, String oid, int nonRepeaters, int maxRepetitions) throws Exception {
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
        pdu.setType(PDU.GETBULK);
        pdu.setNonRepeaters(nonRepeaters);
        pdu.setMaxRepetitions(maxRepetitions);

        Snmp snmp = new Snmp(transport);
        ResponseEvent response = snmp.getBulk(pdu, target);

        if (response != null && response.getResponse() != null) {
            String vbString = response.getResponse().getVariableBindings().toString();
            if (vbString.startsWith("[") && vbString.endsWith("]")) {
                vbString = vbString.substring(1, vbString.length() - 1);
            }
            SnmpStringToJson snmpStringToJson = new SnmpStringToJson(vbString);
            JSONArray jsonArray = snmpStringToJson.toJson();
            
            SnmpRecord[] snmpRecords = new SnmpRecord[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                SnmpRecord snmpRecord = new SnmpRecord();
                snmpRecord.setDeviceIp(deviceIp);
                snmpRecord.setOid(jsonObject.getString("oid"));
                snmpRecord.setValue(jsonObject.getString("value"));
                snmpRecord.setCommunity(community);
                snmpRecords[i] = snmpRecord;
            }
            return snmpRecords;
        } else {
            throw new RuntimeException("SNMP GETBULK request timed out or failed.");
        }
    }

    // public static void main(String[] args) {
    //     SnmpGetBulk snmpGetBulk = new SnmpGetBulk();
    //     try {
    //         JSONArray result = snmpGetBulk.getBulkAsJson("127.0.0.1", "public", "1.3.6.1.2.1.1.1.0", 0, 10);
    //         for (Object jsonObject : result) {
    //             System.out.println(jsonObject.toString());
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
}
