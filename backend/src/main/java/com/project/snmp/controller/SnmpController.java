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
import org.springframework.web.bind.annotation.RestController;

import com.project.snmp.model.SnmpRecord;
import com.project.snmp.service.SnmpMainService;

@RestController
@RequestMapping("/snmp")
public class SnmpController {
    @Autowired
    private SnmpMainService snmpMainService;

    @PostMapping("/get")
    public SnmpRecord[] getSnmpData(
            @RequestParam("deviceIp") String deviceIp, 
            @RequestParam(value = "community", required = false, defaultValue = "public") String community, 
            @RequestParam("oid") String oid,
            @RequestParam(value = "port", required = false, defaultValue = "161") int port,
            @RequestParam(value = "version", required = false, defaultValue = "2c") String version,
            @RequestParam(value = "authUsername", required = false, defaultValue = "nktoan") String authUsername,
            @RequestParam(value = "authPass", required = false, defaultValue = "nktoanauth") String authPass,
            @RequestParam(value = "privPass", required = false, defaultValue = "nktoanpriv") String privPass,
            @RequestParam(value = "authProtocol", required = false, defaultValue = "SHA") String authProtocol,
            @RequestParam(value = "privProtocol", required = false, defaultValue = "DES") String privProtocol,
            @RequestParam(value = "securityLevel", required = false, defaultValue = "3") String securityLevel) {

        if (version.equals("3") && (securityLevel.equals("3")) && (authUsername == null || authPass == null || privPass == null || authProtocol == null || privProtocol == null)) {
            throw new IllegalArgumentException("SNMPv3 requires all authentication parameters to be provided.");
        }
        System.out.println("deviceIP: " + deviceIp);
        try {
            if (version.equals("2c") || version.equals("1")) {
                SnmpRecord[] records = new SnmpRecord[1];
                records[0] = snmpMainService.getSnmpValue(deviceIp, community, oid, port, version);
                return records;
            } else if (version.equals("3")) {
                SnmpRecord[] records = new SnmpRecord[1];
                records[0] = snmpMainService.getSnmpValue(deviceIp, community, oid, port, version, authUsername, authPass, privPass, authProtocol, privProtocol, securityLevel);
                return records;
            } else {
                throw new RuntimeException("Wrong version input");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred while performing SNMP get operation", e);
        }
    }

    @PostMapping("/getnext")
    public SnmpRecord[] getSnmpNextData(
            @RequestParam("deviceIp") String deviceIp, 
            @RequestParam(value ="community", required = false, defaultValue = "public") String community, 
            @RequestParam("oid") String oid,
            @RequestParam(value = "port", required = false, defaultValue = "161") int port,
            @RequestParam(value = "version", required = false, defaultValue = "2c") String version,
            @RequestParam(value = "authUsername", required = false, defaultValue = "nktoan") String authUsername,
            @RequestParam(value = "authPass", required = false, defaultValue = "nktoanauth") String authPass,
            @RequestParam(value = "privPass", required = false, defaultValue = "nktoanpriv") String privPass,
            @RequestParam(value = "authProtocol", required = false, defaultValue = "SHA") String authProtocol,
            @RequestParam(value = "privProtocol", required = false, defaultValue = "DES") String privProtocol,
            @RequestParam(value = "securityLevel", required = false, defaultValue = "3") String securityLevel) {

        if (version.equals("3") && (securityLevel.equals("3")) && (authUsername == null || authPass == null || privPass == null || authProtocol == null || privProtocol == null)) {
            throw new IllegalArgumentException("SNMPv3 requires all authentication parameters to be provided.");
        }
        System.out.println("deviceIP: " + deviceIp);
        try {
            SnmpRecord[] records = new SnmpRecord[1];
            if (version.equals("2c")){
                records[0] = snmpMainService.getSnmpNextValue(deviceIp, community, oid, port, version);
                return records;
            }
            else if(version.equals("3")) {
                records[0] = snmpMainService.getSnmpNextValue(deviceIp, community, oid, port, version,authUsername,authPass,privPass,authProtocol,privProtocol,securityLevel);
                return records;
            }
            else{
                throw new RuntimeException("Wrong version input");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred while performing SNMP getnext operation", e);
        }
    }

    
    @PostMapping("/walk")
    public SnmpRecord[] walkSnmpData(
            @RequestParam("deviceIp") String deviceIp,
            @RequestParam(value = "community", required = false, defaultValue = "public") String community,
            @RequestParam("oid") String oid,
            @RequestParam(value = "port", required = false, defaultValue = "161") int port,
            @RequestParam(value = "version", required = false, defaultValue = "2c") String version,
            @RequestParam(value = "authUsername", required = false, defaultValue = "zuzo") String authUsername,
            @RequestParam(value = "authPass", required = false, defaultValue = "hoangzuzi14") String authPass,
            @RequestParam(value = "privPass", required = false, defaultValue = "zuzozuzo") String privPass,
            @RequestParam(value = "authProtocol", required = false, defaultValue = "SHA") String authProtocol,
            @RequestParam(value = "privProtocol", required = false, defaultValue = "AES") String privProtocol,
            @RequestParam(value = "securityLevel", required = false, defaultValue = "3") String securityLevel) {
        
        if (version.equals("3") && (securityLevel.equals("3")) && (authUsername == null || authPass == null || privPass == null || authProtocol == null || privProtocol == null)) {
            throw new IllegalArgumentException("SNMPv3 requires all authentication parameters to be provided.");
        }
        try {
            if (version.equals("2c") || version.equals("1")) {
                SnmpRecord[] records = snmpMainService.getSnmpWalkValue(deviceIp, community, oid, port, version);
                return records;
            } else if (version.equals("3")) {
                SnmpRecord[] records = snmpMainService.getSnmpWalkValue(deviceIp, community, oid, port, version, authUsername, authPass, privPass, authProtocol, privProtocol, securityLevel);
                return records;
            } else {
                throw new RuntimeException("Wrong version input");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred while performing SNMP walk operation", e);
        }
    }

    @PostMapping("/bulk")
    public SnmpRecord[] bulkSnmpData(
            @RequestParam("deviceIp") String deviceIp, 
            @RequestParam("oid") String oid, 
            @RequestParam(value = "community", required = false, defaultValue = "public") String community,
            @RequestParam(value = "port", required = false, defaultValue = "161") int port,
            @RequestParam(value = "version", required = false, defaultValue = "2c") String version,
            @RequestParam(value = "authUsername", required = false) String authUsername,
            @RequestParam(value = "authPass", required = false) String authPass,
            @RequestParam(value = "privPass", required = false) String privPass,
            @RequestParam(value = "authProtocol", required = false) String authProtocol,
            @RequestParam(value = "privProtocol", required = false) String privProtocol,
            @RequestParam(value = "securityLevel", required = false, defaultValue = "3") String securityLevel) {
        
        if (version.equals("3") && (securityLevel.equals("3")) && (authUsername == null || authPass == null || privPass == null || authProtocol == null || privProtocol == null)) {
            throw new IllegalArgumentException("SNMPv3 requires all authentication parameters to be provided.");
        }
        try {
            if (version.equals("2c") || version.equals("1")) {
                SnmpRecord[] records = snmpMainService.getSnmpBulkValue(deviceIp, community, oid, port, version);
                return records;
            }
            else if (version.equals("3")) {
                SnmpRecord[] records = snmpMainService.getSnmpBulkValue(deviceIp, community, oid, port, version, 
                    authUsername, authPass, privPass, authProtocol, privProtocol, securityLevel);
            return records;
            }
            else {
                throw new RuntimeException("Wrong version input");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred while performing SNMP bulk operation", e);
        }
    }
}