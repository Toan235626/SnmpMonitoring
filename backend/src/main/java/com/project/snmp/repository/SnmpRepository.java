package com.project.snmp.repository;

import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import com.project.snmp.model.Device;
import com.project.snmp.model.SnmpRecord;

import java.util.ArrayList;
import java.util.List;

@Repository
public class SnmpRepository {
    // Temporary in-memory database: deviceIp -> OID values (as JSONObject)
    List<SnmpRecord> snmpDatabase = new ArrayList<>();

    public void saveOidValue(SnmpRecord record) {
        snmpDatabase.add(record);
    }

    public void removeOidValue(SnmpRecord record) { 
        snmpDatabase.remove(record);
    }

    public List<SnmpRecord> getOidValues(String deviceIp) {
        List<SnmpRecord> deviceOidDatabase = new ArrayList<>();
        for (SnmpRecord record : snmpDatabase) {
            if (record.getDevice().equals(deviceIp)) {
                deviceOidDatabase.add(record);
            }
        }
        return deviceOidDatabase;
    }

    public void removeOidValues(String deviceIp) {
        for (int i = snmpDatabase.size() - 1; i >= 0; i--) {
            if (snmpDatabase.get(i).getDevice().equals(deviceIp)) {
                snmpDatabase.remove(i);
            }
        }
    }

    public void updateOidValue(SnmpRecord record) {
        String deviceIp = record.getDevice();
        String oid = record.getOid();
        Boolean found = false;
        for (int i = 0; i < snmpDatabase.size(); i++) {
            SnmpRecord recordTemp = snmpDatabase.get(i);
            if (recordTemp.getDevice().equals(deviceIp) && recordTemp.getOid().equals(oid)) {
                recordTemp.setValue(record.getValue());
                found = true;
                break;
            }
        }
        if (!found) {
            snmpDatabase.add(record);
        }
    }

}