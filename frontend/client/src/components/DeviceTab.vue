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
        <h3 class="table-title">Device History</h3>
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
  padding: 20px;
}
.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}
.search-input {
  max-width: 300px;
}
.actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.loading {
  display: block;
  margin: 20px auto;
}
.error {
  color: red;
  margin-top: 10px;
}
.table-container {
  margin-bottom: 30px;
  overflow-x: auto;
}
.table-title {
  font-size: 1.5rem;
  font-weight: 500;
  color: #333;
  margin-bottom: 10px;
  padding-left: 10px;
}
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.clear-btn {
  margin-right: 10px;
}
.custom-table {
  width: 100%;
  border-collapse: collapse;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.custom-table th {
  background-color: #54aaff; 
  color: white;
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  border-bottom: 2px solid #ddd;
}
.custom-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #ddd;
}
.custom-table tr:nth-child(even) {
  background-color: #f5f5f5; 
}
.custom-table tr:hover {
  background-color: #e3f2fd; 
}
.text-cell {
  word-break: break-word; /* Ngắt từ nếu nội dung quá dài */
  max-width: 300px; /* Giới hạn chiều rộng tối đa */
  white-space: normal; /* Cho phép xuống dòng */
}
</style>