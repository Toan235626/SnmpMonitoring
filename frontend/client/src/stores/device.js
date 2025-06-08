import { defineStore } from "pinia";
import axios from "axios";
import { networkStore } from "./network";

export const deviceStore = defineStore("device", {
  state: () => ({
    devices: [],
    activeTab: null,
    results: {},
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
    async performAction(action, deviceId, params) {
      this.isLoading = true;
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
        let requestParams = {
          deviceIp: device.deviceIp,
          oid: params.oid,
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

        switch (action) {
          case "get":
            endpoint = "/api/snmp/get";
            processResponse = (response) => {
              if (!response.data || response.data.length === 0) {
                this.error = "No SNMP data returned.";
                return;
              }
              this.results[deviceId] = response.data.map((item) => ({
                oid: item.oid,
                value: item.value,
                deviceIp: device.deviceIp,
                community: params.community,
              }));
              this.searchHistory.push(
                ...response.data.map((item) => ({
                  id: Date.now(),
                  deviceId,
                  oid: item.oid,
                  value: item.value,
                  deviceIp: device.deviceIp,
                  community: params.community,
                  time: new Date().toLocaleString(),
                  result: item.value,
                }))
              );
            };
            break;
          case "getNext":
            endpoint = "/api/snmp/getnext";
            const currentResults = this.results[deviceId] || [];
            requestParams.oid =
              currentResults.length > 0
                ? currentResults[currentResults.length - 1].oid
                : params.oid;
            processResponse = (response) => {
              if (!response.data || response.data.length === 0) {
                this.error = "No more SNMP data available.";
                return;
              }
              this.results[deviceId] = response.data.map((item) => ({
                oid: item.oid,
                value: item.value,
                deviceIp: device.deviceIp,
                community: params.community,
              }));
              this.searchHistory.push(
                ...response.data.map((item) => ({
                  id: Date.now(),
                  deviceId,
                  oid: item.oid,
                  value: item.value,
                  deviceIp: device.deviceIp,
                  community: params.community,
                  time: new Date().toLocaleString(),
                  result: item.value,
                }))
              );
            };
            break;
          case "getBulk":
            endpoint = "/api/snmp/bulk";
            processResponse = (response) => {
              if (!response.data || response.data.length === 0) {
                this.error = "No SNMP data returned.";
                return;
              }
              this.results[deviceId] = response.data.map((item) => ({
                oid: item.oid,
                value: item.value,
                deviceIp: device.deviceIp,
                community: params.community,
              }));
              this.searchHistory.push(
                ...response.data.map((item) => ({
                  id: Date.now(),
                  deviceId,
                  oid: item.oid,
                  value: item.value,
                  deviceIp: device.deviceIp,
                  community: params.community,
                  time: new Date().toLocaleString(),
                  result: item.value,
                }))
              );
            };
            break;
          case "walk":
            endpoint = "/api/snmp/walk";
            processResponse = (response) => {
              if (!response.data || response.data.length === 0) {
                this.error = "No SNMP data returned.";
                return;
              }
              this.results[deviceId] = response.data.map((item) => ({
                oid: item.oid,
                value: item.value,
                deviceIp: device.deviceIp,
                community: params.community,
              }));
            };
            break;
          default:
            throw new Error("Invalid action");
        }

        console.log(
          "Sending request to:",
          endpoint,
          "with params:",
          requestParams
        );

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
