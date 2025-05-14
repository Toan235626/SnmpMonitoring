package com.project.snmp.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.snmp4j.Snmp;


public class SnmpStringToJson {
    private String snmpStrings;

    public SnmpStringToJson(String snmpString) {
        this.snmpStrings = snmpString;
    }

    public JSONArray toJson() {
        String[] snmpString = snmpStrings.split(", ");
        JSONArray jsonObjects = new JSONArray();

        for (int i = 0; i < snmpString.length; i++) {
            String[] parts = snmpString[i].split(" = ");
            if (parts.length == 2) {
                String oid = parts[0].trim();
                String value = parts[1].trim();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("oid", oid);
                jsonObject.put("value", value);
                jsonObjects.put(jsonObject);
            } else if (parts.length == 1) {
                String oid = parts[0].trim();
                String value = "No Value";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("oid", oid);
                jsonObject.put("value", value);
                jsonObjects.put(jsonObject);
            } else {
                throw new IllegalArgumentException("Invalid SNMP string format: " + snmpString);
            }
        }
        return jsonObjects;
    }
}
