package com.project.snmp.controller;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.snmp.model.Device;
import com.project.snmp.service.NetworkScannerService;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/scan-subnet")
    public List<Device> scanSelectedSubnet(
                @RequestParam("baseIp") String baseIp,
                @RequestParam(value ="community", required = false, defaultValue = "public") String community,
                @RequestParam(value = "port", required = false, defaultValue = "161") int port,
                @RequestParam("version") String version,
                @RequestParam(value = "authUsername", required = false) String authUsername,
                @RequestParam(value = "authPass", required = false) String authPass,
                @RequestParam(value = "privPass", required = false) String privPass,
                @RequestParam(value = "authProtocol", required = false) String authProtocol,
                @RequestParam(value = "privProtocol", required = false) String privProtocol,
                @RequestParam(value = "securityLevel", required = false, defaultValue = "0") String securityLevel){
        if (version.equals("3") && (authUsername == null || authPass == null || privPass == null || authProtocol == null || privProtocol == null)) {
            throw new IllegalArgumentException("SNMPv3 requires all authentication parameters to be provided.");
        }
        return networkScannerService.scanSubnet(baseIp, community, port, version);
    }


    @PostMapping("/networks")
    public List<String> getLocalSubnets() {
        List<String> subnets = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    InetAddress inetAddr = addr.getAddress();
                    if (inetAddr instanceof Inet4Address) {
                        String[] parts = inetAddr.getHostAddress().split("\\.");
                        if (parts.length == 4) {
                            String baseIp = parts[0] + "." + parts[1] + "." + parts[2];
                            if (!subnets.contains(baseIp)) {
                                subnets.add(baseIp);
                                System.out.println("Found subnet: " + baseIp);
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return subnets;
    }
}
