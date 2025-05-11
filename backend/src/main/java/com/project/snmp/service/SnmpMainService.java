package com.project.snmp.service;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.snmp.service.snmpServices.*;

@Service
public class SnmpMainService {
    
    @Autowired
    protected String ipAddress;
    @Autowired
    protected String community;
    
    public SnmpMainService(@RequestParam String ipAddress,@RequestParam String community) {
        this.ipAddress = ipAddress;
        this.community = community;
    }

    public JSONObject getSnmpValue(String oid) throws Exception {
        SnmpGet snmpGet = new SnmpGet(ipAddress, community);
        return snmpGet.getAsJson(oid);
    }
}
