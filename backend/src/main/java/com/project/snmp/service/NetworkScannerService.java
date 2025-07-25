package com.project.snmp.service;

import org.json.JSONObject;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.snmp.model.Device;
import com.project.snmp.model.SnmpRecord;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class NetworkScannerService {
    @Autowired
    private SnmpMainService snmpMainService;

    public List<Device> scanSubnetV12(String baseIp, String prefix, String community, int port, String version,
            String oid) {
        ExecutorService executor = Executors.newFixedThreadPool(800);
        List<Device> foundDevices = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

        String[] parts = baseIp.split("\\.");
        int prefixInt = Integer.parseInt(prefix);

        // Calculate the number of third octets needed for the subnet
        int thirdOctetCount = (prefixInt <= 24) ? (1 << (24 - prefixInt)) : 1;

        for (int j = 0; j < thirdOctetCount; j++) {
            int thirdOctet = Integer.parseInt(parts[2]) + j;
            for (int i = 1; i <= 254; i++) {
                final String deviceIp = parts[0] + "." + parts[1] + "." + thirdOctet + "." + i;
                executor.submit(() -> {
                    // System.out.println("Scanning IP: " + deviceIp);
                    try {
                        SnmpRecord result = snmpMainService.getSnmpValue(deviceIp, community, oid, port,
                                version);
                        SnmpRecord nameResult = snmpMainService.getSnmpValue(deviceIp, community, "1.3.6.1.2.1.1.1",
                                port, version);
                        if (result != null && result.getValue() != null && result.getValue() != "Null") {
                            String name = nameResult.getValue();
                            String value = result.getValue();
                            Device device = new Device();
                            device.setValue(value);
                            device.setName(name);
                            device.setDeviceIp(deviceIp);
                            device.setCommunity(community);
                            foundDevices.add(device);
                            // System.out.println("Device found: " + device.getName() + " at " +
                            // device.getDeviceIp());
                        }
                    } catch (Exception e) {
                    }
                });
            }
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
        return foundDevices;
    }

    public List<Device> scanSubnetV3(String baseIp, String prefix, String community, int port, String version,
            String username, String authPass, String privPass,
            String authProtocol, String privProtocol, int securityLevel, String oid) {
        ExecutorService executor = Executors.newFixedThreadPool(800);
        List<Device> foundDevices = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

        String[] parts = baseIp.split("\\.");
        int prefixInt = Integer.parseInt(prefix);

        // Calculate the number of third octets needed for the subnet
        int thirdOctetCount = (prefixInt <= 24) ? (1 << (24 - prefixInt)) : 1;

        for (int j = 0; j < thirdOctetCount; j++) {
            int thirdOctet = Integer.parseInt(parts[2]) + j;
            for (int i = 1; i <= 254; i++) {
                final String deviceIp = parts[0] + "." + parts[1] + "." + thirdOctet + "." + i;
                executor.submit(() -> {
                    // System.out.println("Scanning IP: " + deviceIp);
                    try {
                        SnmpRecord result = snmpMainService.getSnmpValue(deviceIp, community, oid, port,
                                version,
                                username, authPass, privPass, authProtocol, privProtocol,
                                String.valueOf(securityLevel));
                        if (result != null && result.getValue() != null && result.getValue() != "Null"
                                && !result.getOid().startsWith("1.3.6.1.6.3.15.1.1")) {
                            String name = result.getValue();
                            Device device = new Device();
                            device.setDeviceIp(deviceIp);
                            device.setName(name);
                            device.setCommunity(community);
                            foundDevices.add(device);
                            System.out.println("Device found: " + device.getName() + " at " + device.getDeviceIp());
                        }
                    } catch (Exception e) {
                    }
                });
            }
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
        return foundDevices;
    }
}
