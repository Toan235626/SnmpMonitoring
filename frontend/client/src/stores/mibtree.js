import { defineStore } from "pinia";
import axios from "axios";
import { networkStore } from "./network";

export const mibTreeStore = defineStore("mibTree", {
  state: () => ({
    devices: [],
    mibTreeData: {},
    loadingStates: {},
    error: null,
    searchResults: null,
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
        if (device.version === "3") {
          params.authUsername = device.authUsername;
          params.authPass = device.authPass;
          params.privPass = device.privPass;
          params.authProtocol = device.authProtocol;
          params.privProtocol = device.privProtocol;
          params.securityLevel = device.securityLevel || "3";
        }
    
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
    
        const requestParams = {
          deviceIp: device.deviceIp,
          community: params.community || device.community || "public",
          port: params.port || device.port || 161,
          version: params.version || device.version || "2c",
        };
        if (requestParams.version === "3") {
          requestParams.authUsername = params.authUsername || device.authUsername;
          if (requestParams.securityLevel === "2" || requestParams.securityLevel === "3") {
            requestParams.authPass = params.authPass || device.authPass;
            requestParams.authProtocol = params.authProtocol || device.authProtocol;
          }
          if (requestParams.securityLevel === "3") {
            requestParams.privPass = params.privPass || device.privPass;
            requestParams.privProtocol = params.privProtocol || device.privProtocol;
          }
          requestParams.securityLevel = params.securityLevel || device.securityLevel || "3";
        }
    
        if (
          requestParams.version === "3" &&
          !requestParams.authUsername
        ) {
          throw new Error("SNMPv3 requires authUsername");
        }
        if (
          requestParams.version === "3" &&
          (requestParams.securityLevel === "2" || requestParams.securityLevel === "3") &&
          (!requestParams.authPass || !requestParams.authProtocol)
        ) {
          throw new Error("SNMPv3 authNoPriv or authPriv requires authPass and authProtocol");
        }
        if (
          requestParams.version === "3" &&
          requestParams.securityLevel === "3" &&
          (!requestParams.privPass || !requestParams.privProtocol)
        ) {
          throw new Error("SNMPv3 authPriv requires privPass and privProtocol");
        }
    
        let endpoint;
        let processResponse;
    
        if (action === "getMibTree") {
          endpoint = "/api/mib-tree";
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
    searchOid(deviceId, oid) {
      this.error = null;
      this.searchResults = null;
      const tree = this.mibTreeData[deviceId];
      if (!tree || !tree.length) {
        this.error = "No MIB tree data available for device";
        return;
      }

      const findNode = (nodes, targetOid, path = []) => {
        for (const node of nodes) {
          const currentPath = [...path, node];
          if (node.oid === targetOid) {
            return { node, path: currentPath };
          }
          if (node.children && node.children.length) {
            const result = findNode(node.children, targetOid, currentPath);
            if (result) return result;
          }
        }
        return null;
      };

      const result = findNode(tree, oid);
      if (result) {
        this.searchResults = {
          deviceId,
          oid,
          node: result.node,
          path: result.path, 
        };
        result.path.forEach((node) => {
          if (node.children && node.children.length) {
            node.expanded = true;
          }
        });
      } else {
        this.error = `OID ${oid} not found in MIB tree for device ${deviceId}`;
      }
    },
  },
});