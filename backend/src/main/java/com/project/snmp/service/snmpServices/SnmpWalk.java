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
import org.snmp4j.security.SecurityLevel;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Component
public class SnmpWalk {

    public SnmpRecord[] walkV1Records(String deviceIp, String community, String rootOid, int port) throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        Snmp snmp = new Snmp(transport);

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(deviceIp + "/" + port));
        target.setRetries(1);
        target.setTimeout(200);
        target.setVersion(SnmpConstants.version1);

        List<SnmpRecord> results = new ArrayList<>();

        try {
            OID root = new OID(rootOid);
            OID currentOid = root;
            Set<String> seenOids = new HashSet<>();
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
                if (vb == null || vb.getOid() == null)
                    break;

                if (lastOid != null && vb.getOid().equals(lastOid))
                    break;
                lastOid = vb.getOid();

                if (!vb.getOid().startsWith(root))
                    break;

                String val = vb.getVariable() == null ? "" : vb.getVariable().toString().toLowerCase();
                if (val.contains("nosuch") || val.contains("error") || val.isEmpty() || val.equals("null"))
                    continue;

                SnmpRecord record = new SnmpRecord();
                record.setDeviceIp(deviceIp);
                record.setCommunity(community);
                record.setOid(vb.getOid().toString());
                record.setValue(val.isEmpty() ? "null" : vb.getVariable().toString());
                results.add(record);

                currentOid = vb.getOid();
            }
            if ("1.3.6.1.4.1".startsWith(rootOid) && !seenOids.contains("1.3.6.1.4.1")) {
                System.out.println("→ Fallback walk from 1.3.6.1.4.1");

                OID currentOid1 = new OID("1.3.6.1.4.1");
                OID enterpriseRoot = new OID("1.3.6.1.4.1");
                int maxSteps1 = 10000;
                int step1 = 0;

                while (step1++ < maxSteps1) {
                    PDU pdu = new PDU();
                    pdu.setType(PDU.GETNEXT);
                    pdu.add(new VariableBinding(currentOid1));

                    ResponseEvent fallback = snmp.send(pdu, target);
                    if (fallback == null || fallback.getResponse() == null)
                        break;

                    VariableBinding vb = fallback.getResponse().get(0);
                    if (vb == null || vb.getOid() == null)
                        break;

                    OID oid = vb.getOid();
                    if (!oid.startsWith(enterpriseRoot))
                        break;

                    String oidStr = oid.toDottedString();
                    if (seenOids.contains(oidStr))
                        break;
                    seenOids.add(oidStr);
                    currentOid1 = oid;

                    String val = vb.getVariable() == null ? "" : vb.getVariable().toString().toLowerCase();
                    if (!val.contains("nosuch") && !val.contains("error") && !val.isEmpty() && !val.equals("null")) {
                        results.add(toSnmpRecord(deviceIp, community, vb));
                    }
                }
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
        target.setRetries(1);
        target.setTimeout(1000);
        target.setVersion(SnmpConstants.version2c);

        Snmp snmp = new Snmp(transport);
        List<SnmpRecord> results = new ArrayList<>();
        Set<String> seenOids = new HashSet<>();
        LinkedList<OID> queueGetNext = new LinkedList<>(); // fallback queue

        OID currentOid = new OID(rootOid);
        OID root = new OID(rootOid);
        int step = 0, maxSteps = 100000;

        try {
            while (step++ < maxSteps) {
                PDU pdu = new PDU();
                if (currentOid.startsWith(new OID("1.3.6.1.4.1"))) {
                    // dùng GETNEXT cho enterprise
                    pdu.setType(PDU.GETNEXT);
                    pdu.add(new VariableBinding(currentOid));
                    // System.out.println("GETNEXT → " + currentOid);
                } else {
                    // dùng GETBULK cho phần còn lại
                    pdu.setType(PDU.GETBULK);
                    pdu.setMaxRepetitions(40);
                    pdu.setNonRepeaters(0);
                    pdu.add(new VariableBinding(currentOid));
                    // System.out.println("GETBULK → " + currentOid);
                }

                ResponseEvent responseEvent = snmp.send(pdu, target);
                PDU response = responseEvent.getResponse();
                if (response == null || response.getVariableBindings().isEmpty())
                    break;

                boolean gotValid = false;

                for (VariableBinding vb : response.getVariableBindings()) {
                    if (vb == null || vb.getOid() == null)
                        continue;
                    OID oid = vb.getOid();

                    if (!oid.startsWith(root)) {
                        // System.out.println("→ Out of root: " + oid);
                        continue;
                    }

                    if (seenOids.contains(oid.toDottedString()))
                        continue;
                    seenOids.add(oid.toDottedString());
                    currentOid = oid;

                    String val = vb.getVariable() == null ? "" : vb.getVariable().toString().toLowerCase();
                    if (val.contains("nosuch") || val.contains("error") || val.isEmpty() || val.equals("null"))
                        continue;

                    gotValid = true;
                    results.add(toSnmpRecord(deviceIp, community, vb));

                    // Nếu là enterprise node → có thể còn .0 con → đưa vào GETNEXT fallback
                    if (oid.toDottedString().startsWith("1.3.6.1.4.1"))
                        queueGetNext.add(oid);
                }

                if (!gotValid)
                    break;
            }

            if ("1.3.6.1.4.1".startsWith(rootOid) && !seenOids.contains("1.3.6.1.4.1")) {
                System.out.println("→ Fallback walk from 1.3.6.1.4.1");

                OID currentOid1 = new OID("1.3.6.1.4.1");
                OID enterpriseRoot = new OID("1.3.6.1.4.1");
                int maxSteps1 = 10000;
                int step1 = 0;

                while (step1++ < maxSteps1) {
                    PDU pdu = new PDU();
                    pdu.setType(PDU.GETNEXT);
                    pdu.add(new VariableBinding(currentOid1));

                    ResponseEvent fallback = snmp.send(pdu, target);
                    if (fallback == null || fallback.getResponse() == null)
                        break;

                    VariableBinding vb = fallback.getResponse().get(0);
                    if (vb == null || vb.getOid() == null)
                        break;

                    OID oid = vb.getOid();
                    if (!oid.startsWith(enterpriseRoot))
                        break;

                    String oidStr = oid.toDottedString();
                    if (seenOids.contains(oidStr))
                        break;
                    seenOids.add(oidStr);
                    currentOid1 = oid;

                    String val = vb.getVariable() == null ? "" : vb.getVariable().toString().toLowerCase();
                    if (!val.contains("nosuch") && !val.contains("error") && !val.isEmpty() && !val.equals("null")) {
                        results.add(toSnmpRecord(deviceIp, community, vb));
                    }
                }
            }

        } finally {
            snmp.close();
            transport.close();
        }

        return results.toArray(new SnmpRecord[0]);
    }

    public SnmpRecord[] walkV3Records(String deviceIp, String username, String authPass, String privPass,
            String authProtocol, String privProtocol, int securityLevel,
            String rootOid, int port) throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        Snmp snmp = new Snmp(transport);
        OID authProt = "MD5".equalsIgnoreCase(authProtocol) ? AuthMD5.ID : AuthSHA.ID;
        OID privProt = "DES".equalsIgnoreCase(privProtocol) ? PrivDES.ID : PrivAES128.ID;

        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        UsmUser user;
        if (securityLevel == SecurityLevel.AUTH_PRIV) {
            user = new UsmUser(new OctetString(username),
                    authProt, new OctetString(authPass),
                    privProt, new OctetString(privPass));
        } else if (securityLevel == SecurityLevel.AUTH_NOPRIV) {
            user = new UsmUser(new OctetString(username),
                    authProt, new OctetString(authPass),
                    null, null);
        } else if (securityLevel == SecurityLevel.NOAUTH_NOPRIV) {
            user = new UsmUser(new OctetString(username),
                    null, null, null, null);
        } else {
            throw new IllegalArgumentException("Unsupported security level: " + securityLevel);
        }
        snmp.getUSM().addUser(new OctetString(username), user);

        UserTarget target = new UserTarget();
        target.setAddress(new UdpAddress(deviceIp + "/" + port));
        target.setRetries(1);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version3);
        target.setSecurityLevel(securityLevel);
        target.setSecurityName(new OctetString(username));

        List<SnmpRecord> results = new ArrayList<>();
        Set<String> seenOids = new HashSet<>();

        OID currentOid = new OID(rootOid);
        OID root = new OID(rootOid);

        try {
            int step = 0, maxSteps = 10000;
            while (step++ < maxSteps) {
                ScopedPDU pdu = new ScopedPDU();
                pdu.setType(PDU.GETBULK);
                pdu.setMaxRepetitions(20);
                pdu.setNonRepeaters(0);
                pdu.add(new VariableBinding(currentOid));

                ResponseEvent responseEvent = snmp.send(pdu, target);
                PDU response = responseEvent.getResponse();
                if (response == null || response.getVariableBindings().isEmpty())
                    break;

                boolean foundValid = false;

                for (VariableBinding vb : response.getVariableBindings()) {
                    if (vb == null || vb.getOid() == null)
                        continue;
                    OID oid = vb.getOid();

                    if (!oid.startsWith(root))
                        continue;
                    String oidStr = oid.toDottedString();
                    if (seenOids.contains(oidStr))
                        continue;

                    seenOids.add(oidStr);
                    currentOid = oid;

                    String val = vb.getVariable() == null ? "" : vb.getVariable().toString().toLowerCase();
                    if (val.contains("nosuch") || val.contains("error") || val.isEmpty() || val.equals("null"))
                        continue;

                    SnmpRecord rec = new SnmpRecord();
                    rec.setDeviceIp(deviceIp);
                    rec.setCommunity(username);
                    rec.setOid(oidStr);
                    rec.setValue(vb.getVariable().toString());
                    results.add(rec);

                    foundValid = true;
                }

                if (!foundValid)
                    break;
            }

            if ("1.3.6.1.4.1".startsWith(rootOid) && !seenOids.contains("1.3.6.1.4.1")) {
                System.out.println("→ Fallback walk from 1.3.6.1.4.1");

                OID currentOid1 = new OID("1.3.6.1.4.1");
                OID enterpriseRoot = new OID("1.3.6.1.4.1");
                int maxSteps1 = 5000;
                int step1 = 0;

                while (step1++ < maxSteps1) {
                    ScopedPDU pdu = new ScopedPDU();
                    pdu.setType(PDU.GETNEXT);
                    pdu.add(new VariableBinding(currentOid1));

                    ResponseEvent fallback = snmp.send(pdu, target);
                    if (fallback == null || fallback.getResponse() == null)
                        break;

                    VariableBinding vb = fallback.getResponse().get(0);
                    if (vb == null || vb.getOid() == null)
                        break;

                    OID oid = vb.getOid();
                    if (!oid.startsWith(enterpriseRoot))
                        break;

                    String oidStr = oid.toDottedString();
                    if (seenOids.contains(oidStr))
                        break;
                    seenOids.add(oidStr);
                    currentOid1 = oid;

                    String val = vb.getVariable() == null ? "" : vb.getVariable().toString().toLowerCase();
                    if (!val.contains("nosuch") && !val.contains("error") && !val.isEmpty() && !val.equals("null")) {
                        results.add(toSnmpRecord(deviceIp, username, vb));
                    }
                }
            }
            System.out.println("SNMPv3 smart walk done: " + results.size() + " OIDs");
        } finally {
            snmp.close();
            transport.close();
        }

        return results.toArray(new SnmpRecord[0]);
    }

    private SnmpRecord toSnmpRecord(String deviceIp, String community, VariableBinding vb) {
        SnmpRecord record = new SnmpRecord();
        record.setDeviceIp(deviceIp);
        record.setCommunity(community);
        record.setOid(vb.getOid().toString());
        record.setValue((vb.getVariable() == null || vb.getVariable().toString().isEmpty())
                ? "null"
                : vb.getVariable().toString());
        return record;
    }

}
