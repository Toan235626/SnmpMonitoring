package com.project.snmp.service.snmpServices;

import org.json.JSONArray;
import org.json.JSONObject;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class SnmpWalk {
    public JSONArray walkAsJsonArray(String address, String community, String rootOid) throws IOException {
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

        JSONArray resultArray = new JSONArray();

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
                JSONObject obj = new JSONObject();
                obj.put("oid", vb.getOid().toString());
                obj.put("value", vb.getVariable().toString());
                resultArray.put(obj);
            }
        }

        snmp.close();
        transport.close();

        return resultArray;
    }
}
