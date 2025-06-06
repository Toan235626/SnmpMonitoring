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
      console.log('networkStore devices:', network.devices);
      this.devices = network.devices;
      console.log('setDevices - this.devices:', this.devices);
      if (this.devices.length && !this.activeTab) {
        this.activeTab = this.devices[0].id;
        console.log('setDevices - activeTab:', this.activeTab);
      }
      this.devices.forEach(device => {
        console.log('Calling buildMibTree for device:', device.id, device.deviceIp);
        this.buildMibTree(device.id, device.deviceIp);
      });
    },
    async buildMibTree(deviceId, deviceIp) {
      this.isLoading = true;
      this.error = null;
      try {
        const device = this.devices.find(d => d.id === deviceId);
        console.log('buildMibTree - device found:', device);
        if (!device) throw new Error('Device not found');

        const params = {
          deviceIp: deviceIp || device.deviceIp,
          community: 'public',
          port: 161,
          version: '2c',
        };

        console.log('Fetching MIB Tree for:', deviceIp, 'with params:', params);
        const response = await axios.post('/api/mib-tree', null, { params });
        console.log('buildMibTree - response.data:', response.data);
        this.mibTreeData[deviceId] = response.data;
        console.log('buildMibTree - mibTreeData updated:', this.mibTreeData);
      } catch (err) {
        this.error = err.response?.data?.error || `An error occurred while fetching MIB tree: ${err.message}`;
        console.error('MIB Tree error:', err.response ? err.response.data : err.message);
        this.mibTreeData[deviceId] = [];
      } finally {
        this.isLoading = false;
      }
    },
    async performAction(action, deviceId, params) {
      this.isLoading = true;
      this.error = null;
      try {
        const device = this.devices.find(d => d.id === deviceId);
        if (!device) throw new Error('Device not found');

        if (!params.oid && action !== 'mibTree' && action !== 'getMibTree') {
          throw new Error('OID is required for this action');
        }
        if (!params.community || !params.port || !params.version) {
          throw new Error('Missing required parameters: Community, Port, or Version');
        }

        if (params.version === '3' && (!params.authUsername || !params.authPass || !params.privPass || !params.authProtocol || !params.privProtocol)) {
          throw new Error('SNMPv3 requires all authentication parameters');
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

        if (params.version === '3') {
          requestParams.authUsername = params.authUsername;
          requestParams.authPass = params.authPass;
          requestParams.privPass = params.privPass;
          requestParams.authProtocol = params.authProtocol;
          requestParams.privProtocol = params.privProtocol;
          requestParams.securityLevel = params.securityLevel;
        }

        switch (action) {
          case 'get':
            endpoint = '/api/snmp/get';
            processResponse = (response) => {
              if (!response.data || response.data.length === 0) {
                this.error = 'No SNMP data returned.';
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
          case 'getNext':
            endpoint = '/api/snmp/getnext';
            const currentResults = this.results[deviceId] || [];
            requestParams.oid = currentResults.length > 0 ? currentResults[currentResults.length - 1].oid : params.oid;
            processResponse = (response) => {
              if (!response.data || response.data.length === 0) {
                this.error = 'No more SNMP data available.';
                return;
              }
              const newResult = response.data.map((item) => ({
                oid: item.oid,
                value: item.value,
                deviceIp: device.deviceIp,
                community: params.community,
              }));
              this.results[deviceId] = [...(this.results[deviceId] || []), ...newResult];
              this.searchHistory.push(
                ...newResult.map((item) => ({
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
          case 'getBulk':
            endpoint = '/api/snmp/bulk';
            processResponse = (response) => {
              if (!response.data || response.data.length === 0) {
                this.error = 'No SNMP data returned.';
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
          case 'walk':
            endpoint = '/api/snmp/walk';
            processResponse = (response) => {
              if (!response.data || response.data.length === 0) {
                this.error = 'No SNMP data returned.';
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
          case 'getMibTree': // Thêm hành động để gọi MIB tree
            endpoint = '/api/mib-tree';
            requestParams = {
              deviceIp: device.deviceIp,
              community: params.community,
              port: params.port,
              version: params.version,
            };
            if (params.version === '3') {
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
            break;
          default:
            throw new Error('Invalid action');
        }

        console.log('Sending request to:', endpoint, 'with params:', requestParams);

        const response = await axios.post(endpoint, null, {
          params: requestParams,
        });

        processResponse(response);
      } catch (err) {
        this.error = err.response?.data?.error || `An error occurred while performing ${action}: ${err.message}`;
        console.error('Request error:', err.response ? err.response.data : err.message);
      } finally {
        this.isLoading = false;
      }
    },
  },
});