package com.project.snmp.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class TreeMerger {

    public static JsonNode mergeTwoTrees(JsonNode standardList, JsonNode vendorListOrNode) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode mergedRoot = mapper.createObjectNode();
        ArrayNode mergedChildren = mapper.createArrayNode();

        mergedRoot.put("name", "DEVICE-MIB");
        mergedRoot.put("oid", "");

        // Thêm standard list
        if (standardList.isArray()) {
            mergedChildren.addAll((ArrayNode) standardList);
        }

        // Thêm vendor (dạng array hoặc object)
        if (vendorListOrNode.isArray()) {
            mergedChildren.addAll((ArrayNode) vendorListOrNode);
        } else if (vendorListOrNode.isObject() && vendorListOrNode.has("oid")) {
            mergedChildren.add(vendorListOrNode);
        }

        mergedRoot.set("children", mergedChildren);
        return mergedRoot;
    }




    // Gán giá trị value cho từng OID trong cây
    public static void mergeValues(JsonNode node, Map<String, String> oidValueMap) {
        if (node.has("oid")) {
            String oid = node.get("oid").asText();
            oid = oid + ".0";
            String value = oidValueMap.getOrDefault(oid, "");
            ((ObjectNode) node).put("value", value);
        }

        if (node.has("children") && node.get("children").isArray()) {
            for (JsonNode child : node.get("children")) {
                mergeValues(child, oidValueMap);
            }
        }
    }

    // Lọc bỏ các node không có value và không có con nào có value
    public static JsonNode filterByValue(JsonNode node) {
        boolean hasValue = node.has("value") && !node.get("value").asText().isEmpty();
        ArrayNode children = (ArrayNode) node.get("children");

        ArrayNode filteredChildren = ((ObjectNode) node).arrayNode();

        if (children != null) {
            for (JsonNode child : children) {
                JsonNode filteredChild = filterByValue(child);
                if (filteredChild != null) {
                    filteredChildren.add(filteredChild);
                }
            }
        }

        if (hasValue || filteredChildren.size() > 0) {
            ((ObjectNode) node).set("children", filteredChildren);
            return node;
        }

        return null;
    }

    public static JsonNode wrapArrayUnderEnterprise(ArrayNode array) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode wrapped = mapper.createObjectNode();

        wrapped.put("name", "enterprises");
        wrapped.put("oid", "1.3.6.1.4.1");
        wrapped.set("children", array);

        return wrapped;
    }


    public static JsonNode wrapUnderEnterprise(JsonNode vendorNode) {
        if (!vendorNode.has("oid")) return vendorNode;

        String oid = vendorNode.get("oid").asText();
        if (!oid.startsWith("1.3.6.1.4.1.")) return vendorNode;

        String[] parts = oid.split("\\.");
        if (parts.length < 8) return vendorNode; // không đủ sâu để cần gói

        String enterpriseOid = String.join(".", Arrays.copyOfRange(parts, 0, 7)); // → 1.3.6.1.4.1

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode wrapped = mapper.createObjectNode();
        wrapped.put("name", "enterprises");
        wrapped.put("oid", enterpriseOid);

        ArrayNode children = mapper.createArrayNode();
        children.add(vendorNode);
        wrapped.set("children", children);

        return wrapped;
    }

}
