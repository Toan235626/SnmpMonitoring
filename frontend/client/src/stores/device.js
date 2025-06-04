import { defineStore } from 'pinia';
import axios from 'axios';
import { networkStore } from './network';

export const deviceStore = defineStore('device', {
  state: () => ({
    devices: [],
    activeTab: null,
    results: {},
    mibTreeData: {},
    searchHistory: [],
    isLoading: false,
    error: null,
  }),
  actions: {
    setDevices() {
      const network = networkStore();
      this.devices = network.devices;
      if (this.devices.length && !this.activeTab) {
        this.activeTab = this.devices[0].id;
      }
    },
    setMibTreeData(deviceId, data) {
      this.mibTreeData[deviceId] = data;
    },
    async fetchSnmpData(deviceId) {
      this.isLoading = true;
      this.error = null;
      try {
        const device = this.devices.find(d => d.id === deviceId);
        if (!device) throw new Error('Device not found');
        const response = await axios.post('/api/snmp/get', null, {
          params: {
            deviceIp: device.ipAddress,
            oid: '1.3.6.1.2.1.1.1.0', // Mặc định OID, có thể thay đổi qua form
            community: 'public', // Mặc định, có thể thay đổi qua form
          },
        });
        this.results[deviceId] = response.data.map((item) => ({
          oid: item.oid,
          value: item.value,
          deviceIp: device.ipAddress,
          community: 'public',
        }));
        this.searchHistory.push(
          ...response.data.map((item) => ({
            oid: item.oid,
            value: item.value,
            deviceIp: device.ipAddress,
            community: 'public',
            timestamp: new Date().toLocaleString(),
          }))
        );
      } catch (err) {
        this.error = err.response?.data?.error || 'An error occurred while fetching SNMP data.';
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    async fetchSnmpGetNext(deviceId) {
      this.isLoading = true;
      this.error = null;
      try {
        const device = this.devices.find(d => d.id === deviceId);
        if (!device) throw new Error('Device not found');
        let oidToFetch;
        let deviceIpToFetch = device.ipAddress;

        const currentResults = this.results[deviceId] || [];
        if (currentResults.length > 0) {
          oidToFetch = currentResults[currentResults.length - 1].oid;
        } else {
          oidToFetch = '1.3.6.1.2.1.1.1.0'; // Mặc định OID
        }

        const response = await axios.post('/api/snmp/getnext', null, {
          params: {
            deviceIp: deviceIpToFetch,
            oid: oidToFetch,
            community: 'public',
          },
        });

        if (!response.data || response.data.length === 0) {
          this.error = 'No more SNMP data available.';
          return;
        }

        const newResult = response.data.map((item) => ({
          oid: item.oid,
          value: item.value,
          deviceIp: deviceIpToFetch,
          community: 'public',
        }));
        this.results[deviceId] = [...(this.results[deviceId] || []), ...newResult];
        this.searchHistory.push(
          ...newResult.map((item) => ({
            oid: item.oid,
            value: item.value,
            deviceIp: deviceIpToFetch,
            community: 'public',
            timestamp: new Date().toLocaleString(),
          }))
        );
      } catch (err) {
        this.error = err.response?.data?.error || 'An error occurred while fetching SNMP data.';
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    async fetchSnmpGetBulk(deviceId) {
      this.isLoading = true;
      this.error = null;
      try {
        const device = this.devices.find(d => d.id === deviceId);
        if (!device) throw new Error('Device not found');
        const response = await axios.post('/api/snmp/bulk', null, {
          params: {
            deviceIp: device.ipAddress,
            oid: '1.3.6.1.2.1.1.1.0', // Mặc định OID
            community: 'public',
          },
        });
        this.results[deviceId] = response.data.map((item) => ({
          oid: item.oid,
          value: item.value,
          deviceIp: device.ipAddress,
          community: 'public',
        }));
        this.searchHistory.push(
          ...response.data.map((item) => ({
            oid: item.oid,
            value: item.value,
            deviceIp: device.ipAddress,
            community: 'public',
            timestamp: new Date().toLocaleString(),
          }))
        );
      } catch (err) {
        this.error = err.response?.data?.error || 'An error occurred while performing SNMP GetBulk.';
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    async fetchSnmpWalk(deviceId) {
      this.isLoading = true;
      this.error = null;
      try {
        const device = this.devices.find(d => d.id === deviceId);
        if (!device) throw new Error('Device not found');
        const response = await axios.post('/api/snmp/walk', null, {
          params: {
            deviceIp: device.ipAddress,
            oid: '1.3.6.1.2.1.1.1.0', // Mặc định OID
            community: 'public',
          },
        });
        this.results[deviceId] = response.data.map((item) => ({
          oid: item.oid,
          value: item.value,
          deviceIp: device.ipAddress,
          community: 'public',
        }));
      } catch (err) {
        this.error = err.response?.data?.error || 'An error occurred while performing SNMP Walk.';
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
  },
});