package com.project.snmp.SnmpTrapListener;

import com.project.snmp.model.Trap;
import com.project.snmp.service.TrapService;

import org.snmp4j.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.PDUv1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SnmpTrapListener implements CommandResponder {

    @Autowired
    private TrapService trapService;

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
        if (pdu == null) return;

        int model = event.getMessageProcessingModel();
        String versionStr;

        List<VariableBinding> varBinds;
        String rawPdu;

        if (model == MessageProcessingModel.MPv1 && pdu instanceof PDUv1) {
            // SNMPv1 trap
            PDUv1 pduV1 = (PDUv1) pdu;
            versionStr = "v1";

            varBinds = new ArrayList<>(pduV1.getVariableBindings());

            rawPdu = String.format("SNMPv1 Trap: agentAddress=%s, genericTrap=%d, specificTrap=%d, timestamp=%d",
                    pduV1.getAgentAddress(), pduV1.getGenericTrap(), pduV1.getSpecificTrap(), pduV1.getTimestamp());

        } else if (model == MessageProcessingModel.MPv2c) {
            versionStr = "v2c";
            varBinds = new ArrayList<>(pdu.getVariableBindings());
            rawPdu = pdu.toString();

        } else if (model == MessageProcessingModel.MPv3) {
            versionStr = "v3";
            varBinds = new ArrayList<>(pdu.getVariableBindings());
            rawPdu = pdu.toString();

        } else {
            versionStr = "unknown";
            varBinds = new ArrayList<>(pdu.getVariableBindings());
            rawPdu = pdu.toString();
        }

        Trap trap = new Trap(rawPdu, varBinds, versionStr);

        trapService.processTrap(trap);

        System.out.println("Received SNMP Trap, version: " + versionStr + ", variables: " + varBinds.size());
    }

    @PreDestroy
    public void stop() throws IOException {
        if (snmp != null) {
            snmp.close();
            System.out.println("SNMP Trap Listener stopped.");
        }
    }
}
