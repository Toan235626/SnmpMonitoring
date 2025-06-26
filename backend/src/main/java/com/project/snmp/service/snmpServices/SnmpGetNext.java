package com.project.snmp.service.snmpServices;

import com.project.snmp.model.SnmpRecord;
import org.json.JSONObject;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Component;

@Component
public class SnmpGetNext {

    public SnmpRecord getNextv12(String deviceIp, String community, String oid, int port, String version)
            throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(deviceIp + "/" + port));
        target.setRetries(1);
        target.setTimeout(200);
        target.setVersion("1".equals(version) ? SnmpConstants.version1 : SnmpConstants.version2c);

        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GETNEXT);

        Snmp snmp = new Snmp(transport);
        ResponseEvent response = snmp.getNext(pdu, target);

        if (response != null && response.getResponse() != null) {
            String vbString = response.getResponse().getVariableBindings().get(0).getVariable().toString();
            if (vbString.startsWith("[") && vbString.endsWith("]")) {
                vbString = vbString.substring(1, vbString.length() - 1);
            }
            SnmpRecord snmpRecord = new SnmpRecord();
            snmpRecord.setDeviceIp(deviceIp);
            snmpRecord.setOid(response.getResponse().get(0).getOid().toString());
            snmpRecord.setValue(vbString);
            snmpRecord.setCommunity(community);
            return snmpRecord;
        } else {
            throw new RuntimeException("SNMP GETNEXT v1/v2c request timed out or failed.");
        }
    }

    public SnmpRecord getNextv3(String deviceIp, String username, String authPass, String privPass,
            String authProtocol, String privProtocol, int securityLevel,
            String oid, int port) throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        // System.out.println(username + " " + authPass + " " + privPass + " " +
        // authProtocol + " " + privProtocol + " "
        // + securityLevel);

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

        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);

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
            throw new IllegalArgumentException("Invalid security level: " + securityLevel);
        }
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
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GETNEXT);

        ResponseEvent response = snmp.getNext(pdu, target);

        if (response != null && response.getResponse() != null) {
            String vbString = response.getResponse().getVariableBindings().get(0).getVariable().toString();
            if (vbString.startsWith("[") && vbString.endsWith("]")) {
                vbString = vbString.substring(1, vbString.length() - 1);
            }
            // System.out.println(vbString);
            SnmpRecord snmpRecord = new SnmpRecord();
            snmpRecord.setDeviceIp(deviceIp);
            snmpRecord.setOid(response.getResponse().get(0).getOid().toString());
            snmpRecord.setValue(vbString);
            snmpRecord.setCommunity(username);
            return snmpRecord;
        } else {
            throw new RuntimeException("SNMP GETNEXT v3 request timed out or failed.");
        }
    }
}