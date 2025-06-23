package com.project.snmp.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Component
public class OidDescription {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<JsonNode> mibTrees = new ArrayList<>();

    @PostConstruct
    public void loadMibTrees() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:/mibs/**/*.json");

        for (Resource resource : resources) {
            JsonNode root = objectMapper.readTree(resource.getInputStream());

            if (root.isArray()) {
                for (JsonNode node : root) {
                    mibTrees.add(node);
                }
            } else if (root.isObject()) {
                mibTrees.add(root);
            } else {
                System.err.println("Invalid MIB format in: " + resource.getFilename());
            }
        }
    }

    public String findDescription(String oid) {
        String cleanOid = oid.endsWith(".0") ? oid.substring(0, oid.length() - 2) : oid;
        for (JsonNode root : mibTrees) {
            String desc = findRecursive(root, cleanOid);
            if (desc != null)
                return desc;
        }
        return "No description found";
    }

    private String findRecursive(JsonNode node, String targetOid) {
        if (node.has("oid") && targetOid.equals(node.get("oid").asText())) {
            return node.has("description") ? node.get("description").asText() : null;
        }

        if (node.has("children")) {
            for (JsonNode child : node.get("children")) {
                String result = findRecursive(child, targetOid);
                if (result != null)
                    return result;
            }
        }
        return null;
    }
}
