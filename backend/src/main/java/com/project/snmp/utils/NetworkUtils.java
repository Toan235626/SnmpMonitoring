package com.project.snmp.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class NetworkUtils {
    public static int prefixToMask(int prefixLength) {
        return ~((1 << (32 - prefixLength)) - 1);
    }
    public static int ipToInt(String ipAddress) throws UnknownHostException {
        byte[] bytes = InetAddress.getByName(ipAddress).getAddress();
        return ByteBuffer.wrap(bytes).getInt();
    }
    public static String intToIp(int ip) {
        return String.format("%d.%d.%d.%d",
                (ip >> 24) & 0xFF,
                (ip >> 16) & 0xFF,
                (ip >> 8) & 0xFF,
                ip & 0xFF);
    }
}
