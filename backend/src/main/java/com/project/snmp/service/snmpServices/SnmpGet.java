package com.project.snmp.service.snmpServices;

import com.project.snmp.model.SnmpRecord;
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

    public SnmpRecord getAsRecord(String deviceIp, String community, String oid, int port, String version) throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(deviceIp + "/" + port));
        target.setRetries(2);
        target.setTimeout(1500);
        if (version.equals("1")) {
            target.setVersion(SnmpConstants.version1);
        } else if (version.equals("2c")) {
            target.setVersion(SnmpConstants.version2c);
        } else if (version.equals("3")) {
            target.setVersion(SnmpConstants.version3);
        } else {
            throw new IllegalArgumentException("Unsupported SNMP version: " + version);
        }

        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);

        Snmp snmp = new Snmp(transport);
        ResponseEvent response = snmp.get(pdu, target);

        if (response != null && response.getResponse() != null) {
            String vbString = response.getResponse().getVariableBindings().toString();
            if (vbString.startsWith("[") && vbString.endsWith("]")) {
                vbString = vbString.substring(1, vbString.length() - 1);
            }
            SnmpStringToJson snmpStringToJson = new SnmpStringToJson(vbString);

            SnmpRecord snmpRecord = new SnmpRecord();
            snmpRecord.setDeviceIp(deviceIp);
            snmpRecord.setOid(oid);
            snmpRecord.setCommunity(community);
            snmpRecord.setValue(snmpStringToJson.toJson().getJSONObject(0).getString("value"));
            return snmpRecord;
        } else {
            throw new RuntimeException("SNMP GET request timed out or failed.");
        }
    }
    // public static void main(String[] args) {
    //     SnmpGet snmpGet = new SnmpGet();
    //     try {
    //         JSONObject result = snmpGet.getAsJson("127.0.0.1", "public", "1.3.6.1.2.1.1.1.0");
    //         System.out.println(result.toString());
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
}