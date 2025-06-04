import { defineStore } from 'pinia';
import axios from 'axios';
import { networkStore } from './network';

export const deviceStore = defineStore('device', {
  state: () => ({
    devices: [],
    results: {},
    searchHistory: JSON.parse(localStorage.getItem('searchHistory')) || [],
    mibTreeData: {},
    activeTab: null,
    isLoading: false,
    error: null,
  }),
  actions: {
    setDevices() {
      const network = networkStore();
      this.devices = network.devices; // Đồng bộ từ networkStore
    },
    async performAction(action, deviceId) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await axios.post(`/api/${action}`, { deviceId });
        this.results[deviceId] = response.data.results;
        if (action === 'walk') {
          this.mibTreeData[deviceId] = response.data.mibTree || {};
        }
      } catch (err) {
        this.error = err.message || `Failed to perform ${action}`;
      } finally {
        this.isLoading = false;
      }
    },
    async search(deviceId, oid, community) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await axios.post('/api/search', { deviceId, oid, community });
        this.results[deviceId] = response.data.results;
        this.searchHistory.push({
          id: Date.now(),
          deviceId,
          time: new Date().toLocaleString(),
          oid,
          community,
          result: response.data.results,
        });
        localStorage.setItem('searchHistory', JSON.stringify(this.searchHistory));
      } catch (err) {
        this.error = err.message || 'Failed to search';
      } finally {
        this.isLoading = false;
      }
    },
    setActiveTab(deviceId) {
      this.activeTab = deviceId;
      if (this.mibTreeData[deviceId] == null) {
        this.performAction('walk', deviceId);
      }
    },
  },
});