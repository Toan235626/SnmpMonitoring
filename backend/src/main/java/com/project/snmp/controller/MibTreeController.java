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
                                    @RequestParam("ipAddress") String ipAddress,
                                    @RequestParam("community") String community,
                                    @RequestParam(value = "port", required = false, defaultValue = "161") int port,
                                    @RequestParam(value = "version", required = false, defaultValue = "2c") String version
                                    ) throws Exception {
        JsonNode resultTree = mibTreeService.buildMergedTree(ipAddress, community, port, version);
        List<JsonNode> resultList = new ArrayList<JsonNode>();
        resultList.add(resultTree);
        return ResponseEntity.ok(resultList);
    }
}
