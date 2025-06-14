<template>
  <div class="mib-tree-page">
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
          <div class="control-panel">
            <div class="actions">
              <v-btn color="primary" @click="refreshMibTree(activeTab)"
                >Refresh MIB Tree</v-btn
              >
            </div>
            <v-btn color="primary" @click="searchOid" class="search-btn"
                >Search</v-btn
              >
            <div class="search-bar">
              <v-text-field
                v-model="searchOidInput"
                label="Search OID (e.g., 1.3.6.1.2.1.1.1)"
                clearable
                @keyup.enter="searchOid"
                class="search-input"
              />
            </div>
          </div>
          <div v-if="error" class="error">{{ error }}</div>
          <div
            v-else-if="
              mibTreeData &&
              mibTreeData[device.id] &&
              mibTreeData[device.id].length
            "
            class="tree-section"
          >
            <h3>MIB Tree for Device {{ device.name }}</h3>
            <MibTree
              :data="mibTreeData[device.id] || []"
              :selected-oid="selectedOid"
              :highlighted-oid="mibTree.highlightedOid"
              @select-oid="handleSelectOid"
            />
          </div>
          <div v-else class="no-data">
            <p>No MIB Tree data available for device {{ device.name }}</p>
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
    const searchOidInput = ref("");

    const handleSelectOid = (oid) => {
      selectedOid.value = oid;
    };

    const refreshMibTree = async (deviceId) => {
      if (mibTree.loadingStates[deviceId]) return;
      const selectedDevice = mibTree.devices.find((d) => d.id === deviceId);
      if (selectedDevice) {
        await mibTree.performMibTreeAction("getMibTree", deviceId, {
          community: selectedDevice.community,
          port: selectedDevice.port,
          version: selectedDevice.version,
          authUsername: selectedDevice.authUsername,
          authPass: selectedDevice.authPass,
          privPass: selectedDevice.privPass,
          authProtocol: selectedDevice.authProtocol,
          privProtocol: selectedDevice.privProtocol,
          securityLevel: selectedDevice.securityLevel,
        });
      } else {
        mibTree.error = "Device not found for refresh";
      }
    };

    const searchOid = () => {
      if (!searchOidInput.value) {
        mibTree.error = "Please enter an OID to search";
        return;
      }
      const oidRegex = /^(\d+\.)*\d+$/;
      if (!oidRegex.test(searchOidInput.value)) {
        mibTree.error =
          "Invalid OID format. Use numbers and dots (e.g., 1.3.6.1.2.1.1.1.0)";
        return;
      }
      mibTree.searchOid(activeTab.value, searchOidInput.value);
      if (mibTree.searchResults && mibTree.searchResults.oid) {
        selectedOid.value = mibTree.searchResults.oid;
      }
    };

    watch(activeTab, (newTab) => {
      if (newTab && !mibTree.loadingStates[newTab]) {
        const selectedDevice = mibTree.devices.find((d) => d.id === newTab);
        if (
          selectedDevice &&
          (!mibTree.mibTreeData[newTab] ||
            mibTree.mibTreeData[newTab].length === 0)
        ) {
          mibTree.buildMibTree(newTab, selectedDevice.deviceIp);
        }
      }
      searchOidInput.value = "";
      mibTree.searchResults = null;
      mibTree.resetHighlight();
    });

    const isLoadingForDevice = computed(
      () => mibTree.loadingStates[activeTab.value] || false
    );

    return {
      devices: mibTree.devices,
      mibTreeData: mibTree.mibTreeData,
      error: mibTree.error,
      isLoadingForDevice,
      activeTab,
      selectedOid,
      handleSelectOid,
      refreshMibTree,
      searchOidInput,
      searchOid,
      searchResults: mibTree.searchResults,
      mibTree,
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
  background: radial-gradient(circle, rgba(0, 184, 212, 0.3), transparent 70%);
  opacity: 0.2;
  pointer-events: none;
}
.control-panel {
  padding: 20px;
  background: linear-gradient(135deg, #676790, #2c2c54);
  border-radius: 12px;
  margin: 20px 0;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  flex-wrap: wrap;
}
.actions {
  display: flex;
  gap: 15px;
}
.search-bar {
  display: flex;
  gap: 15px;
  align-items: center;
}
.search-input {
  width: 700px;
  height: 58px;
  background: linear-gradient(145deg, #ffffff, #e6e6e6);
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}
.search-input:hover {
  transform: scale(1.02);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
}
.search-btn {
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
.search-btn:hover {
  background: linear-gradient(135deg, #007bff, #00b8d4);
  transform: scale(1.05);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
}
.mib-tree-content {
  padding: 20px;
  border-radius: 12px;
  overflow-y: auto;
  transition: all 0.3s ease;
}
.tree-section {
  margin-top: 20px;
  padding: 15px;
  border-radius: 12px;
}
.no-data {
  padding: 15px;
  background: linear-gradient(145deg, #ffffff, #e6e6e6);
  margin: 15px 0;
  border-radius: 6px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
  text-align: center;
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
  background: linear-gradient(135deg, #007bff, #00b8d4);
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