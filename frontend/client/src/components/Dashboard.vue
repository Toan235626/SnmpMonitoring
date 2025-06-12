<template>
  <div class="dashboard">
    <div class="main-content">
      <v-alert v-if="!devices.length" type="info">
        No devices found. Please scan devices in Devices & Network.
      </v-alert>

      <v-tabs v-model="activeTab" class="tabs-wrap">
        <v-tab v-for="device in devices" :key="device.id" :value="device.id">
          <div class="tab-content">
            <div class="tab-name">{{ device.name }}</div>
            <div class="tab-ip">(IP: {{ device.deviceIp }})</div>
          </div>
        </v-tab>
      </v-tabs>

      <v-window v-model="activeTab">
        <v-window-item
          v-for="device in devices"
          :key="device.id"
          :value="device.id"
        >
          <div class="device-header">
            <div
              v-if="
                deviceData &&
                deviceData[device.id] &&
                deviceData[device.id].length
              "
            ></div>
            <DeviceTab :deviceId="device.id" :selected-oid="selectedOid" />
            <div v-if="error" class="error">{{ error }}</div>
          </div>
        </v-window-item>
      </v-window>
    </div>
  </div>
</template>

<script>
import { deviceStore } from "@/stores/device";
import DeviceTab from "@/components/DeviceTab.vue";
import { ref } from "vue";

export default {
  components: { DeviceTab },
  setup() {
    const device = deviceStore();
    device.setDevices();
    const activeTab = ref(device.devices.length ? device.devices[0].id : null);
    const selectedOid = ref("");

    return {
      devices: device.devices,
      activeTab: device.activeTab,
      setActiveTab: device.setActiveTab,
      error: device.error,
      selectedOid,
      activeTab,
    };
  },
};
</script>

<style scoped>
.dashboard {
  display: flex;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  font-family: "Poppins", "Segoe UI", sans-serif;
  padding: 15px;
}
.dashboard::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.4;
  pointer-events: none;
}
.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  border-radius: 10px;
  transition: all 0.3s ease;
}
.main-content:hover {
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}
.v-tabs {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 15px;
}
.v-tab {
  color: #475569;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  padding: 5px 18px;
  transition: all 0.3s ease;
}
.tabs-wrap {
  flex-wrap: wrap;
}

.tab-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.2;
}

.v-tab:hover {
  background: rgba(79, 70, 229, 0.1);
  transform: scale(1.03);
}
.v-tab--active {
  color: #4f46e5;
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(79, 70, 229, 0.2);
}
.v-alert {
  border-radius: 6px;
  margin: 15px 0;
  padding: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  animation: fadeIn 0.4s ease-in;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
