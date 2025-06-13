<template>
  <div class="search-oid-container">
    <h2 class="page-title">Search Value of OID</h2>
    <form @submit.prevent="searchOid" class="search-form">
      <div class="form-group">
        <label for="oid">OID:</label>
        <input
          v-model="oid"
          id="oid"
          placeholder="Enter OID (e.g., 1.3.6.1.2.1.1.1.0)"
          required
        />
      </div>
      <div class="form-group">
        <label for="networks">Networks:</label>
        <select v-model="selectedNetwork" id="networks" required :disabled="!availableNetworks.length || loading">
          <option value="" disabled>Select a network</option>
          <option
            v-for="ipRange in availableNetworks"
            :key="ipRange"
            :value="ipRange"
          >
            {{ ipRange }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label for="version">SNMP Version:</label>
        <select v-model="version" id="version" :disabled="loading" @change="resetV3Fields">
          <option value="1">v1</option>
          <option value="2c">v2c</option>
          <option value="3">v3</option>
        </select>
      </div>
      <div v-if="version === '3'" class="snmp-v3-fields">
        <div class="form-group">
          <label for="securityLevel">Security Level:</label>
          <select v-model="securityLevel" id="securityLevel" :disabled="loading" @change="resetDependentFields">
            <option value="noAuthNoPriv">Level 1 (noAuthNoPriv)</option>
            <option value="authNoPriv">Level 2 (authNoPriv)</option>
            <option value="authPriv">Level 3 (authPriv)</option>
          </select>
        </div>
        <div class="form-group">
          <label for="authUsername">Auth Username:</label>
          <input
            v-model="authUsername"
            id="authUsername"
            placeholder="Enter username"
            :disabled="loading"
          />
        </div>
        <div v-if="securityLevel === 'authNoPriv' || securityLevel === 'authPriv'" class="form-group">
          <label for="authPass">Auth Password:</label>
          <input
            v-model="authPass"
            id="authPass"
            type="password"
            placeholder="Enter auth password"
            :disabled="loading"
          />
        </div>
        <div v-if="securityLevel === 'authNoPriv' || securityLevel === 'authPriv'" class="form-group">
          <label for="authProtocol">Authentication Protocol:</label>
          <select v-model="authProtocol" id="authProtocol" :disabled="loading">
            <option value="MD5">MD5</option>
            <option value="SHA">SHA</option>
          </select>
        </div>
        <div v-if="securityLevel === 'authPriv'" class="form-group">
          <label for="privPass">Privacy Password:</label>
          <input
            v-model="privPass"
            id="privPass"
            type="password"
            placeholder="Enter privacy password"
            :disabled="loading"
          />
        </div>
        <div v-if="securityLevel === 'authPriv'" class="form-group">
          <label for="privProtocol">Privacy Protocol:</label>
          <select v-model="privProtocol" id="privProtocol" :disabled="loading">
            <option value="DES">DES</option>
            <option value="AES">AES</option>
          </select>
        </div>
      </div>
      <div class="button-group">
        <button type="submit" class="search-btn" :disabled="!selectedNetwork || loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>Search</span>
        </button>
        <button type="button" @click="scanSubnets" class="refresh-btn" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>Refresh Networks</span>
        </button>
      </div>
    </form>

    <!-- Error message for subnet scan -->
    <p v-if="scanError" class="error-message">
      {{ scanError }}
    </p>

    <!-- Results table -->
    <table v-if="devices.length" class="result-table">
      <thead>
        <tr>
          <th>Device IP</th>
          <th>Device Name</th>
          <th>Value</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="device in devices" :key="device.deviceIp">
          <td>{{ device.deviceIp }}</td>
          <td>{{ device.name }}</td>
          <td>{{ device.value }}</td>
        </tr>
      </tbody>
    </table>
    <p v-else-if="searched && !devices.length" class="no-results">
      No devices found for the specified OID.
    </p>
  </div>
</template>

<script>
// import axios from "axios";
import axios from "@/axios.js";

export default {
  data() {
    return {
      oid: "1.3.6.1.2.1.1.1",
      selectedNetwork: "",
      version: "2c",
      community: "public",
      port: 161,
      securityLevel: "noAuthNoPriv",
      authUsername: "",
      authPass: "",
      authProtocol: "",
      privPass: "",
      privProtocol: "",
      devices: [],
      searched: false,
      availableNetworks: [],
      scanError: "",
      loading: false,
    };
  },
  async created() {
    await this.scanSubnets();
  },
  methods: {
    resetV3Fields() {
      if (this.version !== "3") {
        this.securityLevel = "noAuthNoPriv";
        this.authUsername = "";
        this.authPass = "";
        this.authProtocol = "MD5";
        this.privPass = "";
        this.privProtocol = "DES";
      }
    },
    resetDependentFields() {
      this.authPass = "";
      this.authProtocol = "MD5";
      this.privPass = "";
      this.privProtocol = "DES";
    },
    async scanSubnets() {
      try {
        this.loading = true;
        this.scanError = "";
        this.devices = []; 
        this.searched = false; 
        this.selectedNetwork = "";
        const response = await axios.post(import.meta.env.VITE_API_BASE_URL + "/device-scan/networks", null, {
          params: {
            community: this.community,
            port: this.port,
            version: this.version,
          },
        });
        this.availableNetworks = response.data.map((item) => `${item.baseIp}/${item.prefix}`);
        if (!this.availableNetworks.length) {
          this.scanError = "No networks found. Please try refreshing or check the server.";
        }
      } catch (error) {
        console.error("Error scanning subnets:", error);
        this.availableNetworks = [];
        this.scanError =
          error.response?.status === 405
            ? "Server rejected the request. Please check if the endpoint supports POST requests."
            : "Failed to scan networks. Please try again or check the server configuration.";
      } finally {
        this.loading = false;
      }
    },
    async searchOid() {
      try {
        this.loading = true;
        this.scanError = "";
        if (!this.selectedNetwork) {
          this.scanError = "Please select a network before searching.";
          return;
        }
        const [baseIp, prefix] = this.selectedNetwork.split("/");

        const params = {
          baseIp,
          prefix,
          community: this.community,
          port: this.port,
          version: this.version,
          oid: this.oid,
        };

        if (this.version === "3") {
          params.securityLevel = this.securityLevel;
          params.authUsername = this.authUsername;
          if (this.securityLevel === "authNoPriv" || this.securityLevel === "authPriv") {
            params.authPass = this.authPass;
            params.authProtocol = this.authProtocol;
          }
          if (this.securityLevel === "authPriv") {
            params.privPass = this.privPass;
            params.privProtocol = this.privProtocol;
          }
        }

        const response = await axios.post(import.meta.env.VITE_API_BASE_URL + "/device-scan/scan-subnet", null, {
          params,
        });
        this.devices = response.data;
        this.searched = true;
      } catch (error) {
        console.error("Error searching OID:", error);
        this.devices = [];
        this.searched = true;
        this.scanError =
          error.response?.status === 400
            ? "Invalid input. Please check the OID and network selection."
            : "Failed to search OID. Please check your input and try again.";
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.search-oid-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.2);
  animation: fadeIn 0.5s ease-in;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  text-align: center;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #1e88e5, #43a047);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.search-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-width: 400px;
  margin: 0 auto;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-group input,
.form-group select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #1e88e5;
  box-shadow: 0 0 8px rgba(30, 136, 229, 0.3);
}

.form-group select:disabled,
.form-group input:disabled {
  background: rgba(200, 200, 200, 0.5);
  cursor: not-allowed;
}

.snmp-v3-fields {
  padding: 10px;
  background: rgba(255, 255, 255, 0.174);
  border-radius: 8px;
  margin-top: 10px;
}

.button-group {
  display: flex;
  gap: 10px;
}

.search-btn,
.refresh-btn {
  position: relative;
  background: linear-gradient(135deg, #43a047, #1e88e5);
  color: #ffffff;
  border: none;
  border-radius: 8px;
  padding: 12px 20px;
  font-size: 16px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.search-btn:disabled,
.refresh-btn:disabled {
  background: rgba(200, 200, 200, 0.5);
  cursor: not-allowed;
}

.refresh-btn {
  background: linear-gradient(135deg, #1e88e5, #43a047);
}

.search-btn:hover:not(:disabled),
.refresh-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #1e88e5, #43a047);
  transform: scale(1.05);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
}

.refresh-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #1e88e5, #43a047);
}

.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #ffffff;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.result-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.result-table th {
  background: linear-gradient(135deg, #1e88e5, #43a047);
  color: #ffffff;
  padding: 15px 18px;
  text-align: left;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
}

.result-table td {
  padding: 15px 18px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  color: #2c3e50;
  transition: background 0.3s ease;
}

.result-table tr:hover {
  background: rgba(227, 242, 253, 0.9);
}

.no-results,
.error-message {
  margin-top: 20px;
  padding: 15px;
  background: rgba(255, 235, 238, 0.9);
  border-radius: 8px;
  color: #d32f2f;
  text-align: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 600px) {
  .search-oid-container {
    padding: 10px;
  }
  .search-form {
    max-width: 100%;
  }
  .result-table th,
  .result-table td {
    padding: 10px;
    font-size: 14px;
  }
  .button-group {
    flex-direction: column;
  }
}
</style>