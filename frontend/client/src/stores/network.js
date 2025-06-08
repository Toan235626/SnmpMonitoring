import { defineStore } from "pinia";
import axios from "axios";

export const networkStore = defineStore("network", {
  state: () => ({
    networks: [],
    devices: [],
    isLoading: false,
    error: null,
    scanDevicesSuccess: false,
  }),
  actions: {
    async scanNetwork() {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await axios.post("/api/device-scan/networks");
        this.networks = response.data.map((item, index) => ({
          id: String(index + 1),
          name: `Network ${index + 1}`,
          ipRange: `${item.baseIp}/${item.prefix}`,
        }));
      } catch (err) {
        this.error = err.message || "Failed to scan networks";
      } finally {
        this.isLoading = false;
      }
    },
    async scanDevices({ networkId, baseIp, prefix, community, version, port }) {
      this.isLoading = true;
      this.error = null;
      this.scanDevicesSuccess = false;
      try {
        const response = await axios.post(
          "/api/device-scan/scan-subnet",
          null,
          {
            params: {
              baseIp,
              prefix,
              community,
              port: port || 161,
              version,
            },
          }
        );
        this.devices = response.data.map((device, index) => ({
          id: `d${networkId}-${index + 1}`,
          name: (device.name || `Device ${index + 1}`).substring(0, 30),
          deviceIp: device.deviceIp || `${baseIp}.${index + 1}`,
          community: community || "public",
          port: port || 161,
          version: version || "2c",
          prefix,
          networkId,
        }));
        this.scanDevicesSuccess = true;
      } catch (err) {
        this.error = err.message || "Failed to scan devices";
      } finally {
        this.isLoading = false;
      }
    },
    clearDevices() {
      this.devices = [];
      this.scanDevicesSuccess = false;
    },
  },
});
