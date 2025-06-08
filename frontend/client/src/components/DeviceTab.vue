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
      <v-btn color="grey" @click="clearResults" class="clear-btn"
        >Clear Results</v-btn
      >
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
        <v-btn color="grey" @click="clearHistory" class="clear-btn"
          >Clear History</v-btn
        >
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
import { ref, computed } from "vue";
import { deviceStore } from "@/stores/device";

export default {
  props: {
    deviceId: {
      type: String,
      required: true,
    },
    selectedOid: {
      type: String,
      default: "",
    },
  },
  setup(props) {
    const store = deviceStore();
    const device = store.devices.find((d) => d.id === props.deviceId);

    const oid = ref(props.selectedOid || "");
    const community = ref(device?.community || "public");
    const port = ref(device?.port || 161);
    const version = ref(device?.version || "2c");
    const authUsername = ref("");
    const authPass = ref("");
    const privPass = ref("");
    const authProtocol = ref("");
    const privProtocol = ref("");
    const securityLevel = ref(0);

    const onVersionChange = (newVersion) => {
      if (newVersion !== "3") {
        authUsername.value = "";
        authPass.value = "";
        privPass.value = "";
        authProtocol.value = "";
        privProtocol.value = "";
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
      );
    };
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
  background: linear-gradient(
    145deg,
    #6f6f75,
    #16213e
  ); 
  min-height: 100vh; 
  border-radius: 12px; 
  position: relative;
  overflow: hidden; 
  font-family: "Poppins", "Segoe UI", sans-serif; 
}
.device-tab::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.3;
  pointer-events: none; 
}
.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 15px; 
  margin-bottom: 30px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.959); 
  border-radius: 10px;
  backdrop-filter: blur(5px); 
}
.search-input {
  max-width: 320px;
  max-height: 58px; 
  background: rgba(255, 255, 255, 0.1); 
  border-radius: 10px;
  transition: all 0.3s ease; 
}
.search-input:hover,
.search-input:focus-within {
  transform: scale(1.02); 
}
.actions {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  justify-content: center; 
}
.actions .v-btn {
  background: linear-gradient(135deg, #00b8d4, #007bff); 
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
  background: linear-gradient(
    135deg,
    #007bff,
    #00b8d4
  ); 
  transform: scale(1.05); 
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
}
.clear-btn {
  background: linear-gradient(
    135deg,
    #ff5252,
    #b71c1c
  ); 
  margin-right: 10px;
}
.clear-btn:hover {
  background: linear-gradient(135deg, #b71c1c, #ff5252);
}
.loading {
  display: block;
  margin: 30px auto;
  color: #00b8d4; 
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite; 
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
.error {
  color: #ff5252; 
  margin-top: 15px;
  padding: 10px;
  background: rgba(255, 82, 82, 0.1); 
  border-radius: 6px;
  border: 1px solid #ff5252;
  text-align: center;
  font-weight: 500;
  animation: fadeIn 0.5s ease-in; 
}
.table-container {
  margin-bottom: 40px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.05); 
  border-radius: 10px;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(5px); 
}
.table-title {
  font-size: 1.8rem;
  font-weight: 600;
  color: #fff; 
  margin-bottom: 15px;
  padding-left: 15px;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3); 
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
  background-color: rgba(255, 255, 255, 0.9); 
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  overflow: hidden; 
  table-layout: fixed; 
}
.custom-table th {
  background: linear-gradient(135deg, #007bff, #00b8d4); 
  color: #fff;
  padding: 15px 18px;
  text-align: left;
  font-weight: 600;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
  text-transform: uppercase;
  letter-spacing: 1px;
  white-space: nowrap; 
}
.custom-table td {
  padding: 15px 18px;
  color: #2c3e50; 
  transition: background 0.3s ease;
  word-break: break-word; 
}
.custom-table tr:nth-child(even) {
  background-color: rgba(245, 245, 245, 0.8); 
}
.custom-table tr:hover {
  background-color: rgba(227, 242, 253, 0.9); 
  transform: scale(1.01); 
}
.text-cell {
  word-break: break-word;
  max-width: 320px; 
  white-space: normal;
  color: #ececec; 
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
