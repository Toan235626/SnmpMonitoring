<!-- MIBTreePage.vue -->
<template>
  <div class="mib-tree-page">
    <v-alert v-if="!devices.length" type="info">
      No devices found. Please scan devices in Devices & Network.
    </v-alert>
    <v-tabs v-model="activeTab">
      <v-tab v-for="device in devices" :key="device.id" :value="device.id">
        {{ device.name }}
        (IP: {{ device.deviceIp }})
      </v-tab>
    </v-tabs>
    <v-progress-circular
      v-if="isLoadingForDevice"
      indeterminate
      color="primary"
      class="loading"
    />
    <v-window v-model="activeTab">
      <v-window-item
        v-for="device in devices"
        :key="device.id"
        :value="device.id"
      >
        <div class="mib-tree-content">
          <div class="actions">
            <v-btn color="primary" @click="refreshMibTree(device.id)"
              >Refresh MIB Tree</v-btn
            >
          </div>
          <div v-if="error" class="error">{{ error }}</div>
          <div
            v-else-if="
              mibTreeData &&
              mibTreeData[device.id] &&
              mibTreeData[device.id].length
            "
          >
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
import { mibTreeStore } from "@/stores/mibtree";
import MibTree from "@/components/MibTree.vue";
import { ref, watch, computed } from "vue";

export default {
  components: { MibTree },
  setup() {
    const mibTree = mibTreeStore();
    mibTree.setMibTreeDevices();
    const activeTab = ref(
      mibTree.devices.length ? mibTree.devices[0].id : null
    );
    const selectedOid = ref("");

    const handleSelectOid = (oid) => {
      selectedOid.value = oid;
    };

    const refreshMibTree = async (deviceId) => {
      if (mibTree.loadingStates[deviceId]) return;
      const selectedDevice = mibTree.devices.find((d) => d.id === deviceId);
      if (selectedDevice) {
        await mibTree.buildMibTree(deviceId, selectedDevice.deviceIp);
      } else {
        mibTree.error = "Device not found for refresh";
      }
    };

    watch(activeTab, (newTab) => {
      if (newTab && !mibTree.loadingStates[newTab]) {
        const selectedDevice = mibTree.devices.find((d) => d.id === newTab);
        if (selectedDevice && (!mibTree.mibTreeData[newTab] || mibTree.mibTreeData[newTab].length === 0)) {
          mibTree.buildMibTree(newTab, selectedDevice.deviceIp);
        }
      }
    });

    const isLoadingForDevice = computed(() => mibTree.loadingStates[activeTab.value] || false);

    return {
      devices: mibTree.devices,
      mibTreeData: mibTree.mibTreeData,
      error: mibTree.error,
      isLoadingForDevice, // Sử dụng computed để kiểm tra loading cho tab hiện tại
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
  padding: 30px;
  min-height: 100vh;
  position: relative;
  overflow: hidden; 
  font-family: "Poppins", "Segoe UI", sans-serif; 
}
.mib-tree-page::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(
    circle,
    rgba(0, 184, 212, 0.3),
    transparent 70%
  ); 
  opacity: 0.2;
  pointer-events: none; 
}
.mib-tree-content {
  padding: 20px;
  border-radius: 12px; 
  overflow-y: auto;
  transition: all 0.3s ease; 
}
.actions {
  margin-bottom: 25px;
  gap: 15px; 
  justify-content: center; 
}
.actions .v-btn {
  background: linear-gradient(
    135deg,
    #00b8d4,
    #007bff
  ); 
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
.error {
  color: #ff5252; 
  margin: 15px 0;
  padding: 10px;
  background: rgba(255, 82, 82, 0.1); 
  border-radius: 6px;
  border: 1px solid #ff5252;
  font-weight: 500;
  text-align: center;
  animation: fadeIn 0.5s ease-in; 
}

.loading {
  display: block;
  margin: 30px auto;
  color: #00b8d4; 
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite; 
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
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
