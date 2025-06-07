<template>
    <div class="mib-tree-page">
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
          <div class="mib-tree-content">
            <div class="actions">
              <v-btn color="primary" @click="refreshMibTree(device.id)">Refresh MIB Tree</v-btn>
            </div>
            <div v-if="error" class="error">{{ error }}</div>
            <div v-else-if="mibTreeData && mibTreeData[device.id] && mibTreeData[device.id].length">
              <h3>MIB Tree for Device {{ device.id }}</h3>
              <MibTree
                :data="mibTreeData[device.id] || []"
                @select-oid="handleSelectOid"
              />
            </div>
            <div v-else>
              <p>No MIB Tree data available for device {{ device.id }}</p>
            </div>
          </div>
        </v-window-item>
      </v-window>
    </div>
  </template>
  
  <script>
  import { mibTreeStore } from '@/stores/mibtree';
  import MibTree from '@/components/MibTree.vue';
  import { ref } from 'vue';
  
  export default {
    components: { MibTree },
    setup() {
      const mibTree = mibTreeStore();
      mibTree.setMibTreeDevices();
      const activeTab = ref(mibTree.devices.length ? mibTree.devices[0].id : null);
      const selectedOid = ref('');
  
      const handleSelectOid = (oid) => {
        selectedOid.value = oid;
      };
  
      const refreshMibTree = async (deviceId) => {
        const selectedDevice = mibTree.devices.find(d => d.id === deviceId);
        if (selectedDevice) {
          await mibTree.buildMibTree(deviceId, selectedDevice.deviceIp);
        } else {
          mibTree.error = 'Device not found for refresh';
        }
      };
  
      return {
        devices: mibTree.devices,
        mibTreeData: mibTree.mibTreeData,
        error: mibTree.error,
        activeTab,
        selectedOid,
        handleSelectOid,
        refreshMibTree,
      };
    },
  };
  </script>
  
  <style scoped>
  .mib-tree-page {
    padding: 20px;
    min-height: 100vh;
  }
  .mib-tree-content {
    padding: 10px;
    background: #f4f4f4;
    overflow-y: auto;
    border-left: 1px solid #ddd;
  }
  .actions {
    margin-bottom: 20px;
  }
  .error {
    color: red;
    margin: 10px 0;
  }
  </style>