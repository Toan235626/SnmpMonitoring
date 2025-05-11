package com.project.snmp.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.snmp.model.Device;
import com.project.snmp.repository.DeviceRepository;

@Service

public class NetworkScannerService {
    @Autowired
    private DeviceRepository deviceRepository;

    public void scanSubnet(String baseIp, String community) {
        for (int i = 1; i <= 254; i++) {
            String ip = baseIp + "." + i;
            SnmpMainService snmpMainService = new SnmpMainService(ip, community);
            try {
                JSONObject result = snmpMainService.getSnmpValue("1.3.6.1.2.1.1.1.0");
                String name = result.getString("value");
                if (result != null) {
                    Device device = new Device();
                    device.setIpAddress(ip);
                    device.setName("SNMP: " + name);
                    device.setCommunity(community);
                    deviceRepository.save(device);
                }
            } catch (Exception e) { }
        }
    }


}


