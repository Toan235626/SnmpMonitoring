<template>
    <div class="device-tab">
      <!-- Thanh tìm kiếm -->
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
        <v-btn color = "primary" @click="performAction('get')">Get</v-btn>
        <v-btn color = "primary" @click="performAction('getNext')">GetNext</v-btn>
        <v-btn color = "primary" @click="performAction('getBulk')">GetBulk</v-btn>
        <v-btn color = "primary" @click="performAction('walk')">Walk</v-btn>
      </div>
  
  
      <!-- Trạng thái loading -->
      <v-progress-circular
        v-if="isLoading"
        indeterminate
        color="primary"
        class="loading"
      />
  
      <!-- Bảng kết quả -->
      <v-table v-if="results.length">
        <thead>
          <tr>
            <th>OID</th>
            <th>Value</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="result in results" :key="result.oid">
            <td>{{ result.oid }}</td>
            <td>{{ result.value }}</td>
          </tr>
        </tbody>
      </v-table>
  
      <!-- Bảng lịch sử tìm kiếm -->
      <v-table v-if="deviceHistory.length">
        <thead>
          <tr>
            <th>Time</th>
            <th>OID</th>
            <th>Community</th>
            <th>Result</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="history in deviceHistory" :key="history.id">
            <td>{{ history.time }}</td>
            <td>{{ history.oid }}</td>
            <td>{{ history.community }}</td>
            <td>{{ history.result }}</td>
          </tr>
        </tbody>
      </v-table>
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
    },
    setup(props) {
      const store = deviceStore();
      const oid = ref('1.3.6.1.2.1.1.1.0'); // Mặc định OID
      const community = ref('public'); // Mặc định community
  
      const deviceHistory = computed(() =>
        store.searchHistory.filter((history) => history.deviceId === props.deviceId)
      );
  
      return {
        oid,
        community,
        results: computed(() => store.results[props.deviceId] || []),
        deviceHistory,
        isLoading: computed(() => store.isLoading),
        performAction: async (action) => {
          switch (action) {
            case 'get':
              await store.fetchSnmpData(props.deviceId);
              break;
            case 'getNext':
              await store.fetchSnmpGetNext(props.deviceId);
              break;
            case 'getBulk':
              await store.fetchSnmpGetBulk(props.deviceId);
              break;
            case 'walk':
              await store.fetchSnmpWalk(props.deviceId);
              break;
          }
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
  </style>