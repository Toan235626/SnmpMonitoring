package com.project.snmp.controller;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.snmp.model.Device;
import com.project.snmp.model.SnmpRecord;
import com.project.snmp.service.NetworkScannerService;
import com.project.snmp.service.SnmpBroadcast;
import com.project.snmp.service.SnmpMainService;
import com.project.snmp.utils.NetworkUtils;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.incrementer.HsqlMaxValueIncrementer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
@RequestMapping("/device-scan")
public class DeviceScanController {
    @Autowired
    NetworkScannerService networkScannerService;
    @Autowired
    SnmpBroadcast snmpBroadcast;
    @Autowired
    SnmpMainService snmpMainService;

    @PostConstruct
    public void debug() {
        // System.out.println("[✓] DeviceScanController loaded");
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/scan-subnet")
    public List<Device> scanSelectedSubnet(
            @RequestParam(value = "mode", required = false, defaultValue = "unicast") String mode,
            @RequestParam("baseIp") String baseIp,
            @RequestParam(value = "community", required = false, defaultValue = "public") String community,
            @RequestParam(value = "prefix", required = false, defaultValue = "24") String prefix,
            @RequestParam(value = "port", required = false, defaultValue = "161") int port,
            @RequestParam(value = "version", required = false, defaultValue = "2c") String version,
            @RequestParam(value = "authUsername", required = false) String authUsername,
            @RequestParam(value = "authPass", required = false, defaultValue = "") String authPass,
            @RequestParam(value = "privPass", required = false, defaultValue = "") String privPass,
            @RequestParam(value = "authProtocol", required = false, defaultValue = "") String authProtocol,
            @RequestParam(value = "privProtocol", required = false, defaultValue = "") String privProtocol,
            @RequestParam(value = "securityLevel", required = false, defaultValue = "1") String securityLevel,
            @RequestParam(value = "oid", required = false, defaultValue = "1.3.6.1.2.1.1.1.0") String oid)
            throws Exception {

        if (version.equals("3")) {
            int securityLevelInt = Integer.parseInt(securityLevel);
            return networkScannerService.scanSubnetV3(baseIp, prefix, community, port, version, authUsername, authPass,
                    privPass, authProtocol, privProtocol, securityLevelInt, oid);
        } else if (version.equals("1") || version.equals("2c")) {
            if (mode.equals("broadcast")) {
                List<String> ips = SnmpBroadcast.sendBroadcastSnmpGet(baseIp, prefix, community, port,
                        "1.3.6.1.2.1.1.1.0");
                List<Device> devices = new ArrayList<Device>();
                for (String ip : ips) {
                    SnmpRecord nameRecord = snmpMainService.getSnmpValue(ip, community, "1.3.6.1.2.1.1.1.0", port,
                            version);
                    Device device = new Device();
                    device.setDeviceIp(ip);
                    device.setName(nameRecord.getValue());
                    SnmpRecord record = snmpMainService.getSnmpValue(ip, community, oid, port, version);
                    device.setValue(record.getValue());
                    devices.add(device);
                }
                return devices;
            }
            return networkScannerService.scanSubnetV12(baseIp, prefix, community, port, version, oid);
        } else {
            throw new IllegalArgumentException("Unsupported SNMP version: " + version);
        }
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/networks")
    public List<HashMap<String, String>> getLocalSubnets() {
        List<HashMap<String, String>> subnets = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    InetAddress inetAddr = addr.getAddress();
                    if (!(inetAddr instanceof Inet4Address))
                        continue;

                    String ip = inetAddr.getHostAddress();
                    int prefix = addr.getNetworkPrefixLength();
                    int ipInt = NetworkUtils.ipToInt(ip);
                    int mask = NetworkUtils.prefixToMask(prefix);
                    int baseIpInt = ipInt & mask;
                    String baseIp = NetworkUtils.intToIp(baseIpInt);

                    HashMap<String, String> netInfo = new HashMap<>();
                    netInfo.put("baseIp", baseIp);
                    netInfo.put("prefix", String.valueOf(prefix));

                    boolean alreadyAdded = subnets.stream().anyMatch(map -> map.get("baseIp").equals(baseIp));
                    if (!alreadyAdded) {
                        subnets.add(netInfo);
                        // System.out.println("Found subnet: " + baseIp);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subnets;
    }

}