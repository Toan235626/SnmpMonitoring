package com.project.snmp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.snmp.model.Device;
import com.project.snmp.service.snmpServices.SnmpService;


@RestController
@RequestMapping("/api/snmp")
public class SnmpController {
    @Autowired
    private SnmpService snmpService;

    @PostMapping("/get")
    public ResponseEntity<?> getSnmpData(@RequestBody Device device) {
        try {
            System.out.println("[DEBUG] Received device: " + device);
            return ResponseEntity.ok(
                snmpService.getOidValue(
                    device.getIpAddress(),
                    device.getCommunity(),
                    device.getOid()
                )
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/devices")
    public List<Device> getAllDevices() {
        return snmpService.getAllDevices();
    }

    @PostMapping("/devices")
    public Device createDevice(@RequestBody Device device) {
        return snmpService.createDevice(device);
    }

    @PutMapping("/devices/{id}")
    public Device updateDevice(@PathVariable Long id, @RequestBody Device device) {
        return snmpService.updateDevice(id, device);
    }

    @DeleteMapping("/devices/{id}")
    public void deleteDevice(@PathVariable Long id) {
        snmpService.deleteDevice(id);
    }
}
