package com.project.snmp.controller;

import java.net.UnknownHostException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.snmp.service.NetworkScannerService;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@RestController
@RequestMapping("/device-scan")
public class DeviceScanController {
    @Autowired
    NetworkScannerService networkScannerService;
    
    @PostConstruct
    public void debug() {
        System.out.println("[✓] DeviceScanController loaded");
    }

    @GetMapping("/scan")
    public String scanDevices() {
        try {
            String localIp = java.net.InetAddress.getLocalHost().getHostAddress();
            String[] parts = localIp.split("\\.");
            if (parts.length == 4) {
                String baseIp = parts[0] + "." + parts[1] + "." + parts[2];
                System.out.println("Base IP: " + baseIp);
                String community = "public"; 
                networkScannerService.scanSubnet(baseIp, community);
                System.out.println("Scanning completed.");
                return "Scanning completed.";
            } else {
                return "Invalid local IP address format.";
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Failed to get local IP address: " + e.getMessage(); 
        }
    }
}
