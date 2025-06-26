package com.project.snmp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.project.snmp.model.SnmpRecord;
import com.project.snmp.utils.TreeMerger;
import com.project.snmp.utils.VendorResolver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class MibTreeService {
    @Autowired
    private SnmpMainService snmpMainService;

    public JsonNode buildMergedTreeV12(String ip, String community, int port, String version) throws Exception {
        String sysObjectID = snmpMainService.getSnmpValue(ip, community, "1.3.6.1.2.1.1.2.0", port, version).getValue();
        String sysDescr = snmpMainService.getSnmpValue(ip, community, "1.3.6.1.2.1.1.1.0", port, version).getValue();

        String vendor = VendorResolver.resolve(sysObjectID, sysDescr);

        // System.out.println("Vendor: " + vendor);

        SnmpRecord[] snmpRecords = snmpMainService.getSnmpWalkValue(ip, community, "1.3.6.1", port, version);
        Map<String, String> oidValues = new java.util.HashMap<>();
        for (SnmpRecord record : snmpRecords) {
            oidValues.put(record.getOid(), record.getValue());
        }

        ArrayNode standardList = loadStandardTree();
        JsonNode vendorData = loadVendorMibTree(vendor);

        JsonNode mergedTree = TreeMerger.mergeTwoTrees(standardList, vendorData);
        TreeMerger.mergeValues(mergedTree, oidValues);
        return TreeMerger.filterByValue(mergedTree);

    }

    public JsonNode buildMergedTreeV3(String ip, String community, int port, String username, String authPass,
            String privPass,
            String authProtocol, String privProtocol, int securityLevel) throws Exception {
        String securityLevelStr = String.valueOf(securityLevel);
        String sysObjectID = snmpMainService.getSnmpValue(ip, community, "1.3.6.1.2.1.1.2.0", port, "3", username,
                authPass, privPass, authProtocol, privProtocol, securityLevelStr).getValue();
        String sysDescr = snmpMainService.getSnmpValue(ip, community, "1.3.6.1.2.1.1.1.0", port, "3", username,
                authPass, privPass, authProtocol, privProtocol, securityLevelStr).getValue();

        String vendor = VendorResolver.resolve(sysObjectID, sysDescr);

        // System.out.println("Vendor: " + vendor);

        SnmpRecord[] snmpRecords = snmpMainService.getSnmpWalkValue(ip, community, "1.3.6.1", port, "3", username,
                authPass, privPass, authProtocol, privProtocol, securityLevelStr);
        Map<String, String> oidValues = new java.util.HashMap<>();
        for (SnmpRecord record : snmpRecords) {
            oidValues.put(record.getOid(), record.getValue());
        }

        ArrayNode standardList = loadStandardTree();
        JsonNode vendorData = loadVendorMibTree(vendor);

        JsonNode mergedTree = TreeMerger.mergeTwoTrees(standardList, vendorData);
        TreeMerger.mergeValues(mergedTree, oidValues);
        return TreeMerger.filterByValue(mergedTree);

    }

    private ArrayNode loadStandardTree() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("mibs/standard/standard_oid.json");
        if (is == null) {
            throw new FileNotFoundException("Standard MIB tree not found.");
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(is);

        if (!node.isArray()) {
            throw new IOException("Expected standard_oid_sorted.json to be a JSON array.");
        }

        return (ArrayNode) node;
    }

    private JsonNode loadVendorMibTree(String vendor) throws IOException {
        String path = String.format("mibs/vendor/%s_tree.json", vendor);
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new FileNotFoundException("MIB file not found for vendor: " + vendor);
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode data = mapper.readTree(is);
        if (data.isArray()) {
            return TreeMerger.wrapArrayUnderEnterprise((ArrayNode) data);
        } else {
            return TreeMerger.wrapUnderEnterprise(data);
        }
    }

}
