package com.project.snmp.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.snmp.service.snmpServices.*;

@Service
public class SnmpMainService {

    @Autowired
    private SnmpGet snmpGet;

}
