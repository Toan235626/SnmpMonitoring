package com.project.snmp.SnmpTrapListener;

import com.project.snmp.model.Trap;
import com.project.snmp.service.TrapService;
import com.project.snmp.utils.OidDescription;

import org.snmp4j.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.mp.MessageProcessingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class SnmpTrapListener implements CommandResponder {

    @Autowired
    private TrapService trapService;
    @Autowired
    private OidDescription oidDescription;

    private Snmp snmp;

    @PostConstruct
    public void start() throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping(new UdpAddress("0.0.0.0/162"));
        snmp = new Snmp(transport);
        snmp.addCommandResponder(this);
        transport.listen();
        System.out.println("SNMP Trap Listener started on UDP port 162");
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        PDU pdu = event.getPDU();
        String senderIp = event.getPeerAddress().toString(); // → "udp:192.168.63.100/162"
        if (pdu == null)
            return;

        int model = event.getMessageProcessingModel();
        String versionStr;
        List<VariableBinding> varBinds;
        String rawPdu;

        System.out.println("Processing PDU...");
        System.out.println("Model: " + model);
        System.out.println("PDU: " + pdu.toString());

        if (model == MessageProcessingModel.MPv1 && pdu instanceof PDUv1) {
            PDUv1 pduV1 = (PDUv1) pdu;
            versionStr = "1";
            varBinds = new ArrayList<>(pduV1.getVariableBindings());
            rawPdu = String.format(
                    "SNMPv1 Trap: agentAddress = %s, genericTrap = %d, specificTrap = %d, timestamp = %d",
                    pduV1.getAgentAddress(), pduV1.getGenericTrap(), pduV1.getSpecificTrap(), pduV1.getTimestamp());

        } else if (model == MessageProcessingModel.MPv2c) {
            versionStr = "2c";
            varBinds = new ArrayList<>(pdu.getVariableBindings());
            System.out.println(varBinds);
            rawPdu = pdu.toString();

        } else if (model == MessageProcessingModel.MPv3) {
            versionStr = "3";
            varBinds = new ArrayList<>(pdu.getVariableBindings());
            rawPdu = pdu.toString();

        } else {
            versionStr = "unknown";
            varBinds = new ArrayList<>(pdu.getVariableBindings());
            rawPdu = pdu.toString();
        }

        List<HashMap<String, String>> listVarBinds = new ArrayList<>();
        System.out.println("Variable Bindings:");
        for (VariableBinding vb : varBinds) {
            HashMap<String, String> map = new HashMap<>();
            map.put("oid", vb.getOid().toString());
            map.put("value", vb.getVariable().toString());
            map.put("description", oidDescription.findDescription(vb.getOid().toString()));
            listVarBinds.add(map);
            System.out.println(" - " + vb.toString());
        }

        Trap trap = new Trap(rawPdu, listVarBinds, versionStr, senderIp.toString());
        trapService.processTrap(trap);

        System.out.println("Received SNMP Trap:");
        System.out.println(trap.toString());
    }

    @PreDestroy
    public void stop() throws IOException {
        if (snmp != null) {
            snmp.close();
            System.out.println("SNMP Trap Listener stopped.");
        }
    }
}