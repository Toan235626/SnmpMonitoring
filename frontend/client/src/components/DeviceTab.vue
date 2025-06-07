<template>
  <div class="device-tab">
    <div class="search-bar">
      <v-text-field
        v-model="oid"
        label="OID"
        variant="outlined"
        class="search-input"
      />
      <v-text-field
        v-model="community"
        label="Community String"
        variant="outlined"
        class="search-input"
      />
      <v-text-field
        v-model="port"
        label="Port"
        type="number"
        variant="outlined"
        class="search-input"
      />
      <v-select
        v-model="version"
        label="SNMP Version"
        :items="['1', '2c', '3']"
        variant="outlined"
        class="search-input"
        @update:modelValue="onVersionChange"
      />
      <v-text-field
        v-if="version === '3'"
        v-model="authUsername"
        label="Auth Username"
        variant="outlined"
        class="search-input"
      />
      <v-text-field
        v-if="version === '3'"
        v-model="authPass"
        label="Auth Password"
        type="password"
        variant="outlined"
        class="search-input"
      />
      <v-text-field
        v-if="version === '3'"
        v-model="privPass"
        label="Privacy Password"
        type="password"
        variant="outlined"
        class="search-input"
      />
      <v-select
        v-if="version === '3'"
        v-model="authProtocol"
        label="Auth Protocol"
        :items="['MD5', 'SHA']"
        variant="outlined"
        class="search-input"
      />
      <v-select
        v-if="version === '3'"
        v-model="privProtocol"
        label="Privacy Protocol"
        :items="['DES', 'AES']"
        variant="outlined"
        class="search-input"
      />
      <v-text-field
        v-if="version === '3'"
        v-model="securityLevel"
        label="Security Level"
        type="number"
        variant="outlined"
        class="search-input"
      />
    </div>

    <div class="actions">
      <v-btn color="primary" @click="performAction('get')">Get</v-btn>
      <v-btn color="primary" @click="performAction('getNext')">GetNext</v-btn>
      <v-btn color="primary" @click="performAction('getBulk')">GetBulk</v-btn>
      <v-btn color="primary" @click="performAction('walk')">Walk</v-btn>
      <v-btn color="grey" @click="clearResults" class="clear-btn">Clear Results</v-btn>
    </div>

    <v-progress-circular
      v-if="isLoading"
      indeterminate
      color="primary"
      class="loading"
    />

    <div class="table-container">
      <h3 class="table-title">SNMP Results</h3>
      <v-table v-if="results.length" class="custom-table">
        <thead>
          <tr>
            <th>OID</th>
            <th>Value</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="result in results" :key="result.oid">
            <td class="text-cell">{{ result.oid }}</td>
            <td class="text-cell">{{ result.value }}</td>
          </tr>
        </tbody>
      </v-table>
    </div>

    <div class="table-container">
      <div class="table-header">
        <h3 class="table-title">History</h3>
        <v-btn color="grey" @click="clearHistory" class="clear-btn">Clear History</v-btn>
      </div>
      <v-table v-if="deviceHistory.length" class="custom-table">
        <thead>
          <tr>
            <th>Time</th>
            <th>OID</th>
            <th>Community</th>
            <th>Value</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="history in deviceHistory" :key="history.id">
            <td class="text-cell">{{ history.time }}</td>
            <td class="text-cell">{{ history.oid }}</td>
            <td class="text-cell">{{ history.community }}</td>
            <td class="text-cell">{{ history.value }}</td>
          </tr>
        </tbody>
      </v-table>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { deviceStore } from '@/stores/device';

export default {
  props: {
    deviceId: {
      type: String,
      required: true,
    },
    selectedOid: {
      type: String,
      default: '',
    },
  },
  setup(props) {
    const store = deviceStore();
    const oid = ref(props.selectedOid || '');
    const community = ref('public');
    const port = ref(161);
    const version = ref('2c');
    const authUsername = ref('');
    const authPass = ref('');
    const privPass = ref('');
    const authProtocol = ref('');
    const privProtocol = ref('');
    const securityLevel = ref(0);

    const onVersionChange = (newVersion) => {
      if (newVersion !== '3') {
        authUsername.value = '';
        authPass.value = '';
        privPass.value = '';
        authProtocol.value = '';
        privProtocol.value = '';
        securityLevel.value = 0;
      }
    };

    const deviceHistory = computed(() =>
      store.searchHistory.filter(
        (history) => history.deviceId === props.deviceId
      )
    );

    const activeTab = computed(() => store.activeTab);
    const results = computed(() => store.results[props.deviceId] || []);
    const error = computed(() => store.error);
    const clearResults = () => {
      store.results[props.deviceId] = [];
    };
    const clearHistory = () => {
      store.searchHistory = store.searchHistory.filter(
        (history) => history.deviceId !== props.deviceId
      )};
    return {
      oid,
      community,
      port,
      version,
      authUsername,
      authPass,
      privPass,
      authProtocol,
      privProtocol,
      securityLevel,
      onVersionChange,
      results,
      deviceHistory,
      activeTab,
      error,
      clearHistory,
      clearResults,
      isLoading: computed(() => store.isLoading),
      performAction: async (action) => {
        await store.performAction(action, props.deviceId, {
          oid: oid.value,
          community: community.value,
          port: Number(port.value),
          version: version.value,
          authUsername: authUsername.value,
          authPass: authPass.value,
          privPass: privPass.value,
          authProtocol: authProtocol.value,
          privProtocol: privProtocol.value,
          securityLevel: String(securityLevel.value),
        });
      },
    };
  },
};
</script>

<style scoped>
.device-tab {
  padding: 30px;
  background: linear-gradient(145deg, #6f6f75, #16213e); /* Dark, cosmic gradient for premium feel */
  min-height: 100vh; /* Full viewport height for immersion */
  border-radius: 12px; /* Smooth, modern corners */
  position: relative;
  overflow: hidden; /* Contain pseudo-elements */
  font-family: 'Poppins', 'Segoe UI', sans-serif; /* Elegant, modern font */
}
.device-tab::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.3;
  pointer-events: none; /* Allow interaction through overlay */
}
.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 15px; /* Wider gap for breathing room */
  margin-bottom: 30px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.959); /* Subtle transparent white */
  border-radius: 10px;
  backdrop-filter: blur(5px); /* Glassmorphism effect */
}
.search-input {
  max-width: 320px;
  max-height: 58px; /* Limit height for better layout */
  background: rgba(255, 255, 255, 0.1); /* Semi-transparent input background */
  border-radius: 10px;
  transition: all 0.3s ease; /* Smooth transitions */
}
.search-input:hover, .search-input:focus-within {
  transform: scale(1.02); /* Slight scale on hover/focus */
  box-shadow: 0 0 6px rgba(0, 184, 212, 0.5); /* Cyan glow */
}
.actions {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  justify-content: center; /* Center buttons for premium layout */
}
.actions .v-btn {
  background: linear-gradient(135deg, #00b8d4, #007bff); /* Vibrant gradient */
  color: #fff;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}
.actions .v-btn:hover {
  background: linear-gradient(135deg, #007bff, #00b8d4); /* Reverse gradient on hover */
  transform: scale(1.05); /* Slight scale for interaction */
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
}
.clear-btn {
  background: linear-gradient(135deg, #ff5252, #b71c1c); /* Red gradient for clear button */
  margin-right: 10px;
}
.clear-btn:hover {
  background: linear-gradient(135deg, #b71c1c, #ff5252);
}
.loading {
  display: block;
  margin: 30px auto;
  color: #00b8d4; /* Cyan for loading spinner */
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite; /* Smooth spin animation */
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.error {
  color: #ff5252; /* Vivid red */
  margin-top: 15px;
  padding: 10px;
  background: rgba(255, 82, 82, 0.1); /* Subtle red background */
  border-radius: 6px;
  border: 1px solid #ff5252;
  text-align: center;
  font-weight: 500;
  animation: fadeIn 0.5s ease-in; /* Fade-in effect */
}
.table-container {
  margin-bottom: 40px;
  overflow-x: auto;
  padding: 15px;
  background: rgba(255, 255, 255, 0.05); /* Semi-transparent white */
  border-radius: 10px;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(5px); /* Glassmorphism effect */
}
.table-title {
  font-size: 1.8rem;
  font-weight: 600;
  color: #fff; /* White for contrast */
  margin-bottom: 15px;
  padding-left: 15px;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3); /* Subtle shadow for text */
}
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.custom-table {
  width: 100%;
  border-collapse: collapse;
  background-color: rgba(255, 255, 255, 0.9); /* Slightly transparent white */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  overflow: hidden; /* Round corners for table */
}
.custom-table th {
  background: linear-gradient(135deg, #007bff, #00b8d4); /* Gradient header */
  color: #fff;
  padding: 15px 18px;
  text-align: left;
  font-weight: 600;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
  text-transform: uppercase;
  letter-spacing: 1px;
}
.custom-table td {
  padding: 15px 18px;
  color: #2c3e50; /* Dark text for contrast */
  transition: background 0.3s ease;
}
.custom-table tr:nth-child(even) {
  background-color: rgba(245, 245, 245, 0.8); /* Subtle even row color */
}
.custom-table tr:hover {
  background-color: rgba(227, 242, 253, 0.9); /* Light hover effect */
  transform: scale(1.01); /* Slight scale on hover */
}
.text-cell {
  word-break: break-word;
  max-width: 320px; /* Slightly wider for balance */
  white-space: normal;
  color: #ececec; /* Softer dark color */
  padding: 12px 18px;
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
</style>