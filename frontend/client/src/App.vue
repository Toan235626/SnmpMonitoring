<template>
  <div class="mib-browser-container">
    <!-- Toolbar -->
    <header class="toolbar">
      <div class="toolbar-left">
        <h1>SNMP MIB Browser</h1>
      </div>
      <div class="toolbar-right">
        <button @click="fetchSnmpData" :disabled="loading" title="SNMP Get">
          <i class="fas fa-play"></i> Get
        </button>
        <button @click="fetchSnmpGetNext" :disabled="loading" title="SNMP GetNext">
          <i class="fas fa-step-forward"></i> GetNext
        </button>
        <button @click="fetchSnmpGetBulk" :disabled="loading" title="SNMP GetBulk">
          <i class="fas fa-list"></i> GetBulk
        </button>
        <button @click="fetchSnmpWalk" :disabled="loading" title="SNMP Walk">
          <i class="fas fa-walking"></i> Walk
        </button>
        <button @click="clearForm" :disabled="loading" title="Clear Form">
          <i class="fas fa-eraser"></i> Clear
        </button>
        <button @click="showNetworkModal = true">Scan</button>
        <ScanNetworkModal
          :visible="showNetworkModal"
          @close="showNetworkModal = false"
          @networks-scanned="handleScannedNetworks"
        />

        <ScanDeviceModal
          :visible="showDeviceModal"
          :ip="selectedIP"
          @close="showDeviceModal = false"
          @scan-device="handleDeviceScan"
        />
      </div>     
    </header>
        <!-- Tabs for Multiple Queries -->
        <div class="tabs">
          <div
            v-for="(tab, index) in tabs"
            :key="index"
            :class="{ 'tab-active': activeTab === index }"
            @click="activeTab = index"
          >
            {{ tab.name }}
            <span @click.stop="closeTab(index)">×</span>
          </div>
          <button @click="addTab">+ New Tab</button>
        </div>
    <!-- Main Layout -->
    <div class="main-layout">
      <!-- MIB Tree Sidebar -->
      <aside class="mib-tree">
        <h3>MIB Tree</h3>
        <div class="tree-view">
          <ul>
            <MibNode
              v-for="node in mibTree"
              :key="node.oid"
              :node="node"
              :selected-oid="form.oid"
              @select-oid="selectOid"
            />
          </ul>
        </div>
      </aside>

      <!-- Main Content -->
      <main class="main-content">

        <!-- SNMP Form -->
        <div class="form-card">
          <form @submit.prevent="fetchSnmpData" class="form">
            <div class="form-group">
              <label for="deviceIp">Device IP:</label>
              <input
                type="text"
                id="deviceIp"
                v-model="form.deviceIp"
                placeholder="e.g., 192.168.1.20"
                required
              />
            </div>
            <div class="form-group">
              <label for="oid">OID:</label>
              <input
                type="text"
                id="oid"
                v-model="form.oid"
                placeholder="e.g., 1.3.6.1.2.1.1.3.0"
                required
              />
            </div>
            <div class="form-group">
              <label for="community">Community String:</label>
              <input
                type="text"
                id="community"
                v-model="form.community"
                placeholder="e.g., public"
                required
              />
            </div>
          </form>
        </div>
        <div class="devices-card" ref="resultSection" v-if="networkIPs.length">
          <h3>Discovered Network</h3>
          <table>
              <thead>
                <tr><th>IP</th><th> Actions </th></tr>
              </thead>
            <tbody>
                <tr v-for="ip in networkIPs" :key="ip">
                  <td>{{ ip }}</td>
                  <td><button @click="openDeviceScan(ip)">Use</button></td>
                </tr>
            </tbody>
          </table>
        </div>
        <div class="devices-card" v-if="discoveredDevices.length">
          <h3>Discovered Devices</h3>
          <table>
            <thead>
              <tr><th>Device IP</th><th>Name</th><th>Community</th></tr>
            </thead>
            <tbody>
              <tr v-for="device in discoveredDevices" :key="device.deviceIp">
                <td>{{ device.deviceIp }}</td>
                <td>{{ device.name}}</td>
                <td>{{ device.community }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- Result Table -->
        <div v-if="result && result.length > 0" class="result-card">
          <h3>Result</h3>
          <table>
            <thead>
              <tr>
                <th>OID</th>
                <th>Value</th>
                <th>IP</th>
                <th>Community</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in result" :key="index">
                <td>{{ item.oid }}</td>
                <td>{{ item.value }}</td>
                <td>{{ item.deviceIp }}</td>
                <td>{{ item.community }}</td>
              </tr>
            </tbody>
          </table>
          <button class="copy-btn" @click="copyResult">Copy to Clipboard</button>
        </div>
        <div v-if="error" class="error-card">
          <h3>Error</h3>
          <p>{{ error }}</p>
        </div>

        <!-- SNMP History -->
        <div class="history-card">
          <h3>SNMP History</h3>
          <button @click="clearHistory" v-if="history.length">Clear All History</button>
          <table v-if="history.length">
            <thead>
              <tr>
                <th>OID</th>
                <th>Value</th>
                <th>IP</th>
                <th>Community</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(entry, index) in history" :key="index">
                <td>{{ entry.oid }}</td>
                <td>{{ entry.value }}</td>
                <td>{{ entry.deviceIp }}</td>
                <td>{{ entry.community }}</td>
                <td><button @click="removeHistory(index)">Remove</button></td>
              </tr>
            </tbody>
          </table>
          <p v-if="!history.length">No history available.</p>
        </div>
      </main>
    </div>    
  </div>
</template>

<script>
import { useToast } from "vue-toastification";
import Clipboard from "clipboard";
import axios from "axios";
import { ref, onMounted } from 'vue';
import MibNode from './components/MibNode.vue'; // Import MibNode component
import ScanNetworkModal from './components/ScanNetworkModal.vue'; // Import ScanNetworkModal component
import ScanDeviceModal from './components/ScanDeviceModal.vue'; // Import ScanDeviceModal component
import mibData from "./assets/MIBTree.json"; // Import MIB data from JSON file

export default {
  name: "App",
  components: {
      MibNode,
      ScanNetworkModal,
      ScanDeviceModal,
    },
  data() {
    return {
      result: [],
      error: null,
      devices: [],
      showNetworkModal: false,
      showDeviceModal: false,
      selectedIP: '',
      discoveredDevices: [],  
      networkIPs: [],
    };
  },
  methods: {
    handleGetResponse(getResponse) {
      this.result.push(getResponse);
    },
    handleWalkResponse(walkResponse) {
      this.result = this.result.concat(walkResponse);
    },
    copyResult() {
      const text = JSON.stringify(this.result, null, 2);
      navigator.clipboard.writeText(text).then(() => {
        alert("Copied to clipboard!");
      });
    },
    openDeviceScan(ip) {
      this.selectedIP = ip
      this.showDeviceModal = true
    },
    handleDeviceScan({ ip, port, community }) {
      this.discoveredDevices.push({ ip, port, community })
      this.showDeviceModal = false
    }
  },
  setup() {
    const toast = useToast();
    // Form data
    const form = ref({
      deviceIp: "",
      oid: "",
      community: "",
    });

    // State for result, error, loading, devices, and history
    const result = ref(null);
    const error = ref(null);
    const loading = ref(false);
    const devices = ref([]);
    const history = ref([]);
    const networkIPs = ref([]);
    const discoveredDevices = ref([]); 
    // const showAddDeviceModal = ref(false);
    const newDevice = ref({ deviceIp: "", community: "" });
    const newNetworks = ref({ deviceIp: "", community: "" });
    const handleNetworksScanned = (newDevices) => {
      const uniqueDevices = newDevices.filter(
        d => !devices.value.some(existing => existing.deviceIp === d.deviceIp)
      );
      devices.value.push(...uniqueDevices);

      networkIPs.value = devices.value.map(d => d.deviceIp);

      toast.success("Networks scan completed!");
    };
    const handleScannedNetworks = (scannedDevices) => {
    networkIPs.value = scannedDevices.map(d => d.deviceIp);
    devices.value = scannedDevices;
    console.log("Scanned Devices:", scannedDevices);
    };

    const handleDeviceScan = (results) => {
      results.forEach(device => {
        if (!discoveredDevices.value.some(d => d.deviceIp === device.deviceIp)) {
            discoveredDevices.value.push(device);
        } 
      });
    };

    // Tabs management
    const tabs = ref([{ name: "Query 1" }]);
    const activeTab = ref(0);
    // MIB Tree data
    const processMibData = (data) => {
      return data.map(node => ({
        name: node.name,
        oid: node.oid,
        expanded: node.expanded || false, 
        children: node.children ? processMibData(node.children) : []
      }));
    };
    // Process MIB data to create a tree structure
    const oidForm = ref({
      oid: "",
    });

    const mibTree = ref([]);

    // Khởi tạo mibTree khi component được mount
    onMounted(() => {
      mibTree.value = processMibData(mibData);
    });
    // Function to select OID from MIB tree
    const selectOid = (oid) => {
      form.value.oid = oid;
    };

    // Function to toggle node expansion
    const toggleNode = (node) => {
      if (node.children) {
        node.expanded = !node.expanded;
      }
      selectOid(node.oid);
    };

    // Function to fetch SNMP data 
    const fetchSnmpData = async () => {
    loading.value = true;
    result.value = [];
    error.value = null;

    try {
      const response = await axios.post("api/snmp/get", null, {
        params: {
          deviceIp: form.value.deviceIp,
          oid: form.value.oid,
          community: form.value.community,
        },
      });
      result.value = response.data.map((item) => ({
        oid: item.oid,
        value: item.value,
        deviceIp: form.value.deviceIp,
        community: form.value.community,
      }));
      history.value.push(
        ...response.data.map((item) => ({
        oid: item.oid,
        value: item.value,
        deviceIp: form.value.deviceIp,
        community: form.value.community,
        timestamp: new Date().toLocaleString(),
        }))
      );
      } catch (err) {
        console.error(err);
        error.value = err.response?.data?.error || "An error occurred while fetching SNMP data.";
      } finally {
      loading.value = false;
      }
      };

    // Function to fetch SNMP GetBulk
    const fetchSnmpGetBulk = async () => {
      loading.value = true;
      result.value = [];
      error.value = null;

      try {
      const response = await axios.post("/api/snmp/bulk", null, {
        params: {
        deviceIp: form.value.deviceIp,
        oid: form.value.oid,
        community: form.value.community,
        },
      });

      result.value = response.data.map((item) => ({
        oid: item.oid,
        value: item.value,
        deviceIp: form.value.deviceIp,
        community: form.value.community,
      }));

      history.value.push(
        ...response.data.map((item) => ({
        oid: item.oid,
        value: item.value,
        deviceIp: form.value.deviceIp,
        community: form.value.community,
        timestamp: new Date().toLocaleString(),
        }))
      );
      } catch (err) {
      error.value = err.response?.data?.error || "An error occurred while performing SNMP GetBulk.";
      } finally {
      loading.value = false;
      }
    };

    // Function to fetch SNMP GetNext
const fetchSnmpGetNext = async () => {
  loading.value = true;
  error.value = null;

  try {
    // Determine the OID and deviceIp to use
    let oidToFetch;
    let deviceIpToFetch;

    if (result.value.length > 0) {
      // Use the last entry from result.value
      const lastResult = result.value[result.value.length - 1];
      oidToFetch = lastResult.oid;
      deviceIpToFetch = lastResult.deviceIp;
    } else {
      // Use form values if result is empty
      oidToFetch = form.value.oid;
      deviceIpToFetch = form.value.deviceIp;
    }

    // Perform the GetNext request
    const response = await axios.post("api/snmp/getnext", null, {
      params: {
        deviceIp: deviceIpToFetch,
        oid: oidToFetch,
        community: form.value.community,
      },
    });

    // Check if response.data is empty or invalid
    if (!response.data || response.data.length === 0) {
      error.value = "No more SNMP data available.";
      return;
    }

    // Map response data (expecting a single item)
    const newResult = response.data.map((item) => ({
      oid: item.oid,
      value: item.value,
      deviceIp: deviceIpToFetch,
      community: form.value.community,
    }));

    // Add to result (only one item expected)
    result.value.push(...newResult);

    // Add to history with timestamp
    history.value.push(
      ...newResult.map((item) => ({
        oid: item.oid,
        value: item.value,
        deviceIp: deviceIpToFetch,
        community: form.value.community,
        timestamp: new Date().toLocaleString(),
      }))
    );
  } catch (err) {
    console.error(err);
    error.value = err.response?.data?.error || "An error occurred while fetching SNMP data.";
  } finally {
    loading.value = false;
  }
};

    // Function to simulate SNMP Walk
    const fetchSnmpWalk = async () => {
      loading.value = true;
      result.value = [];
      error.value = null;

      try {
      const response = await axios.post("/api/snmp/walk", null, {
        params: {
        deviceIp: form.value.deviceIp,
        oid: form.value.oid,
        community: form.value.community,
        },
      });

      // Map the response data to match the result table structure
      result.value = response.data.map((item) => ({
        oid: item.oid,
        value: item.value,
        deviceIp: form.value.deviceIp,
        community: form.value.community,
      }));
      } catch (err) {
      error.value =
        err.response?.data?.error || "An error occurred while performing SNMP Walk.";
      } finally {
      loading.value = false;
      }
    };

    // Function to use device (fill form)
    const useDevice = (device) => {
      form.value.deviceIp = device.deviceIp;
      form.value.community = device.community;
      toast.info(`Selected networks: ${device.deviceIp}`);
    };

    // Function to remove device
    const removeDevice = (index) => {
      devices.value.splice(index, 1);
      toast.success("Networks removed!");
    };

    // Function to clear form
    const clearForm = () => {
      form.value.deviceIp = "";
      form.value.oid = "";
      form.value.community = "";
      result.value = null;
      error.value = null;
      discoveredDevices.value = [];
      networkIPs.value = [];
      toast.success("Form cleared!");
    };

    // Function to copy result to clipboard
    const copyResult = () => {
      const clipboard = new Clipboard(".copy-btn", {
        text: () => JSON.stringify(result.value, null, 2),
      });
      clipboard.on("success", () => {
        toast.success("Copied to clipboard!");
        clipboard.destroy();
      });
      clipboard.on("error", () => {
        toast.error("Failed to copy!");
        clipboard.destroy();
      });
    };

    // Tab management functions
    var a = 2
    const addTab = () => {
      tabs.value.push({ name: `Query ${a}` });
      a = a + 1;
      clearForm();
    };

    // History management
    const removeHistory = (index) => {
      history.value.splice(index, 1);
      toast.success("History entry removed!");
    };

    const clearHistory = () => {
      history.value = [];
      toast.success("History cleared!");
    };


    return {
      form,
      result,
      error,
      loading,
      devices,
      newDevice,
      newNetworks,
      tabs,
      activeTab,
      mibTree,
      history,
      fetchSnmpData,
      fetchSnmpGetBulk,
      fetchSnmpGetNext,
      fetchSnmpWalk,
      useDevice,
      removeDevice,
      clearForm,
      copyResult,
      selectOid,
      toggleNode,
      addTab,
      removeHistory,
      clearHistory,
      handleNetworksScanned,
      handleScannedNetworks,
      networkIPs,
      handleDeviceScan,
      discoveredDevices,
    };
  }
};
</script>

<style>
/* Reset default styles */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.mib-browser-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f5f5;
  font-family: "Arial", sans-serif;
}

/* Toolbar */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #e0e0e0;
  padding: 10px 20px;
  border-bottom: 1px solid #ccc;
}

.toolbar-left h1 {
  font-size: 18px;
  color: #333;
}

.toolbar-right {
  display: flex;
  gap: 10px;
}

.toolbar-right button {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  border: 1px solid #999;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 12px;
  color: #333;
}

.toolbar-right button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.toolbar-right button i {
  font-size: 14px;
}

/* Main Layout */
.main-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* MIB Tree Sidebar */
.mib-tree {
  width: 360px;
  background: #fff;
  border-right: 1px solid #ccc;
  padding: 10px;
  overflow-y: auto;
}

.mib-tree h3 {
  font-size: 14px;
  margin-bottom: 10px;
  color: #333;
}

.tree-view ul {
  list-style: none;
  color: #333;
}

.tree-view li {
  padding: 5px 10px;
  cursor: pointer;
  font-size: 12px;
  color: #333;
}

.tree-view li:hover {
  background: #e0e0e0;
}

.tree-view li > span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.tree-view li i {
  font-size: 12px;
}

.node-selected {
  font-weight: bold;
  color: #007bff;
}

.tree-view ul ul {
  margin-left: 20px;
}

/* Main Content */
.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* Tabs */
.tabs {
  display: flex;
  gap: 5px;
  margin-bottom: 0px;
}

.tabs div {
  padding: 8px 12px;
  background: #ddd;
  border: 1px solid #ccc;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
  cursor: pointer;
  font-size: 12px;
  color: #333;
}

.tab-active {
  background: #fff !important;
  font-weight: bold;
}

.tabs div span {
  margin-left: 5px;
  color: #999;
}

.tabs button {
  padding: 8px;
  border: 1px solid #ccc;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

/* Form Card */
.form-card {
  background: #fff;
  padding: 15px;
  border: 1px solid #ccc;
  border-radius: 4px;
  margin-bottom: 10px;
}

.form {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 10px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

label {
  font-size: 12px;
  margin-bottom: 5px;
  color: #333;
}

input {
  padding: 6px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 12px;
}

input:focus {
  border-color: #007bff;
  outline: none;
}

/* Devices Card */
.devices-card {
  background: #fff;
  padding: 15px;
  border: 1px solid #ccc;
  border-radius: 4px;
  margin-bottom: 10px;
}

.devices-card h3 {
  font-size: 14px;
  margin-bottom: 10px;
  color: #333;
}

.devices-card table {
  width: 100%;
  border-collapse: collapse;
}

.devices-card th,
.devices-card td {
  padding: 8px;
  border: 1px solid #ddd;
  font-size: 12px;
  text-align: left;
}

.devices-card th {
  background: #e0e0e0;
  font-weight: bold;
}

.devices-card button {
  padding: 6px 12px;
  border: 1px solid #999;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 5px;
}

.devices-card button:first-child {
  background: #007bff;
  color: #fff;
}

.devices-card button:last-child {
  background: #dc3545;
  color: #fff;
}

/* Result Card */
.result-card,
.error-card,
.history-card {
  background: #fff;
  padding: 15px;
  border: 1px solid #ccc;
  border-radius: 4px;
  margin-bottom: 10px;
}

.result-card h3,
.error-card h3,
.history-card h3 {
  font-size: 14px;
  margin-bottom: 10px;
  color: #333;
}

.result-card table,
.error-card table,
.history-card table {
  width: 100%;
  border-collapse: collapse;
}

.result-card th,
.result-card td,
.error-card th,
.error-card td,
.history-card th,
.history-card td {
  padding: 8px;
  border: 1px solid #ddd;
  font-size: 12px;
  text-align: left;
}

.result-card th,
.error-card th,
.history-card th {
  background: #e0e0e0;
  font-weight: bold;
}

.copy-btn {
  margin-top: 10px;
  padding: 6px 12px;
  border: 1px solid #999;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.error-card {
  background: #f8d7da;
  border-color: #f5c6cb;
}

.error-card p {
  font-size: 12px;
  color: #721c24;
}

.history-card button {
  padding: 6px 12px;
  border: 1px solid #999;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-top: 10px;
}

.history-card button:hover {
  background: #dc3545;
  color: #fff;
}

/* Modal */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background: #fff;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 0; /* Góc vuông */
  max-width: 400px;
  width: 100%;
}

.modal-content h3 {
  font-size: 14px;
  margin-bottom: 15px;
  color: #333;
}

.modal-content .form-group {
  margin-bottom: 15px;
}

.modal-content input {
  padding: 6px;
  border: 1px solid #ccc;
  border-radius: 0; /* Góc vuông */
  font-size: 12px;
  width: 100%;
}

.modal-content input:focus {
  border-color: #007bff;
  outline: none;
}

.modal-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.modal-buttons button {
  padding: 6px 12px;
  border: 1px solid #999;
  border-radius: 0; /* Góc vuông */
  cursor: pointer;
  font-size: 12px;
}

.modal-buttons button:first-child {
  background: #007bff;
  color: #fff;
}

.modal-buttons button:last-child {
  background: #6c757d;
  color: #fff;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .main-layout {
    flex-direction: column;
  }

  .mib-tree {
    width: 100%;
    max-height: 200px;
  }

  .form {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    gap: 10px;
  }

  .toolbar-right {
    flex-wrap: wrap;
  }

  .tabs {
    flex-wrap: wrap;
  }
}
</style>