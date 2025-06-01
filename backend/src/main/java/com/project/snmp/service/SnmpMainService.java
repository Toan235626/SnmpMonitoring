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


    public SnmpRecord getSnmpValue(String deviceIp, String community,String oid, int port, String version) throws Exception {
        SnmpRecord record = snmpGet.getAsRecord(deviceIp, community, oid, port, version);

        // Nếu value là lỗi SNMP → thử lại với .0
        String value = record.getValue();
        System.out.println("SNMP GET: " + record.getOid() + " - " + value);
        if (value == null || value.isEmpty() || value.equals("noSuchObject") || value.equals("Null")) {
            System.out.println("SNMP GET: " + record.getOid() + " - " + value + " → Fallback to .0");
            record = snmpGet.getAsRecord(deviceIp, community, oid + ".0", port, version);
            value = record.getValue();
        }

        return record;
    }

    public SnmpRecord getSnmpNextValue(String deviceIp, String community,String oid, int port, String version) throws Exception {
        SnmpRecord getNextResult = snmpGetNext.getNextAsRecord(deviceIp, community,oid, port, version);

        // Nếu value là lỗi SNMP → thử lại với .0
        String value = getNextResult.getValue();
        System.out.println("SNMP GETNEXT: " + getNextResult.getOid() + " - " + value);
        if (value == null || value.isEmpty() || value.equals("noSuchObject") || value.equals("Null")) {
            System.out.println("SNMP GETNEXT: " + getNextResult.getOid() + " - " + value + " → Fallback to .0");
            getNextResult = snmpGetNext.getNextAsRecord(deviceIp, community, oid + ".0", port, version);
            value = getNextResult.getValue();
        }

        return getNextResult;
    }
    public SnmpRecord[] getSnmpBulkValue(String deviceIp, String community,String oid, int port, String version) throws Exception {
        SnmpRecord[] getBulkResult = snmpGetBulk.getBulkAsRecords(deviceIp, community,oid, port, version);

        return getBulkResult;
    } 
    public SnmpRecord[] getSnmpWalkValue(String deviceIp, String community,String oid, int port, String version) throws Exception {
        SnmpRecord[] getWalkResult = snmpWalk.walkAsRecords(deviceIp, community,oid, port, version);

        return getWalkResult;
    }
}
