import { defineStore } from "pinia";
import axiosInstance from "@/axios.js";
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
    
        let requestParams = {
          deviceIp: device.deviceIp,
          oid: params.oid,
          community: params.community,
          port: params.port,
          version: params.version,
        };
    
        if (params.version === "3") {
          requestParams.authUsername = params.authUsername;
          if (params.securityLevel === "2" || params.securityLevel === "3") {
            requestParams.authPass = params.authPass;
            requestParams.authProtocol = params.authProtocol;
          }
          if (params.securityLevel === "3") {
            requestParams.privPass = params.privPass;
            requestParams.privProtocol = params.privProtocol;
          }
          requestParams.securityLevel = params.securityLevel;
        }
    
        if (
          params.version === "3" &&
          !params.authUsername
        ) {
          throw new Error("SNMPv3 requires authUsername");
        }
        if (
          params.version === "3" &&
          (params.securityLevel === "2" || params.securityLevel === "3") &&
          (!params.authPass || !params.authProtocol)
        ) {
          throw new Error("SNMPv3 authNoPriv or authPriv requires authPass and authProtocol");
        }
        if (
          params.version === "3" &&
          params.securityLevel === "3" &&
          (!params.privPass || !params.privProtocol)
        ) {
          throw new Error("SNMPv3 authPriv requires privPass and privProtocol");
        }

        let endpoint;
        let processResponse;

        switch (action) {
          case "get":
            endpoint = "/snmp/get";
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
            endpoint =  "/snmp/getnext";
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
            endpoint ="/snmp/bulk";
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
            endpoint = "/snmp/walk";
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

        const response = await axiosInstance.post(endpoint, null, {
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
