import { defineStore } from "pinia";
import axios from '@/axios.js';

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
        const response = await axios.post('/device-scan/networks')
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
    async scanDevices({
      networkId,
      baseIp,
      prefix,
      community,
      port,
      mode,
      version,
      authUsername,
      authPass,
      privPass,
      authProtocol,
      privProtocol,
      securityLevel
    }) {
      this.isLoading = true;
      this.error = null;
      this.scanDevicesSuccess = false;
      try {
        const params = {
          baseIp,
          prefix,
          community,
          port: port || 161,
          mode,
        };

        if (mode === "unicast") {
          params.version = version || "2c";
          if (version === "3") {
            params.authUsername = authUsername;
            if (securityLevel === "2" || securityLevel === "3") {
              params.authPass = authPass;
              params.authProtocol = authProtocol;
            }
            if (securityLevel === "3") {
              params.privPass = privPass;
              params.privProtocol = privProtocol;
            }
            params.securityLevel = securityLevel || "3";
          }
        }

        const response = await axios.post(
          "/device-scan/scan-subnet",
          null,
          { params }
        );
        this.devices = response.data.map((device, index) => ({
          id: `d${networkId}-${index + 1}`,
          name: (device.name || `Device ${index + 1}`).substring(0, 30),
          deviceIp: device.deviceIp || `${baseIp}.${index + 1}`,
          community: community || "public",
          port: port || 161,
          version: mode === "unicast" ? (version || "2c") : undefined,
          prefix,
          networkId,
          authUsername: mode === "unicast" && version === "3" ? authUsername : undefined,
          authPass: mode === "unicast" && version === "3" && (securityLevel === "2" || securityLevel === "3") ? authPass : undefined,
          privPass: mode === "unicast" && version === "3" && securityLevel === "3" ? privPass : undefined,
          authProtocol: mode === "unicast" && version === "3" && (securityLevel === "2" || securityLevel === "3") ? authProtocol : undefined,
          privProtocol: mode === "unicast" && version === "3" && securityLevel === "3" ? privProtocol : undefined,
          securityLevel: mode === "unicast" && version === "3" ? (securityLevel || "3") : undefined,
        }));
        this.scanDevicesSuccess = true;
      } catch (err) {
        this.error = err.message || "Failed to scan devices";
      } finally {
        this.isLoading = false;
      }
    },
  }  
});