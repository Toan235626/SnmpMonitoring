<template>
  <div>
    <div class="header"></div>
    <v-btn color="green" @click="handleScanNetwork" :disabled="isLoading"
      >Scan Network</v-btn
    >
    <v-progress-circular
      v-if="isLoading"
      indeterminate
      color="primary"
      class="loading"
    />
    <v-alert v-if="error" type="error">{{ error }}</v-alert>
    <v-alert v-if="!networks.length && !isLoading && !error" type="info">
      No networks found. Click "Scan Network" to start.
    </v-alert>
    <v-table v-if="networks.length">
      <thead>
        <tr>
          <th>Network Name</th>
          <th>IP Range</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="network in networks" :key="network.id">
          <td>{{ network.name }}</td>
          <td>{{ network.ipRange }}</td>
          <td>
            <v-btn color="green" @click="openScanDialog(network)"
              >Scan Devices</v-btn
            >
          </td>
        </tr>
      </tbody>
    </v-table>

    <v-dialog v-model="dialog" max-width="500px">
      <v-card>
        <v-card-title>Scan Devices</v-card-title>
        <v-card-text>
          <v-form ref="form">
            <v-text-field
              v-model="scanForm.baseIp"
              label="Base IP (e.g., 192.168.5)"
              :rules="[(v) => !!v || 'Base IP is required']"
            />
            <v-text-field
              v-model="scanForm.prefix"
              label="IP Prefix (e.g., 24)"
              :rules="[(v) => !!v || 'IP Prefix is required']"
            />
            <v-text-field
              v-model="scanForm.community"
              label="Community String"
              :rules="[(v) => !isSnmpV3 || !!v || 'Community is required']"
              v-if="!isSnmpV3"
            />
            <v-text-field
              v-model="scanForm.port"
              label="Port"
              type="number"
              :rules="[
                (v) => !!v || 'Port is required',
                (v) => Number(v) > 0 || 'Port must be a positive number',
                (v) => Number.isInteger(Number(v)) || 'Port must be an integer',
              ]"
            />
            <v-select
              v-model="scanForm.version"
              label="SNMP Version"
              :items="['1', '2c', '3']"
              :rules="[(v) => !!v || 'Version is required']"
            />
            <template v-if="isSnmpV3">
              <v-select
                v-model="scanForm.securityLevel"
                label="Security Level"
                :items="['1', '2', '3']"
                :rules="[(v) => !!v || 'Security Level is required']"
              />
              <v-text-field
                v-model="scanForm.authUsername"
                label="Auth Username"
                :rules="[(v) => !!v || 'Auth Username is required']"
              />
              <template
                v-if="
                  scanForm.securityLevel === '2' ||
                  scanForm.securityLevel === '3'
                "
              >
                <v-text-field
                  v-model="scanForm.authPass"
                  label="Auth Password"
                  type="password"
                  :rules="[(v) => !!v || 'Auth Password is required']"
                />
                <v-select
                  v-model="scanForm.authProtocol"
                  label="Auth Protocol"
                  :items="['MD5', 'SHA']"
                  :rules="[(v) => !!v || 'Auth Protocol is required']"
                />
              </template>
              <template v-if="scanForm.securityLevel === '3'">
                <v-text-field
                  v-model="scanForm.privPass"
                  label="Privacy Password"
                  type="password"
                  :rules="[(v) => !!v || 'Privacy Password is required']"
                />
                <v-select
                  v-model="scanForm.privProtocol"
                  label="Privacy Protocol"
                  :items="['DES', 'AES']"
                  :rules="[(v) => !!v || 'Privacy Protocol is required']"
                />
              </template>
            </template>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn color="white" text @click="dialog = false">Cancel</v-btn>
          <v-btn color="white" @click="handleScanDevices">Scan</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import { networkStore } from "@/stores/network";
import { computed, ref } from "vue";

export default {
  setup() {
    const store = networkStore();
    const dialog = ref(false);
    const scanForm = ref({
      baseIp: "",
      prefix: "",
      community: "public",
      port: 161,
      version: "",
      authUsername: "",
      authPass: "",
      privPass: "",
      authProtocol: "",
      privProtocol: "",
      securityLevel: "3", // Mặc định là authPriv
    });
    const selectedNetwork = ref(null);
    const form = ref(null);

    const networks = computed(() => store.networks);
    const isLoading = computed(() => store.isLoading);
    const error = computed(() => store.error);
    const isSnmpV3 = computed(() => scanForm.value.version === "3");

    const handleScanNetwork = async () => {
      await store.scanNetwork();
    };

    const openScanDialog = (network) => {
      selectedNetwork.value = network;
      scanForm.value.baseIp = network.ipRange.split("/")[0];
      scanForm.value.prefix = network.ipRange.split("/")[1];
      dialog.value = true;
    };

    const handleScanDevices = async () => {
      if (form.value.validate()) {
        await store.scanDevices({
          networkId: selectedNetwork.value.id,
          baseIp: scanForm.value.baseIp,
          prefix: scanForm.value.prefix,
          community: scanForm.value.community,
          port: Number(scanForm.value.port),
          version: scanForm.value.version,
          authUsername: scanForm.value.authUsername,
          authPass: scanForm.value.authPass,
          privPass: scanForm.value.privPass,
          authProtocol: scanForm.value.authProtocol,
          privProtocol: scanForm.value.privProtocol,
          securityLevel: scanForm.value.securityLevel,
        });
        dialog.value = false;
        scanForm.value = {
          baseIp: "",
          prefix: "",
          community: "public",
          port: 161,
          version: "",
          authUsername: "",
          authPass: "",
          privPass: "",
          authProtocol: "",
          privProtocol: "",
          securityLevel: "3",
        };
      }
    };

    return {
      networks,
      isLoading,
      error,
      handleScanNetwork,
      scanDevices: store.scanDevices,
      dialog,
      scanForm,
      form,
      openScanDialog,
      handleScanDevices,
      isSnmpV3,
    };
  },
};
</script>

<style scoped>
.header {
  padding: 10px;
}
.loading {
  display: block;
  margin: 30px auto;
  width: 60px;
  height: 60px;
  color: #1e88e5;
  border-radius: 50%;
  animation: pulse 1.5s infinite ease-in-out;
  box-shadow: 0 0 15px rgba(30, 136, 229, 0.6);
}
.v-btn {
  background: linear-gradient(135deg, #43a047, #1e88e5);
  color: #ffffff;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}
.v-btn:hover {
  background: linear-gradient(135deg, #1e88e5, #43a047);
  transform: scale(1.05);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
}
.v-alert {
  border-radius: 8px;
  margin: 20px 0;
  padding: 15px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
  animation: fadeIn 0.5s ease-in;
}
.v-table {
  width: 100%;
  border-collapse: collapse;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  margin: 20px 0;
}
.v-table th {
  background: linear-gradient(135deg, #1e88e5, #43a047);
  color: #ffffff;
  padding: 15px 18px;
  text-align: left;
  font-weight: 600;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
  text-transform: uppercase;
  letter-spacing: 1px;
}
.v-table td {
  padding: 15px 18px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  color: #2c3e50;
  transition: background 0.3s ease;
}
.v-table tr:hover {
  background-color: rgba(227, 242, 253, 0.9);
}
.v-dialog .v-card {
  border-radius: 12px;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
}
.v-card-title {
  background: linear-gradient(135deg, #1e88e5, #43a047);
  color: #ffffff;
  padding: 15px;
  border-radius: 3px 3px 0 0;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.v-card-text {
  padding: 20px;
}
.v-form .v-text-field,
.v-form .v-select {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}
.v-card-actions .v-btn {
  padding: 10px 20px;
  border-radius: 8px;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes pulse {
  0% {
    transform: scale(0.9);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
  100% {
    transform: scale(0.9);
    opacity: 0.7;
  }
}
</style>
