package com.project.snmp.service.snmpServices;

import org.json.JSONArray;
import org.json.JSONObject;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;
import org.springframework.stereotype.Component;

import com.project.snmp.model.SnmpRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SnmpWalk {
    public SnmpRecord[] walkAsRecords(String address, String community, String rootOid) throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(address + "/161"));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        Snmp snmp = new Snmp(transport);

        // TreeUtils hỗ trợ thực hiện walk
        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.getSubtree(target, new OID(rootOid));

        ArrayList<SnmpRecord> resultList = new java.util.ArrayList<>();

        if (events == null || events.isEmpty()) {
            throw new RuntimeException("SNMP walk failed or no result returned.");
        }

        for (TreeEvent event : events) {
            if (event == null) continue;
            if (event.isError()) {
                throw new RuntimeException("Error during SNMP walk: " + event.getErrorMessage());
            }
            VariableBinding[] varBindings = event.getVariableBindings();
            if (varBindings == null || varBindings.length == 0) continue;

            for (VariableBinding vb : varBindings) {
                SnmpRecord snmpRecord = new SnmpRecord();
                snmpRecord.setDevice(address);
                snmpRecord.setOid(vb.getOid().toString());
                if (vb.getVariable() == null) {
                    snmpRecord.setValue("null");
                } else if (vb.getVariable().toString().isEmpty()) {
                    snmpRecord.setValue("null");
                } else {
                    snmpRecord.setValue(vb.getVariable().toString());
                }
                resultList.add(snmpRecord);
            }
        }

        snmp.close();
        snmp.close();
        transport.close();

        return resultList.toArray(new SnmpRecord[0]);
    }
    // public static void main(String[] args) {
    //     SnmpWalk snmpWalk = new SnmpWalk();
    //     try {
    //         JSONArray result = snmpWalk.walkAsJsonArray("127.0.0.1", "public", "1.3.6.1.2.1.1");
    //         for (int i = 0; i < result.length(); i++) {
    //             System.out.println(result.getJSONObject(i).toString());
    //         }
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
}
