package com.project.snmp.model;

public class SnmpRecord {
    private String deviceIp;
    private String oid;
    private String value;
    private String community;

    // Constructors
    public SnmpRecord() {}
    public SnmpRecord(String deviceIp, String oid, String value, String community) {
        this.deviceIp = deviceIp;
        this.oid = oid;
        this.value = value;
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
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return "SnmpRecord{" +
                "device ip='" + deviceIp + '\'' +
                ", oid='" + oid + '\'' +
                ", community='" + community + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
    public void setCommunity(String community) {
        this.community = community;
    }
    public String getCommunity() {
        return community;
    }
}
