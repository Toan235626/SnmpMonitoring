package com.project.snmp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.snmp.service.SnmpService;
import com.project.snmp.model.Device;

@RestController
public class SnmpController {
    @Autowired
    private SnmpService snmpService;
    
    @GetMapping("/api/snmp/devices")
    public List<Device> getAllDevices() {
        return snmpService.getAllDevices();
    }
    @PostMapping("/api/snmp/devices")
    public Device createDevice(@RequestBody Device device) {
        return snmpService.createDevice(device);
    }
    @PutMapping("/api/devices/{id}")
    public Device updateDevice(@PathVariable Long id, @RequestBody Device device) {
        return snmpService.updateDevice(id, device);
    }
    @DeleteMapping("/api/devices/{id}")
    public void deleteDevice(@PathVariable Long id) {
        snmpService.deleteDevice(id);
    }
}
