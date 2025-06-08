import { defineStore } from "pinia";
import axios from "axios";
import { networkStore } from "./network";

export const mibTreeStore = defineStore("mibTree", {
  state: () => ({
    devices: [],
    mibTreeData: {},
    loadingStates: {},
    error: null,
  }),
  actions: {
    async setMibTreeDevices() { 
      this.isLoading = true;
      const network = networkStore();
      this.devices = network.devices;
      try {
        await Promise.all(
          this.devices.map(async (device) => {
            this.loadingStates[device.id] = true; 
            try {
              await this.buildMibTree(device.id, device.deviceIp);
            } finally {
              this.loadingStates[device.id] = false; 
            }
          })
        );
      } catch (err) {
        this.error = "Failed to load MIB tree for devices: " + err.message;
        console.error("setMibTreeDevices error:", err);
      } finally {
        this.isLoading = false; 
      }
    },
    async buildMibTree(deviceId, deviceIp) {
      this.loadingStates[deviceId] = true;
      this.error = null;
      try {
        const device = this.devices.find((d) => d.id === deviceId);
        if (!device) throw new Error("Device not found");

        const params = {
          deviceIp: deviceIp || device.deviceIp,
          community: device.community || "public",
          port: device.port || 161,
          version: device.version || "2c",
        };

        const response = await axios.post("/api/mib-tree", null, { params });
        this.mibTreeData[deviceId] = response.data;
      } catch (err) {
        this.error =
          err.response?.data?.error ||
          `An error occurred while fetching MIB tree: ${err.message}`;
        console.error(
          "MIB Tree error:",
          err.response ? err.response.data : err.message
        );
        this.mibTreeData[deviceId] = [];
      } finally {
        this.loadingStates[deviceId] = false;
      }
    },
    async performMibTreeAction(action, deviceId, params) {
      this.loadingStates[deviceId] = true;
      this.error = null;
      try {
        const device = this.devices.find((d) => d.id === deviceId);
        if (!device) throw new Error("Device not found");
        
        const community = device.community || params.community || "public";
        const port = device.port || params.port || 161;
        const version = device.version || params.version || "2c";

        if (!params.community || !params.port || !params.version) {
          throw new Error(
            "Missing required parameters: Community, Port, or Version"
          );
        }

        if (
          params.version === "3" &&
          (!params.authUsername ||
            !params.authPass ||
            !params.privPass ||
            !params.authProtocol ||
            !params.privProtocol)
        ) {
          throw new Error("SNMPv3 requires all authentication parameters");
        }

        let endpoint;
        let processResponse;
        let requestParams;

        if (action === "getMibTree") {
          endpoint = "/api/mib-tree";
          requestParams = {
            deviceIp: device.deviceIp,
            community: params.community,
            port: params.port,
            version: params.version,
          };
          if (params.version === "3") {
            requestParams.authUsername = params.authUsername;
            requestParams.authPass = params.authPass;
            requestParams.privPass = params.privPass;
            requestParams.authProtocol = params.authProtocol;
            requestParams.privProtocol = params.privProtocol;
            requestParams.securityLevel = params.securityLevel;
          }
          processResponse = (response) => {
            this.mibTreeData[deviceId] = response.data;
          };
        } else {
          throw new Error("Invalid action");
        }

        const response = await axios.post(endpoint, null, {
          params: requestParams,
        });

        processResponse(response);
      } catch (err) {
        this.error =
          err.response?.data?.error ||
          `An error occurred while performing ${action}: ${err.message}`;
        console.error(
          "Request error:",
          err.response ? err.response.data : err.message
        );
      } finally {
        this.loadingStates[deviceId] = false;
      }
    },
  },
});