package com.project.snmp.repository;

import com.project.snmp.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    // You can add custom query methods here if needed
}
