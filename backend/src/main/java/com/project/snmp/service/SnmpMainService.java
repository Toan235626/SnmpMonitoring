package com.project.snmp.service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.project.snmp.model.SnmpRecord;
import com.project.snmp.service.snmpServices.*;

@Service
public class SnmpMainService {

    public SnmpRecord getSnmpValue(String ipAddress, String community,String oid) throws Exception {
        SnmpGet snmpGet = new SnmpGet();
        return snmpGet.getAsRecord(ipAddress, community,oid);
    }

    public SnmpRecord getSnmpNextValue(String ipAddress, String community,String oid) throws Exception {
        SnmpGetNext snmpGetNext = new SnmpGetNext();
        return snmpGetNext.getNextAsRecord(ipAddress, community,oid);
    }
    public SnmpRecord[] getSnmpBulkValue(String ipAddress, String community,String oid, int nonRepeaters, int maxRepetitions) throws Exception {
        SnmpGetBulk snmpGetBulk = new SnmpGetBulk();
        SnmpRecord[] getBulkResult = snmpGetBulk.getBulkAsRecords(ipAddress, community,oid, nonRepeaters, maxRepetitions);

        return getBulkResult;
    } 
    public SnmpRecord[] getSnmpWalkValue(String ipAddress, String community,String oid) throws Exception {
        SnmpWalk snmpWalk = new SnmpWalk();
        SnmpRecord[] getWalkResult = snmpWalk.walkAsRecords(ipAddress, community,oid);
        
        return getWalkResult;
    }
}
