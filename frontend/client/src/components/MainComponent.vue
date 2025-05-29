<template>
    <div class="main-wrapper">
      <button @click="showScanNetworkModal = true">Scan</button>
  
      <!-- Popup Scan Network -->
      <ScanNetworkModal
        :visible="showScanNetworkModal"
        @close="showScanNetworkModal = false"
        @networks-scanned="handleNetworksScanned"
        @use-network="openDeviceModal"
      />
  
      <!-- Popup Scan Device (nhập port + community) -->
      <ScanDeviceModal
        :visible="showScanDeviceModal"
        :selected-ip="selectedIP"
        @close="showScanDeviceModal = false"
        @scan-device="handleDeviceScan"
      />
  
      <!-- Bảng thiết bị -->
      <div class="devices-card">
        <h3>Discovered Devices</h3>
        <table>
          <thead>
            <tr>
              <th>IP</th>
              <th>Port</th>
              <th>Community</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="device in discoveredDevices" :key="device.ip">
              <td>{{ device.ip }}</td>
              <td>{{ device.port }}</td>
              <td>{{ device.community }}</td>
              <td>{{ device.status }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </template>
  
  <script>
  import ScanNetworkModal from './ScanNetworkModal.vue'
  import ScanDeviceModal from './ScanDeviceModal.vue'
  
  export default {
    components: { ScanNetworkModal, ScanDeviceModal },
    data() {
      return {
        showScanNetworkModal: false,
        showScanDeviceModal: false,
        selectedIP: null,
        discoveredDevices: []
      }
    },
    methods: {
      handleScannedNetworks(networks) {
        console.log("Networks scanned:", networks)
        // You can store networks if needed
      },
      openDeviceModal(ip) {
        this.selectedIP = ip
        this.showScanDeviceModal = true
      },
      handleDeviceScan({ ip, port, community }) {
        // Simulate API call
        const result = {
          ip,
          port,
          community,
          status: 'Success' // or 'Failed'
        }
        this.discoveredDevices.push(result)
        this.showScanDeviceModal = false
      }
    }
    
  }
  </script>
  
  <style scoped>
  .main-wrapper {
    padding: 20px;
  }
  button {
    padding: 8px 16px;
    font-size: 14px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
  }
  .devices-card {
    margin-top: 30px;
  }
  </style>
  