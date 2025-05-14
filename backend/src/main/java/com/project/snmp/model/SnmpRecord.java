package com.project.snmp.model;

public class SnmpRecord {
    private String device;
    private String oid;
    private String value;

    // Constructors
    public SnmpRecord() {}
    public SnmpRecord(String device, String oid, String value) {
        this.device = device;
        this.oid = oid;
        this.value = value;
    }

    // Getters and Setters
    public String getDevice() {
        return device;
    }
    public void setDevice(String device) {
        this.device = device;
    }
    public String getOid() {
        return oid;
    }
    public void setOid(String oid) {
        this.oid = oid;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return "SnmpRecord{" +
                "device ip='" + device + '\'' +
                ", oid='" + oid + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
