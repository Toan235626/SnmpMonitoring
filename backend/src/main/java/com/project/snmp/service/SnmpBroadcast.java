package com.project.snmp.service;

import java.util.concurrent.*;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SnmpBroadcast {

    public static List<String> sendBroadcastSnmpGet(String baseIp, String prefix, String community, int port,
            String oid) throws Exception {

        List<String> allResponses = new ArrayList<>();

        String[] parts = baseIp.split("\\.");
        int prefixInt = Integer.parseInt(prefix);
        int thirdOctetBase = Integer.parseInt(parts[2]);
        int subnetCount = (prefixInt <= 24) ? (1 << (24 - prefixInt)) : 1;

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(20, subnetCount));

        List<Future<List<String>>> futures = new ArrayList<>();

        for (int j = 0; j < subnetCount; j++) {
            int thirdOctet = thirdOctetBase + j;
            String broadcastIp = parts[0] + "." + parts[1] + "." + thirdOctet + ".255";

            // System.out.println(" Submit broadcast to: " + broadcastIp);
            Future<List<String>> future = executor.submit(() -> sendSingleBroadcast(broadcastIp, port, community, oid));
            futures.add(future);
        }

        for (Future<List<String>> future : futures) {
            try {
                allResponses.addAll(future.get(2, TimeUnit.SECONDS)); // timeout nếu cần
            } catch (TimeoutException e) {
                System.err.println("Timeout on broadcast response");
            }
        }

        executor.shutdown();
        return allResponses;
    }

    private static List<String> sendSingleBroadcast(String broadcastIp, int port, String community, String oid)
            throws Exception {
        List<String> responses = new ArrayList<>();

        byte[] request = createSnmpV2cGetPacket(community, oid);

        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        socket.setSoTimeout(1000);

        DatagramPacket packet = new DatagramPacket(
                request, request.length,
                InetAddress.getByName(broadcastIp), port);
        socket.send(packet);

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 1000) {
            try {
                byte[] buf = new byte[2048];
                DatagramPacket response = new DatagramPacket(buf, buf.length);
                socket.receive(response);

                String fromIp = response.getAddress().getHostAddress();
                if (!responses.contains(fromIp)) {
                    responses.add(fromIp);
                }

            } catch (SocketTimeoutException e) {
            }
        }

        socket.close();
        return responses;
    }

    public static byte[] createSnmpV2cGetPacket(String community, String oidStr) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] oid = encodeOID(oidStr);

        // VarBind = SEQUENCE { OID, NULL }
        ByteArrayOutputStream varBind = new ByteArrayOutputStream();
        varBind.write(0x06);
        varBind.write(oid.length);
        varBind.write(oid); // OID
        varBind.write(0x05);
        varBind.write(0x00); // NULL

        byte[] varBindContent = varBind.toByteArray();
        ByteArrayOutputStream varBindSeq = new ByteArrayOutputStream();
        varBindSeq.write(0x30);
        varBindSeq.write(varBindContent.length);
        varBindSeq.write(varBindContent); // VarBind

        byte[] vbList = varBindSeq.toByteArray();
        ByteArrayOutputStream vbListSeq = new ByteArrayOutputStream();
        vbListSeq.write(0x30);
        vbListSeq.write(vbList.length);
        vbListSeq.write(vbList);

        ByteArrayOutputStream pdu = new ByteArrayOutputStream();
        pdu.write(0x02);
        pdu.write(1);
        pdu.write(1); // request-id = 1
        pdu.write(0x02);
        pdu.write(1);
        pdu.write(0); // error-status = 0
        pdu.write(0x02);
        pdu.write(1);
        pdu.write(0); // error-index = 0
        pdu.write(vbListSeq.toByteArray());

        byte[] pduBytes = pdu.toByteArray();
        out.write(0xA0);
        out.write(pduBytes.length);
        out.write(pduBytes); // GET PDU

        // Header: version, community, PDU
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        header.write(0x02);
        header.write(1);
        header.write(1); // version = 1 (v2c)
        header.write(0x04);
        header.write(community.length());
        header.write(community.getBytes());
        header.write(out.toByteArray());

        byte[] body = header.toByteArray();
        ByteArrayOutputStream message = new ByteArrayOutputStream();
        message.write(0x30);
        message.write(body.length);
        message.write(body);

        return message.toByteArray();
    }

    public static byte[] encodeOID(String oidStr) {
        String[] parts = oidStr.split("\\.");
        int[] oid = Arrays.stream(parts).mapToInt(Integer::parseInt).toArray();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(40 * oid[0] + oid[1]);
        for (int i = 2; i < oid.length; i++) {
            int val = oid[i];
            if (val < 128) {
                out.write(val);
            } else {
                out.write(0x80 | (val >> 7));
                out.write(val & 0x7F);
            }
        }

        return out.toByteArray();
    }

    public static String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString().trim();
    }

    public static String decodeSnmpResponse(byte[] data, int length) {
        try {
            int index = 0;

            // Bỏ qua header: SEQUENCE (0x30), độ dài
            if (data[index++] != 0x30)
                return "Invalid SNMP (missing SEQ)";
            index += getLengthFieldSize(data[index]);

            // Version
            if (data[index++] != 0x02)
                return "Invalid SNMP (missing version)";
            index += 1 + data[index]; // skip version

            // Community string
            if (data[index++] != 0x04)
                return "Invalid SNMP (missing community)";
            int commLen = data[index++];
            index += commLen;

            // PDU
            if (data[index++] != (byte) 0xA2)
                return "Not a Response-PDU (0xA2)";
            index += getLengthFieldSize(data[index]);

            // request-id
            if (data[index++] != 0x02)
                return "Missing request-id";
            index += 1 + data[index];

            // error-status
            if (data[index++] != 0x02)
                return "Missing error-status";
            index += 1 + data[index];

            // error-index
            if (data[index++] != 0x02)
                return "Missing error-index";
            index += 1 + data[index];

            // VarBindList
            if (data[index++] != 0x30)
                return "Missing VarBindList";
            index += getLengthFieldSize(data[index]);

            // VarBind
            if (data[index++] != 0x30)
                return "Missing VarBind";
            index += getLengthFieldSize(data[index]);

            // OID
            if (data[index++] != 0x06)
                return "Missing OID";
            int oidLen = data[index++];
            index += oidLen;

            // VALUE
            byte type = data[index++];
            int valLen = data[index++];
            byte[] val = Arrays.copyOfRange(data, index, index + valLen);
            return new String(val);
        } catch (Exception e) {
            return "Decode error: " + e.getMessage();
        }
    }

    private static int getLengthFieldSize(int lenByte) {
        if ((lenByte & 0x80) == 0)
            return 1;
        else
            return (lenByte & 0x7F) + 1;
    }

}