<template>
    <div class="search-oid-container">
      <h2 class="page-title">Search Value of OID</h2>
      <form @submit.prevent="searchOid" class="search-form">
        <div class="form-group">
          <label for="oid">OID:</label>
          <input v-model="oid" id="oid" placeholder="Enter OID (e.g., 1.3.6.1.2.1.1.1.0)" required />
        </div>
        <div class="form-group">
          <label for="baseIp">Base IP:</label>
          <input v-model="baseIp" id="baseIp" placeholder="e.g., 192.168.1.0" required />
        </div>
        <div class="form-group">
          <label for="prefix">Prefix:</label>
          <input v-model="prefix" id="prefix" placeholder="e.g., 24" required />
        </div>
        <div class="form-group">
          <label for="version">SNMP Version:</label>
          <select v-model="version" id="version">
            <option value="1">v1</option>
            <option value="2c">v2c</option>
            <option value="3">v3</option>
          </select>
        </div>
        <div v-if="version === '3'" class="snmp-v3-fields">
          <div class="form-group">
            <label for="authUsername">Auth Username:</label>
            <input v-model="authUsername" id="authUsername" placeholder="Enter username" />
          </div>
          <div class="form-group">
            <label for="authPass">Auth Password:</label>
            <input v-model="authPass" id="authPass" type="password" placeholder="Enter auth password" />
          </div>
          <div class="form-group">
            <label for="privPass">Privacy Password:</label>
            <input v-model="privPass" id="privPass" type="password" placeholder="Enter privacy password" />
          </div>
        </div>
        <button type="submit" class="search-btn">Search</button>
      </form>
  
      <!-- Bảng hiển thị kết quả -->
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
  import axios from 'axios';
  
  export default {
    data() {
      return {
        oid: '1.3.6.1.2.1.1.1',
        baseIp: '192.168.56.0',
        prefix: '24',
        version: '2c',
        community: 'public',
        port: 161,
        authUsername: '',
        authPass: '',
        privPass: '',
        devices: [],
        searched: false,
      };
    },
    methods: {
      async searchOid() {
        try {
          const params = {
            baseIp: this.baseIp,
            prefix: this.prefix,
            community: this.community,
            port: this.port,
            version: this.version,
            oid: this.oid,
          };
  
          if (this.version === '3') {
            params.authUsername = this.authUsername;
            params.authPass = this.authPass;
            params.privPass = this.privPass;
          }
  
          const response = await axios.post('api/device-scan/scan-subnet', null, { params });
          this.devices = response.data;
          this.searched = true;
        } catch (error) {
          console.error('Error searching OID:', error);
          this.devices = [];
          this.searched = true;
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
  
  .snmp-v3-fields {
    padding: 10px;
    background: rgba(227, 242, 253, 0.5);
    border-radius: 8px;
    margin-top: 10px;
  }
  
  .search-btn {
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
  
  .search-btn:hover {
    background: linear-gradient(135deg, #1e88e5, #43a047);
    transform: scale(1.05);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
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
  
  .no-results {
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
  }
  </style>