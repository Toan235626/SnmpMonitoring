package com.project.snmp.model;

public class SnmpRecord {
    private String deviceIp;
    private String oid;
    private String oid_value;
    private String community;
    private String description;

    // Constructors
    public SnmpRecord() {
    }

    public SnmpRecord(String deviceIp, String oid, String value, String community) {
        this.deviceIp = deviceIp;
        this.oid = oid;
        this.oid_value = value;
        this.community = community;
    }

    // Getters and Setters
    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getValue() {
        return oid_value;
    }

    public void setValue(String value) {
        this.oid_value = value;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "SnmpRecord{" +
                "device ip='" + deviceIp + '\'' +
                ", oid='" + oid + '\'' +
                ", community='" + community + '\'' +
                ", value='" + oid_value + '\'' +
                '}';
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getCommunity() {
        return community;
    }
}
