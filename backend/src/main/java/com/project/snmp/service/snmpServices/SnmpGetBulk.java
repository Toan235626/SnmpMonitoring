package com.project.snmp.service.snmpServices;

import com.project.snmp.model.SnmpRecord;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.stereotype.Component;

@Component
public class SnmpGetBulk {

    public SnmpRecord[] getBulkv2(String deviceIp, String community, String oid, int port) throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(deviceIp + "/" + port));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GETBULK);
        pdu.setMaxRepetitions(10);

        Snmp snmp = new Snmp(transport);
        ResponseEvent response = snmp.getBulk(pdu, target);

        if (response != null && response.getResponse() != null) {
            PDU responsePDU = response.getResponse();
            List<SnmpRecord> snmpRecordsList = new ArrayList<>();

            for (VariableBinding vb : responsePDU.getVariableBindings()) {
                if (vb.getVariable().toString() != null && vb.getVariable().toString() != ""
                        && !vb.getVariable().isException()) {
                    SnmpRecord snmpRecord = new SnmpRecord();
                    snmpRecord.setDeviceIp(deviceIp);
                    snmpRecord.setOid(vb.getOid().toString());
                    snmpRecord.setValue(vb.getVariable().toString());
                    snmpRecord.setCommunity(community);
                    snmpRecordsList.add(snmpRecord);
                }
            }
            return snmpRecordsList.toArray(new SnmpRecord[0]);
        } else {
            throw new RuntimeException("SNMP GETBULK request timed out or failed.");
        }
    }

    public SnmpRecord[] getBulkv3(String deviceIp, String username, String authPass, String privPass,
            String authProtocol, String privProtocol, int securityLevel,
            String rootOid, int port) throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        USM usm = new USM(SecurityProtocols.getInstance(),
                new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);

        OID authProto = null;
        if ("SHA".equalsIgnoreCase(authProtocol))
            authProto = AuthSHA.ID;
        else if ("MD5".equalsIgnoreCase(authProtocol))
            authProto = AuthMD5.ID;

        OID privProto = null;
        if ("AES".equalsIgnoreCase(privProtocol))
            privProto = PrivAES128.ID;
        else if ("DES".equalsIgnoreCase(privProtocol))
            privProto = PrivDES.ID;

        UsmUser user;
        if (securityLevel == SecurityLevel.AUTH_PRIV) {
            user = new UsmUser(new OctetString(username),
                    authProto, new OctetString(authPass),
                    privProto, new OctetString(privPass));
        } else if (securityLevel == SecurityLevel.AUTH_NOPRIV) {
            user = new UsmUser(new OctetString(username),
                    authProto, new OctetString(authPass),
                    null, null);
        } else if (securityLevel == SecurityLevel.NOAUTH_NOPRIV) {
            user = new UsmUser(new OctetString(username),
                    null, null, null, null);
        } else {
            throw new IllegalArgumentException("Invalid SNMPv3 security level: " + securityLevel);
        }
        Snmp snmp = new Snmp(transport);
        snmp.getUSM().addUser(new OctetString(username), user);

        UserTarget target = new UserTarget();
        target.setAddress(new UdpAddress(deviceIp + "/" + port));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version3);
        target.setSecurityLevel(securityLevel);
        target.setSecurityName(new OctetString(username));

        ScopedPDU pdu = new ScopedPDU();
        pdu.add(new VariableBinding(new OID(rootOid)));
        pdu.setType(PDU.GETBULK);
        pdu.setMaxRepetitions(10);

        ResponseEvent response = snmp.getBulk(pdu, target);

        if (response != null && response.getResponse() != null) {
            PDU responsePDU = response.getResponse();
            List<SnmpRecord> snmpRecordsList = new ArrayList<>();

            for (VariableBinding vb : responsePDU.getVariableBindings()) {
                if (vb.getVariable().toString() != null && vb.getVariable().toString() != ""
                        && !vb.getVariable().isException()) {
                    SnmpRecord snmpRecord = new SnmpRecord();
                    snmpRecord.setDeviceIp(deviceIp);
                    snmpRecord.setOid(vb.getOid().toString());
                    snmpRecord.setValue(vb.getVariable().toString());
                    snmpRecord.setCommunity(username);
                    snmpRecordsList.add(snmpRecord);
                }
            }
            return snmpRecordsList.toArray(new SnmpRecord[0]);
        } else {
            throw new RuntimeException("SNMP GETBULK request timed out or failed.");
        }
    }

}