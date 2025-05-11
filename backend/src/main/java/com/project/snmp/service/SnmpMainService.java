package com.project.snmp.service;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.project.snmp.service.snmpServices.*;

@Service
public class SnmpMainService {
    
    public JSONObject getSnmpValue(String ipAddress, String community,String oid) throws Exception {
        SnmpGet snmpGet = new SnmpGet();
        return snmpGet.getAsJson(ipAddress, community,oid);
    }
}
