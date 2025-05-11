package com.project.snmp.utils;

import org.json.JSONObject;
import org.snmp4j.Snmp;


public class SnmpStringToJson {
    private String snmpString;

    public SnmpStringToJson(String snmpString) {
        this.snmpString = snmpString;
    }

    public JSONObject toJson() {
        String[] parts = snmpString.split(" = ");
        if (parts.length == 2) {
            String oid = parts[0].trim();
            String value = parts[1].trim();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("oid", oid);
            jsonObject.put("value", value);

            return jsonObject;
        } else {
            throw new IllegalArgumentException("Invalid SNMP string format: " + snmpString);
        }
    }
}
