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

    <div class="mib-tree">
      <div v-if="isLoading">
        <p>Loading MIB Tree...</p>
      </div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else-if="mibTreeData && mibTreeData[activeTab] && mibTreeData[activeTab].length">
        <h3>MIB Tree for Device {{ activeTab }}</h3>
        <MibTree
          :data="mibTreeData[activeTab] || []"
          @select-oid="handleSelectOid"
        />
      </div>
      <div v-else>
        <p>No MIB Tree data available for device {{ activeTab }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { deviceStore } from '@/stores/device';
import MibTree from '@/components/MibTree.vue';
import DeviceTab from '@/components/DeviceTab.vue';
import { ref } from 'vue';

export default {
  components: { MibTree, DeviceTab },
  setup() {
    const device = deviceStore();
    device.setDevices();
    const selectedOid = ref('');

    console.log('Dashboard - initial devices:', device.devices);
    console.log('Dashboard - initial activeTab:', device.activeTab);
    console.log('Dashboard - initial mibTreeData:', device.mibTreeData);

    const handleSelectOid = (oid) => {
      selectedOid.value = oid;
      console.log('Dashboard - selected OID:', oid);
    };

    return {
      devices: device.devices,
      activeTab: device.activeTab,
      mibTreeData: device.mibTreeData,
      setActiveTab: device.setActiveTab,
      error: device.error,
      isLoading: device.isLoading,
      selectedOid,
      handleSelectOid,
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
.mib-tree {
  width: 300px;
  background: #f4f4f4;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  border-left: 1px solid #ddd;
  padding: 10px;
}
@media (max-width: 768px) {
  .dashboard {
    flex-direction: column;
  }
  .mib-tree {
    position: static;
    width: 100%;
    height: auto;
  }
}
.error {
  color: red;
  margin: 10px;
}
</style>