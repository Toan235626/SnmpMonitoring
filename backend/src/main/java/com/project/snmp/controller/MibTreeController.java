package com.project.snmp.controller;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.project.snmp.service.MibTreeService;

@RestController
public class MibTreeController {

    @Autowired
    private MibTreeService mibTreeService;

    @PostMapping("/mib-tree")
    public ResponseEntity<List<JsonNode>> getMibTree(
                                    @RequestParam("deviceIp") String ipAddress,
                                    @RequestParam(value = "community", required = false, defaultValue = "public") String community,
                                    @RequestParam(value = "port", required = false, defaultValue = "161") int port,
                                    @RequestParam(value = "version", required = false, defaultValue = "2c") String version,
                                    @RequestParam(value = "authUsername", required = false) String authUsername,
                                    @RequestParam(value = "authPass", required = false) String authPass,
                                    @RequestParam(value = "privPass", required = false) String privPass,
                                    @RequestParam(value = "authProtocol", required = false) String authProtocol,
                                    @RequestParam(value = "privProtocol", required = false) String privProtocol,
                                    @RequestParam(value = "securityLevel", required = false, defaultValue = "0") String securityLevel
                                    ) throws Exception {
        if (version.equals("3") && (authUsername == null || authPass == null || privPass == null || authProtocol == null || privProtocol == null)) {
            throw new IllegalArgumentException("SNMPv3 requires all authentication parameters to be provided.");
        }
        JsonNode resultTree = mibTreeService.buildMergedTree(ipAddress, community, port, version);
        List<JsonNode> resultList = new ArrayList<JsonNode>();
        resultList.add(resultTree);
        return ResponseEntity.ok(resultList);
    }
}
