package com.project.snmp.controller;

import com.project.snmp.model.Trap;
import com.project.snmp.service.TrapService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/traps")
public class TrapController {

    @Autowired
    private TrapService trapService;

    @GetMapping
    public List<Trap> getAllTraps() {
        return trapService.getAllTraps();
    }
}
