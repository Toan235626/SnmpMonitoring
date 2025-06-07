<template>
  <div class="dashboard">
    <div class="main-content">
      <v-alert v-if="!devices.length" type="info">
        No devices found. Please scan devices in Devices & Network.
      </v-alert>

      <v-tabs v-model="activeTab">
        <v-tab v-for="device in devices" :key="device.id" :value="device.id">
          {{ device.name }}
        </v-tab>
      </v-tabs>

      <v-window v-model="activeTab">
        <v-window-item
          v-for="device in devices"
          :key="device.id"
          :value="device.id"
        >
          <DeviceTab
            :deviceId="device.id"
            :selected-oid="selectedOid"
          />
        </v-window-item>
      </v-window>
    </div>
  </div>
</template>

<script>
import { deviceStore } from '@/stores/device';
import DeviceTab from '@/components/DeviceTab.vue';
import { ref } from 'vue';

export default {
  components: { DeviceTab },
  setup() {
    const device = deviceStore();
    device.setDevices();
    const selectedOid = ref('');

    return {
      devices: device.devices,
      activeTab: device.activeTab,
      setActiveTab: device.setActiveTab,
      error: device.error,
      isLoading: device.isLoading,
      selectedOid,
    };
  },
};
</script>

<style scoped>
.dashboard {
  display: flex;
  min-height: 100vh;
}
.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
@media (max-width: 768px) {
  .dashboard {
    flex-direction: column;
  }
}
.error {
  color: red;
  margin: 10px;
}
</style>