package com.project.snmp.service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.project.snmp.model.SnmpRecord;
import com.project.snmp.service.snmpServices.*;

@Service
public class SnmpMainService {

    public SnmpRecord getSnmpValue(String deviceIp, String community,String oid, int port) throws Exception {
        SnmpGet snmpGet = new SnmpGet();
        return snmpGet.getAsRecord(deviceIp, community,oid, port);
    }

    public SnmpRecord getSnmpNextValue(String deviceIp, String community,String oid, int port) throws Exception {
        SnmpGetNext snmpGetNext = new SnmpGetNext();
        return snmpGetNext.getNextAsRecord(deviceIp, community,oid, port);
    }
    public SnmpRecord[] getSnmpBulkValue(String deviceIp, String community,String oid, int port) throws Exception {
        SnmpGetBulk snmpGetBulk = new SnmpGetBulk();
        SnmpRecord[] getBulkResult = snmpGetBulk.getBulkAsRecords(deviceIp, community,oid, port);

        return getBulkResult;
    } 
    public SnmpRecord[] getSnmpWalkValue(String deviceIp, String community,String oid, int port) throws Exception {
        SnmpWalk snmpWalk = new SnmpWalk();
        SnmpRecord[] getWalkResult = snmpWalk.walkAsRecords(deviceIp, community,oid, port);

        return getWalkResult;
    }
}
