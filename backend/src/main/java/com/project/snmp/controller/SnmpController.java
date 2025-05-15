package com.project.snmp.controller;

import org.hibernate.engine.internal.Collections;
import org.json.JSONObject;
import org.snmp4j.Snmp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.snmp.model.SnmpRecord;
import com.project.snmp.service.SnmpMainService;

@Controller
@RequestMapping("/snmp")
public class SnmpController {
    @Autowired
    private SnmpMainService snmpMainService;

    @PostMapping("/get")
    @ResponseBody
    public ResponseEntity<SnmpRecord> getSnmpData(
            @RequestParam("deviceIp") String deviceIp, 
            @RequestParam("community") String community, 
            @RequestParam("oid") String oid) {
        System.out.println("deviceIP: " + deviceIp);
        try {
            SnmpRecord record = snmpMainService.getSnmpValue(deviceIp, community, oid);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/set")
    @ResponseBody
    public String setSnmpData(
            @RequestParam String deviceIp, 
            @RequestParam("oid") String oid, 
            @RequestParam("value") String value) {
        return "SNMP set response for deviceIP: " + deviceIp + " and OID: " + oid;
    }

    @PostMapping("/walk")
    @ResponseBody
    public ResponseEntity<SnmpRecord[]> walkSnmpData(
            @RequestParam("deviceIp") String deviceIp,
            @RequestParam("community") String community,
            @RequestParam("oid") String oid) {
        try {
            SnmpRecord[] records = snmpMainService.getSnmpWalkValue(deviceIp, community, oid);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/bulk")
    @ResponseBody
    public ResponseEntity<SnmpRecord[]> bulkSnmpData(
            @RequestParam("deviceIp") String deviceIp, 
            @RequestParam("oid") String oid, 
            @RequestParam("community") String community) {
        try {
            SnmpRecord[] records = snmpMainService.getSnmpBulkValue(deviceIp, community, oid);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}