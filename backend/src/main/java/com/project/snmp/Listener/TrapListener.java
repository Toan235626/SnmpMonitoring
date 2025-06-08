// package com.project.snmp.listener;

// import com.project.snmp.model.Trap;
// import com.project.snmp.service.NetworkScannerService;
// import com.project.snmp.service.TrapService;

// import org.snmp4j.*;
// import org.snmp4j.smi.*;
// import org.snmp4j.transport.DefaultUdpTransportMapping;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;

// import javax.annotation.PostConstruct;
// import javax.annotation.PreDestroy;
// import java.io.IOException;
// import java.util.List;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;

// @Component
// public class SnmpTrapListener {

// @Autowired
// private TrapService trapService;

// @Autowired
// private NetworkScannerService networkScannerService;

// private ExecutorService executor = Executors.newSingleThreadExecutor();

// private Snmp snmp;

// @PostConstruct
// public void init() throws IOException {
// TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping(new
// UdpAddress("0.0.0.0/162"));
// snmp = new Snmp(transport);

// snmp.addCommandResponder(event -> {
// PDU pdu = event.getPDU();
// if (pdu != null) {
// Trap trap = new Trap(pdu.toString(), pdu.getVariableBindings());

// // Gọi service xử lý trap
// trapService.processTrap(trap);

// // Chạy scanSubnet trong thread riêng để không chặn trap listener
// executor.submit(() -> {
// System.out.println("Starting subnet scan in separate thread...");
// networkScannerService.scanSubnet("192.168.1", "public", 161);
// System.out.println("Subnet scan complete.");
// });
// }
// });

// transport.listen();
// System.out.println("SNMP Trap Listener started...");
// }

// @PreDestroy
// public void shutdown() throws IOException {
// if (snmp != null) {
// snmp.close();
// }
// executor.shutdown();
// System.out.println("SNMP Trap Listener stopped.");
// }
// }
