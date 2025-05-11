package com.project.snmp.controller;

import org.json.JSONObject;
import org.snmp4j.Snmp;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.snmp.service.SnmpMainService;

@Controller
@RequestMapping("/api/snmp/{ip}")

public class SnmpController {

    @GetMapping("/get/{oid}")
    @ResponseBody
    public JSONObject getSnmpData(@PathVariable String ip, @RequestHeader("Snmp-Community") String community, @PathVariable String oid) {
        try {
            SnmpMainService snmpService = new SnmpMainService(ip, community);
            JSONObject result = snmpService.getSnmpValue(oid);

            if (result.has("snmpErrorStatus")) {
                int errorStatus = result.getInt("snmpErrorStatus");
                if (errorStatus == 16) {    //16 = authorizationError in SNMP
                    JSONObject errorObj = new JSONObject();
                    errorObj.put("error", "Unauthorized or incorrect community string.");
                    return errorObj;
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred while processing SNMP GET request: " + e.getMessage());
        }
    }

    @GetMapping("/set/{oid}/{value}")
    @ResponseBody
    public String setSnmpData(@PathVariable String ip, @PathVariable String oid, @PathVariable String value) {
        return "SNMP set response for IP: " + ip + " and OID: " + oid;
    }

    @GetMapping("/walk/{oid}")
    @ResponseBody
    public String walkSnmpData(@PathVariable String ip, @RequestHeader("Snmp-Community") String community, @PathVariable String oid) {
        return "SNMP walk response for IP: " + ip + " and OID: " + oid;
    }

    @GetMapping("/bulk/{oid}/{count}")
    @ResponseBody
    public String bulkSnmpData(@PathVariable String ip, @PathVariable String oid, @PathVariable int count) {
        return "SNMP bulk response for IP: " + ip + ", OID: " + oid + ", and Count: " + count;
    }
}