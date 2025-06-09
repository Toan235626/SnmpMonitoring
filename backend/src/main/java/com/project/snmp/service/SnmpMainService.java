package com.project.snmp.service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.snmp.model.SnmpRecord;
import com.project.snmp.service.snmpServices.*;

@Service
public class SnmpMainService {

    @Autowired
    private SnmpGet snmpGet;
    @Autowired
    private SnmpGetNext snmpGetNext;
    @Autowired
    private SnmpGetBulk snmpGetBulk;
    @Autowired
    private SnmpWalk snmpWalk;

    public SnmpRecord getSnmpValue(String deviceIp,String community, String oid,int port,
                                   String version, String... v3params) throws Exception {
        SnmpRecord record;

        if ("3".equals(version)) {
            String username = v3params[0];
            String authPass = v3params[1];
            String privPass = v3params[2];
            String authProtocol = v3params[3];
            String privProtocol = v3params[4];
            int securityLevel = Integer.parseInt(v3params[5]);
            // System.out.println("Hello SNMP GET: " + deviceIp + " " + username + " " + authPass + " " + privPass + " " + authProtocol + " " + privProtocol + " " + securityLevel);
            record = snmpGet.getAsRecordv3(deviceIp, username, authPass, privPass,
                    authProtocol, privProtocol, securityLevel, oid, port);
        } else {
            record = snmpGet.getAsRecordv12(deviceIp, community, oid, port, version);
        }

        String value = record.getValue();
        System.out.println("SNMP GET: " + record.getOid() + " - " + value);
        if (value == null || value.isEmpty() || value.equals("noSuchObject") || value.equals("Null") || value.equals("noSuchInstance")) {
            System.out.println("SNMP GET: " + record.getOid() + " - " + value + " → Fallback to .0");
            if ("3".equals(version)) {
                String username = v3params[0];
                String authPass = v3params[1];
                String privPass = v3params[2];
                String authProtocol = v3params[3];
                String privProtocol = v3params[4];
                int securityLevel = Integer.parseInt(v3params[5]);
                record = snmpGet.getAsRecordv3(deviceIp, username, authPass, privPass,
                        authProtocol, privProtocol, securityLevel, oid + ".0", port);
            } else {
                record = snmpGet.getAsRecordv12(deviceIp, community, oid + ".0", port, version);
            }
        }
        return record;
    }

    public SnmpRecord getSnmpNextValue(String deviceIp, String community, String oid, int port,
                                       String version, String... v3params) throws Exception {
        SnmpRecord getNextResult;

        if ("3".equals(version)) {
            String username = v3params[0];
            String authPass = v3params[1];
            String privPass = v3params[2];
            String authProtocol = v3params[3];
            String privProtocol = v3params[4];
            int securityLevel = Integer.parseInt(v3params[5]);
            getNextResult = snmpGetNext.getNextv3(deviceIp, username, authPass, privPass,
                    authProtocol, privProtocol, securityLevel, oid, port);
        } else {
            getNextResult = snmpGetNext.getNextv12(deviceIp, community, oid, port, version);
        }

        String value = getNextResult.getValue();
        System.out.println("SNMP GETNEXT: " + getNextResult.getOid() + " - " + value);
        if (value == null || value.isEmpty() || value.equals("noSuchObject") || value.equals("Null")) {
            System.out.println("SNMP GETNEXT: " + getNextResult.getOid() + " - " + value + " → Fallback to .0");
            if ("3".equals(version)) {
                String username = v3params[0];
                String authPass = v3params[1];
                String privPass = v3params[2];
                String authProtocol = v3params[3];
                String privProtocol = v3params[4];
                int securityLevel = Integer.parseInt(v3params[5]);
                getNextResult = snmpGetNext.getNextv3(deviceIp, username, authPass, privPass,
                        authProtocol, privProtocol, securityLevel, oid + ".0", port);
            } else {
                getNextResult = snmpGetNext.getNextv12(deviceIp, community, oid + ".0", port, version);
            }
        }
        return getNextResult;
    }

    public SnmpRecord[] getSnmpBulkValue(String deviceIp, String community, String oid, int port,
                                        String version, String... v3params) throws Exception {
        if ("3".equals(version)) {
            String username = v3params[0];
            String authPass = v3params[1];
            String privPass = v3params[2];
            String authProtocol = v3params[3];
            String privProtocol = v3params[4];
            int securityLevel = Integer.parseInt(v3params[5]);
            return snmpGetBulk.getBulkv3(deviceIp, username, authPass, privPass,
                    authProtocol, privProtocol, securityLevel, oid, port);
        } else {
            return snmpGetBulk.getBulkv2(deviceIp, community, oid, port);
        }
    }

    public SnmpRecord[] getSnmpWalkValue(String deviceIp, String community, String rootOid, int port,
                                         String version, String... v3params) throws Exception {
        switch (version) {
            case "1":
                return snmpWalk.walkV1Records(deviceIp, community, rootOid, port);
            case "2c":
                return snmpWalk.walkV2Records(deviceIp, community, rootOid, port);
            case "3":
                String username = v3params[0];
                String authPass = v3params[1];
                String privPass = v3params[2];
                String authProtocol = v3params[3];
                String privProtocol = v3params[4];
                int securityLevel = Integer.parseInt(v3params[5]);
                return snmpWalk.walkV3Records(deviceIp, username, authPass, privPass,
                        authProtocol, privProtocol, securityLevel, rootOid, port);
            default:
                throw new IllegalArgumentException("Unsupported SNMP version: " + version);
        }
    }
}
