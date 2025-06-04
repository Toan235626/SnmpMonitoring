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
          <v-window-item v-for="device in devices" :key="device.id" :value="device.id">
            <DeviceTab :deviceId="device.id" />
          </v-window-item>
        </v-window>
      </div>
  
      <div class="mib-tree">
        <MibTree :data="mibTreeData[activeTab] || {}" />
      </div>
    </div>
  </template>
  
  <script>
  import { deviceStore } from '@/stores/device';
  import { networkStore } from '@/stores/network';
  import MibTree from '@/components/MibTree.vue';
  import DeviceTab from '@/components/DeviceTab.vue';
  
  export default {
    components: { MibTree, DeviceTab },
    setup() {
      const device = deviceStore();
      const network = networkStore();
  
      device.setDevices();
  
      return {
        devices: device.devices,
        activeTab: device.activeTab,
        mibTreeData: device.mibTreeData,
        setActiveTab: device.setActiveTab,
        networkDevices: network.devices,
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
  </style>