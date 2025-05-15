package com.project.snmp.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.snmp.model.Device;
import com.project.snmp.model.SnmpRecord;
import com.project.snmp.repository.DeviceRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class NetworkScannerService {
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private SnmpMainService snmpMainService;

    public List<Device> scanSubnet(String baseIp, String community) {
        ExecutorService executor = Executors.newFixedThreadPool(20); // 20 threads, adjust as needed
        for (int i = 1; i <= 254; i++) {
            final String ipAddress = baseIp + "." + i;
            executor.submit(() -> {
                System.out.println("Scanning IP: " + ipAddress);
                try {
                    SnmpRecord result = snmpMainService.getSnmpValue(ipAddress, community, "1.3.6.1.2.1.1.1.0");
                    if (result != null) {
                        String name = result.getValue();
                        Device device = new Device();
                        device.setIpAddress(ipAddress);
                        device.setName(name);
                        device.setCommunity(community);
                        System.out.println("Device found: " + device.getName() + " at " + device.getIpAddress());
                        if (!deviceRepository.existsByIpAddress(ipAddress)) {
                            deviceRepository.save(device);
                        }
                    }
                } catch (Exception e) {}});
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, java.util.concurrent.TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        return deviceRepository.findAll();
    }

}



