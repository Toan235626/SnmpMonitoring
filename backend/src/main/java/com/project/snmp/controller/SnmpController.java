package com.project.snmp.controller;

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
    public ResponseEntity<SnmpRecord> getSnmpData(@RequestParam("ip") String ip, @RequestParam("community") String community, @RequestParam("oid") String oid) {
        System.out.println("IP: " + ip);
        try {
            SnmpRecord record = snmpMainService.getSnmpValue(ip, community, oid);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/set")
    @ResponseBody
    public String setSnmpData(@PathVariable String ip, @RequestParam("oid") String oid, @RequestParam("value") String value) {
        return "SNMP set response for IP: " + ip + " and OID: " + oid;
    }

    @GetMapping("/walk")
    @ResponseBody
    public String walkSnmpData(@PathVariable String ip, @RequestParam("community") String community, @RequestParam("oid") String oid) {
        return "SNMP walk response for IP: " + ip + " and OID: " + oid;
    }

    @GetMapping("/bulk")
    @ResponseBody
    public String bulkSnmpData(@PathVariable String ip, @RequestParam("oid") String oid, @RequestParam("count") int count) {
        return "SNMP bulk response for IP: " + ip + ", OID: " + oid + ", and Count: " + count;
    }
}