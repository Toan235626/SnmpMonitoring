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
        <v-btn color="primary" @click="search">Search</v-btn>
      </div>
  
      <!-- Nút tác vụ -->
      <div class="actions">
        <v-btn @click="performAction('get')">Get</v-btn>
        <v-btn @click="performAction('getNext')">GetNext</v-btn>
        <v-btn @click="performAction('getBulk')">GetBulk</v-btn>
        <v-btn @click="performAction('walk')">Walk</v-btn>
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
  import { deviceStore } from '@/stores/device'; // Import store cụ thể
  
  export default {
    props: {
      deviceId: {
        type: String,
        required: true,
      },
    },
    setup(props) {
      const store = deviceStore(); // Sử dụng store cụ thể: deviceStore
  
      // Lọc lịch sử tìm kiếm theo deviceId
      const deviceHistory = computed(() =>
        store.searchHistory.filter((history) => history.deviceId === props.deviceId)
      );
  
      return {
        oid: ref(''),
        community: ref(''),
        results: computed(() => store.results[props.deviceId] || []),
        deviceHistory,
        isLoading: computed(() => store.isLoading),
        search: async () => {
          await store.search(props.deviceId, oid.value, community.value);
        },
        performAction: async (action) => {
          await store.performAction(action, props.deviceId);
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