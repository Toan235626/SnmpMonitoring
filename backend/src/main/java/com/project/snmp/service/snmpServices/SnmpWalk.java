package com.project.snmp.service.snmpServices;

import org.json.JSONArray;
import org.json.JSONObject;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;
import org.springframework.stereotype.Component;

import com.project.snmp.model.SnmpRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SnmpWalk {

    public SnmpRecord[] walkV1Records(String deviceIp, String community, String rootOid, int port) throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        Snmp snmp = new Snmp(transport);

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(deviceIp + "/" + port));
        target.setRetries(2);
        target.setTimeout(3000);
        target.setVersion(SnmpConstants.version1);

        List<SnmpRecord> results = new ArrayList<>();

        try {
            OID root = new OID(rootOid);
            OID currentOid = root;
            OID lastOid = null;
            int step = 0, maxSteps = 50000;

            while (step++ < maxSteps) {
                PDU pdu = new PDU();
                pdu.add(new VariableBinding(currentOid));
                pdu.setType(PDU.GETNEXT);

                ResponseEvent responseEvent = snmp.send(pdu, target);

                if (responseEvent == null || responseEvent.getResponse() == null) {
                    System.err.println("Timeout at step " + step);
                    break;
                }

                VariableBinding vb = responseEvent.getResponse().get(0);
                if (vb == null || vb.getOid() == null) break;

                if (lastOid != null && vb.getOid().equals(lastOid)) break;
                lastOid = vb.getOid();

                if (!vb.getOid().startsWith(root)) break;

                String varStr = vb.getVariable() == null ? "" : vb.getVariable().toString().toLowerCase();
                if (varStr.contains("error") || varStr.contains("nosuch")) {
                    currentOid = vb.getOid().successor();
                    continue;
                }

                SnmpRecord record = new SnmpRecord();
                record.setDeviceIp(deviceIp);
                record.setCommunity(community);
                record.setOid(vb.getOid().toString());
                record.setValue(varStr.isEmpty() ? "null" : vb.getVariable().toString());
                results.add(record);

                currentOid = vb.getOid();
            }

            System.out.println("SNMPv1 walked " + step + " steps for " + rootOid);
        } finally {
            snmp.close();
            transport.close();
        }

        return results.toArray(new SnmpRecord[0]);
    }


    public SnmpRecord[] walkV2Records(String deviceIp, String community, String rootOid, int port) throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(deviceIp + "/" + port));
        target.setRetries(2);
        target.setTimeout(3000);
        target.setVersion(SnmpConstants.version2c);

        Snmp snmp = new Snmp(transport);
        List<SnmpRecord> resultList = new ArrayList<>();

        try {
            TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));
            List<TreeEvent> events = treeUtils.getSubtree(target, new OID(rootOid));

            boolean fallback = (events == null || events.isEmpty());

            if (!fallback) {
                for (TreeEvent event : events) {
                    if (event == null || event.isError()) {
                        fallback = true;
                        break;
                    }
                    VariableBinding[] varBindings = event.getVariableBindings();
                    if (varBindings == null || varBindings.length == 0) continue;

                    for (VariableBinding vb : varBindings) {
                        resultList.add(toSnmpRecord(deviceIp, community, vb));
                    }
                }
            }

            if (fallback) {
                System.err.println("TreeUtils failed. Fallback to manual walk for OID: " + rootOid);
                resultList.addAll(Arrays.asList(walkManually(snmp, target, rootOid, deviceIp, community)));
            }

        } finally {
            snmp.close();
            transport.close();
        }

        return resultList.toArray(new SnmpRecord[0]);
    }

    public SnmpRecord[] walkV3Records(String deviceIp, String username, String authPass, String privPass,
                               String authProtocol, String privProtocol, int securityLevel,
                               String rootOid, int port) throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        Snmp snmp = new Snmp(transport);

        // USM + User
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);

        OID authProt = "MD5".equalsIgnoreCase(authProtocol) ? AuthMD5.ID : AuthSHA.ID;
        OID privProt = "DES".equalsIgnoreCase(privProtocol) ? PrivDES.ID : PrivAES128.ID;

        snmp.getUSM().addUser(new OctetString(username),
            new UsmUser(new OctetString(username),
                authProt, new OctetString(authPass),
                privProt, new OctetString(privPass))
        );

        UserTarget target = new UserTarget();
        target.setAddress(new UdpAddress(deviceIp + "/" + port));
        target.setRetries(2);
        target.setTimeout(3000);
        target.setVersion(SnmpConstants.version3);
        target.setSecurityLevel(securityLevel);
        target.setSecurityName(new OctetString(username));

        List<SnmpRecord> resultList = new ArrayList<>();

        try {
            TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));
            List<TreeEvent> events = treeUtils.getSubtree(target, new OID(rootOid));

            boolean fallback = (events == null || events.isEmpty());

            if (!fallback) {
                for (TreeEvent event : events) {
                    if (event == null || event.isError()) {
                        fallback = true;
                        break;
                    }
                    VariableBinding[] varBindings = event.getVariableBindings();
                    if (varBindings == null || varBindings.length == 0) continue;

                    for (VariableBinding vb : varBindings) {
                        SnmpRecord record = new SnmpRecord();
                        record.setDeviceIp(deviceIp);
                        record.setCommunity(username);
                        record.setOid(vb.getOid().toString());
                        record.setValue((vb.getVariable() == null || vb.getVariable().toString().isEmpty())
                                ? "null" : vb.getVariable().toString());
                        resultList.add(record);
                    }
                }
            }

            if (fallback) {
                System.err.println("SNMPv3 TreeUtils fallback for OID: " + rootOid);
                // bạn có thể gọi manualWalkV3 tương tự nếu muốn
            }

        } finally {
            snmp.close();
            transport.close();
        }

        return resultList.toArray(new SnmpRecord[0]);
    }



    private SnmpRecord[] walkManually(Snmp snmp, CommunityTarget target, String rootOid, String deviceIp, String community) throws IOException {
        List<SnmpRecord> results = new ArrayList<>();
        OID currentOid = new OID(rootOid);
        OID root = new OID(rootOid);

        int step = 0;
        int maxSteps = 50000; // avoid infinite loop

        OID lastOid = null;
        while (step++ < maxSteps) {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(currentOid));
            pdu.setType(PDU.GETNEXT);

            ResponseEvent responseEvent = snmp.send(pdu, target);

            if (responseEvent == null || responseEvent.getResponse() == null) {
                System.err.println("Timeout or no response at step " + step);
                break;
            }

            VariableBinding vb = responseEvent.getResponse().get(0);
            if (vb == null || vb.getOid() == null) {
                System.err.println("Invalid variable binding at step " + step);
                break;
            }

            // Dừng nếu OID không thay đổi (bị lặp)
            if (lastOid != null && vb.getOid().equals(lastOid)) {
                System.out.println("OID repeated at step " + step + ": " + vb.getOid());
                break;
            }
            lastOid = vb.getOid();

            if (!vb.getOid().startsWith(root)) {
                System.out.println("Reached outside of root OID at step " + step + ": " + vb.getOid());
                break;
            }

            // Skip error values (genError, noSuchName, ...)
            String varStr = vb.getVariable() == null ? "" : vb.getVariable().toString().toLowerCase();
            if (varStr.contains("error") || varStr.contains("nosuch")) {
                System.err.println("Skipped failed OID: " + vb.getOid());
                currentOid = vb.getOid().successor(); // move to next OID
                continue;
            }

            results.add(toSnmpRecord(deviceIp, community, vb));
            currentOid = vb.getOid();
        }

        System.out.println("Walked " + step + " steps from OID: " + rootOid);
        return results.toArray(new SnmpRecord[0]);
    }

    private SnmpRecord toSnmpRecord(String deviceIp, String community, VariableBinding vb) {
        SnmpRecord record = new SnmpRecord();
        record.setDeviceIp(deviceIp);
        record.setCommunity(community);
        record.setOid(vb.getOid().toString());
        record.setValue((vb.getVariable() == null || vb.getVariable().toString().isEmpty())
                ? "null" : vb.getVariable().toString());
        return record;
    }



    // public static void main(String[] args) {
    //     SnmpWalk snmpWalk = new SnmpWalk();
    //     try {
    //         SnmpRecord[] records = snmpWalk.walkAsRecords("127.0.0.1", "public", "1.3.6", 161);
    //         for (SnmpRecord snmpRecord : records) {
    //             System.out.println(snmpRecord.getOid() + " = " + snmpRecord.getValue());
    //         }
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
}
