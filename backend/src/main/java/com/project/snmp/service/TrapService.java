package com.project.snmp.service;

import com.project.snmp.model.Trap;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TrapService {

    // Lưu trữ trap nhận được trong bộ nhớ (demo)
    private final List<Trap> trapList = Collections.synchronizedList(new ArrayList<>());

    public void processTrap(Trap trap) {
        System.out.println("Processing trap:");
        System.out.println(trap.toString());
        trapList.add(trap);
        // TODO: Có thể lưu DB hoặc xử lý nghiệp vụ khác
    }

    public List<Trap> getAllTraps() {
        return new ArrayList<>(trapList);
    }
}
