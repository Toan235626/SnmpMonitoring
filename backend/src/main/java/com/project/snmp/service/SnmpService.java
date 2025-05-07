package com.project.snmp.service;

// Nhập các thư viện cần thiết cho SNMP và Spring
import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Đánh dấu lớp này là một dịch vụ để Spring quản lý
@Service
public class SnmpService {

    private Snmp snmp; // Đối tượng SNMP để gửi và nhận dữ liệu
    private List<Device> devices = new ArrayList<>(); // Danh sách thiết bị quản lý

    // Phương thức khởi tạo, sẽ được gọi sau khi bean được khởi tạo
    @PostConstruct
    public void init() throws IOException {
        // Khởi tạo đối tượng SNMP với mapping UDP mặc định
        snmp = new Snmp(new DefaultUdpTransportMapping());
        snmp.listen(); // Bắt đầu lắng nghe các yêu cầu SNMP
    }

    // Phương thức để lấy dữ liệu từ một thiết bị SNMP
    public String getSnmpData(String ipAddress, String community, String oid) {
        // Tạo địa chỉ và target cho yêu cầu SNMP
        Address targetAddress = GenericAddress.parse("udp:" + ipAddress + "/161");
        CommunityTarget target = new CommunityTarget(targetAddress, new OctetString(community));
        target.setRetries(2); // Thiết lập số lần thử
        target.setTimeout(1500); // Thiết lập thời gian chờ
        target.setVersion(SnmpConstants.version2c); // Phiên bản SNMP

        // Tạo OID và PDU cho yêu cầu
        OID oidToGet = new OID(oid);
        VariableBinding vb = new VariableBinding(oidToGet);
        PDU pdu = new PDU();
        pdu.add(vb); // Thêm biến binding vào PDU
        pdu.setType(PDU.GET); // Đặt loại PDU là GET

        // Gửi yêu cầu SNMP và xử lý phản hồi
        try {
            ResponseEvent responseEvent = snmp.send(pdu, target);
            if (responseEvent.getResponse() != null) {
                VariableBinding responseVB = responseEvent.getResponse().get(0);
                return responseVB.toString(); // Trả về kết quả
            } else {
                return "No response from SNMP agent."; // Không có phản hồi
            }
        } catch (IOException e) {
            e.printStackTrace(); // In lỗi nếu có
            return "Error: " + e.getMessage(); // Trả về thông báo lỗi
        }
    }

    // Phương thức để lấy danh sách tất cả thiết bị
    public List<Device> getAllDevices() {
        return devices; // Trả về danh sách thiết bị
    }

    // Phương thức để tạo một thiết bị mới
    public Device createDevice(Device device) {
        devices.add(device); // Thêm thiết bị vào danh sách
        return device; // Trả về thiết bị vừa tạo
    }

    // Phương thức để cập nhật thông tin thiết bị
    public Device updateDevice(Long id, Device device) {
        Device existingDevice = devices.stream()
                .filter(d -> d.getId().equals(id)) // Tìm thiết bị theo ID
                .findFirst()
                .orElse(null);
        if (existingDevice != null) {
            // Cập nhật thông tin thiết bị
            existingDevice.setIp(device.getIp());
            existingDevice.setCommunity(device.getCommunity());
            existingDevice.setOid(device.getOid());
        }
        return existingDevice; // Trả về thiết bị đã được cập nhật
    }

    // Phương thức để xóa thiết bị theo ID
    public void deleteDevice(Long id) {
        devices.removeIf(d -> d.getId().equals(id)); // Xóa thiết bị nếu tìm thấy
    }

    // Phương thức sẽ được gọi trước khi bean bị hủy
    @PreDestroy
    public void close() throws IOException {
        // Đóng kết nối SNMP
        if (snmp != null) {
            snmp.close();
        }
    }
}