package com.project.snmp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String ipAddress;
    private String community;
    private String oid; // viết thường để tuân chuẩn Java
    private String value;

    public Device() {
    }

    public Device(String name, String ipAddress) {
        this.name = name;
        this.ipAddress = ipAddress;
    }

    public String getDeviceIp() {
        return ipAddress;
    }

    public void setDeviceIp(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // --- toString ---
    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", community='" + community + '\'' +
                ", oid='" + oid + '\'' +
                '}';
    }

    // --- equals() ---
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Device))
            return false;

        Device device = (Device) o;

        return id != null && id.equals(device.id);
    }
}
