import { defineStore } from 'pinia';
import axios from 'axios';

export const networkStore = defineStore('network', {
  state: () => ({
    networks: [],
    devices: [],
    isLoading: false,
    error: null,
  }),
  actions: {
    async scanNetwork() {
        this.isLoading = true;
        this.error = null;
        try {
          const response = await axios.post('/api/device-scan/networks');
          console.log('API Response:', response.data);
          this.networks = response.data.map((ip, index) => ({
            id: String(index + 1),
            name: `Network ${index + 1}`,
            ipRange: `${ip}/24`,
          }));
          console.log('Updated Networks:', this.networks);
        } catch (err) {
          this.error = err.message || 'Failed to scan networks';
          console.error('API Error:', err);
        } finally {
          this.isLoading = false;
        }
      },
      async scanDevices({ networkId, baseIp, community, version, port }) {
        this.isLoading = true;
        this.error = null;
        try {
          const response = await axios.post('/api/device-scan/scan-subnet', null, {
            params: {
              baseIp,
              community,
              port: port || 161,
              version,
            },
          });
          console.log('Scan Devices Response:', response.data);
          this.devices = response.data.map((device, index) => ({
            id: `d${networkId}-${index + 1}`,
            name: (device.name || `Device ${index + 1}`).substring(0, 30),
            ipAddress: device.ipAddress || `${baseIp}.${index + 1}`,
          }));
        } catch (err) {
          this.error = err.message || 'Failed to scan devices';
          console.error('Scan Devices Error:', err);
        } finally {
          this.isLoading = false;
        }
      },
    clearDevices() {
      this.devices = [];
    },
  },
});