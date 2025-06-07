<template>
  <div>
    <v-btn color="green" @click="handleScanNetwork" :disabled="isLoading">Scan Network</v-btn>
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
            <v-btn color="green" @click="openScanDialog(network)">Scan Devices</v-btn>
          </td>
        </tr>
      </tbody>
    </v-table>

    <!-- Dialog nhập thông tin quét -->
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
              v-model="scanForm.community"
              label="Community String"
              :rules="[(v) => !!v || 'Community is required']"
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
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn color="grey" text @click="dialog = false">Cancel</v-btn>
          <v-btn color="primary" @click="handleScanDevices">Scan</v-btn>
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
      community: "public",
      port: 161,
      version: "",
    });
    const selectedNetwork = ref(null);
    const form = ref(null);

    const networks = computed(() => store.networks);
    const isLoading = computed(() => store.isLoading);
    const error = computed(() => store.error);

    const handleScanNetwork = async () => {
      await store.scanNetwork();
    };

    const openScanDialog = (network) => {
      selectedNetwork.value = network;
      // scanForm.value.baseIp =
      //   network.ipRange.split(".")[0] +
      //   "." +
      //   network.ipRange.split(".")[1] +
      //   "." +
      //   network.ipRange.split(".")[2]; // Lấy baseIp từ ipRange
      scanForm.value.baseIp = network.ipRange.replace('/24', '');
      dialog.value = true;
    };

    const handleScanDevices = async () => {
      if (form.value.validate()) {
        await store.scanDevices({
          networkId: selectedNetwork.value.id,
          baseIp: scanForm.value.baseIp,
          community: scanForm.value.community,
          port: Number(scanForm.value.port),
          version: scanForm.value.version,
        });
        dialog.value = false;
        scanForm.value = {
          baseIp: "",
          community: "",
          port: "161",
          version: "",
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
    };
  },
};
</script>

<style scoped>
.loading {
  display: block;
  margin: 20px auto;
}
</style>
