package com.project.snmp.utils;

public class VendorResolver {

    public static String resolve(String sysObjectId, String sysDescr) {
        if (sysObjectId == null)
            sysObjectId = "";
        if (sysDescr == null)
            sysDescr = "";

        sysObjectId = sysObjectId.trim();
        sysDescr = sysDescr.toLowerCase();

        // System.out.println("sysObjectID = " + sysObjectId);
        // System.out.println("sysDescr = " + sysDescr);

        if (sysObjectId.startsWith("1.3.6.1.4.1.9"))
            return "cisco";
        if (sysObjectId.startsWith("1.3.6.1.4.1.674"))
            return "dell";
        if (sysObjectId.startsWith("1.3.6.1.4.1.11"))
            return "hp";
        if (sysObjectId.startsWith("1.3.6.1.4.1.12356"))
            return "fortinet";
        if (sysObjectId.startsWith("1.3.6.1.4.1.123560"))
            return "fortinet";
        if (sysObjectId.startsWith("1.3.6.1.4.1.311"))
            return "microsoft";
        if (sysObjectId.startsWith("1.3.6.1.4.1.8072"))
            return "net-snmp";
        if (sysObjectId.startsWith("1.3.6.1.4.1.24681"))
            return "qnap";
        if (sysObjectId.startsWith("1.3.6.1.4.1.6876"))
            return "vmware";
        if (sysObjectId.startsWith("1.3.6.1.4.1.68760"))
            return "vmware";
        if (sysObjectId.startsWith("1.3.6.1.4.1.11863"))
            return "tplink";
        if (sysObjectId.startsWith("1.3.6.1.4.1.41112"))
            return "ubnt";
        if (sysObjectId.startsWith("1.3.6.1.4.1.2011"))
            return "huawei";
        if (sysObjectId.startsWith("1.3.6.1.4.1.14988"))
            return "mikrotik";

        if (sysDescr.contains("windows"))
            return "microsoft";
        if (sysDescr.contains("linux"))
            return "net-snmp";
        if (sysDescr.contains("ubuntu") || sysDescr.contains("ubuntu") || sysDescr.contains("debian"))
            return "net-snmp";
        if (sysDescr.contains("vmware"))
            return "vmware";
        if (sysDescr.contains("fortios"))
            return "fortinet";
        if (sysDescr.contains("hp") || sysDescr.contains("hewlett"))
            return "hp";
        if (sysDescr.contains("cisco"))
            return "cisco";
        if (sysDescr.contains("dell"))
            return "dell";
        if (sysDescr.contains("qnap"))
            return "qnap";
        if (sysDescr.contains("tplink") || sysDescr.contains("tp-link"))
            return "tplink";
        if (sysDescr.contains("ubiquiti") || sysDescr.contains("ubnt"))
            return "ubnt";
        if (sysDescr.contains("huawei"))
            return "huawei";
        if (sysDescr.contains("mikrotik"))
            return "mikrotik";

        return "unknown";
    }
}
