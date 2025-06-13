package com.project.snmp.service.snmpServices;

import com.project.snmp.model.SnmpRecord;
import com.project.snmp.service.SnmpMainService;

import org.json.JSONObject;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
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
public class SnmpGet {

    public SnmpRecord getAsRecordv12(String deviceIp, String community, String oid, int port, String version)
            throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(deviceIp + "/" + port));
        target.setRetries(1);
        target.setTimeout(200);
        if (version.equals("1")) {
            target.setVersion(SnmpConstants.version1);
        } else if (version.equals("2c")) {
            target.setVersion(SnmpConstants.version2c);
        } else {
            throw new IllegalArgumentException("Unsupported SNMP version: " + version);
        }

        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);

        Snmp snmp = new Snmp(transport);
        ResponseEvent response = snmp.get(pdu, target);

        if (response != null && response.getResponse() != null) {
            String vbString = response.getResponse().getVariableBindings().get(0).getVariable().toString();
            if (vbString.startsWith("[") && vbString.endsWith("]")) {
                vbString = vbString.substring(1, vbString.length() - 1);
            }

            SnmpRecord snmpRecord = new SnmpRecord();
            snmpRecord.setDeviceIp(deviceIp);
            snmpRecord.setOid(oid);
            snmpRecord.setCommunity(community);
            snmpRecord.setValue(vbString);
            return snmpRecord;
        } else {
            throw new RuntimeException("SNMP GET request timed out or failed.");
        }
    }

    public SnmpRecord getAsRecordv3(String deviceIp, String username, String authPass, String privPass,
            String authProtocol, String privProtocol, int securityLevel,
            String rootOid, int port) throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

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

        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);

        Snmp snmp = new Snmp(transport);
        snmp.getUSM().addUser(new OctetString(username), user);

        UserTarget target = new UserTarget();
        target.setAddress(new UdpAddress(deviceIp + "/" + port));
        target.setRetries(1);
        target.setTimeout(200);
        target.setVersion(SnmpConstants.version3);
        target.setSecurityLevel(securityLevel);
        target.setSecurityName(new OctetString(username));

        ScopedPDU pdu = new ScopedPDU();
        pdu.add(new VariableBinding(new OID(rootOid)));
        pdu.setType(PDU.GET);

        try {
            ResponseEvent response = snmp.get(pdu, target);

            if (response != null && response.getResponse() != null) {
                String vbString = response.getResponse().getVariableBindings().get(0).getVariable().toString();
                if (vbString.startsWith("[") && vbString.endsWith("]")) {
                    vbString = vbString.substring(1, vbString.length() - 1);
                }

                SnmpRecord snmpRecord = new SnmpRecord();
                snmpRecord.setDeviceIp(deviceIp);
                snmpRecord.setOid(response.getResponse().getVariableBindings().get(0).getOid().toString());
                snmpRecord.setValue(vbString);
                return snmpRecord;
            } else {
                throw new RuntimeException("SNMP GET request timed out or failed.");
            }
        } finally {
            snmp.close();
            transport.close();
        }
    }
}