import { defineStore } from "pinia";
import axios from "axios";
import { networkStore } from "./network";

export const mibTreeStore = defineStore("mibTree", {
  state: () => ({
    devices: [],
    mibTreeData: {},
    isLoading: false,
    error: null,
  }),
  actions: {
    setMibTreeDevices() {
      const network = networkStore();
      this.devices = network.devices;
      this.devices.forEach((device) => {
        this.buildMibTree(device.id, device.deviceIp);
      });
    },
    async buildMibTree(deviceId, deviceIp) {
      this.isLoading = true;
      this.error = null;
      try {
        const device = this.devices.find((d) => d.id === deviceId);
        if (!device) throw new Error("Device not found");

        const params = {
          deviceIp: deviceIp || device.deviceIp,
          community: "public",
          port: 161,
          version: "2c",
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
        this.isLoading = false;
      }
    },
    async performMibTreeAction(action, deviceId, params) {
      this.isLoading = true;
      this.error = null;
      try {
        const device = this.devices.find((d) => d.id === deviceId);
        if (!device) throw new Error("Device not found");

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
        this.isLoading = false;
      }
    },
  },
});
