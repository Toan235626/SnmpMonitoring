package com.project.snmp.service;

import com.project.snmp.model.Trap;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TrapService {

    private final List<Trap> trapList = Collections.synchronizedList(new ArrayList<>());

    public void processTrap(Trap trap) {
        if (trap == null) {
            System.out.println("Received null trap, skipping.");
            return;
        }

        System.out.println("Processing trap:");
        System.out.println(trap.toString());
        
        trapList.add(trap);
        System.out.println("Trap added to list. Total traps: " + trapList.size());
        
        // Mở rộng lưu DB hoặc xử lý nghiệp vụ khác nếu cần
    }

    public List<Trap> getAllTraps() {
        List<Trap> trapsCopy = new ArrayList<>(trapList);
        System.out.println("Retrieving all traps. Total traps: " + trapsCopy.size());
        return trapsCopy;
    }
}