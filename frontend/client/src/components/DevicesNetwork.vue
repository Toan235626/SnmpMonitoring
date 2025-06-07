<template>
  <div>
    <div class="header">
    </div>  
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
              v-model="scanForm.prefix"
              label="IP Prefix (e.g., 24)"
              :rules="[(v) => !!v || 'IP Prefix is required']"
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
      prefix: "",
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
      scanForm.value.baseIp = network.ipRange.split("/")[0]; // Lấy baseIp từ ipRange
      scanForm.value.prefix = network.ipRange.split("/")[1]; // Lấy prefix từ ipRange
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
        });
        dialog.value = false;
        scanForm.value = {
          baseIp: "",
          prefix: "",
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
.header {
  padding: 10px;
}
.loading {
  display: block;
  margin: 30px auto;
  width: 60px; /* Larger for visibility */
  height: 60px;
  color: #1e88e5; /* Vibrant blue to match theme */
  border-radius: 50%; /* Circular shape */
  animation: pulse 1.5s infinite ease-in-out; /* Pulsing effect for dynamism */
  box-shadow: 0 0 15px rgba(30, 136, 229, 0.6); /* Glow effect */
}
.v-btn {
  background: linear-gradient(135deg, #43a047, #1e88e5); /* Green to blue gradient for buttons */
  color: #ffffff; /* White text */
  border-radius: 8px; /* Rounded corners */
  padding: 10px 20px; /* Comfortable padding */
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease; /* Smooth transitions */
}
.v-btn:hover {
  background: linear-gradient(135deg, #1e88e5, #43a047); /* Reverse gradient on hover */
  transform: scale(1.05); /* Slight scale for interaction */
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
}
.v-alert {
  border-radius: 8px; /* Rounded corners */
  margin: 20px 0; /* Spacing */
  padding: 15px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15); /* Subtle shadow */
  animation: fadeIn 0.5s ease-in; /* Fade-in for alerts */
}
.v-table {
  width: 100%;
  border-collapse: collapse;
  background-color: rgba(255, 255, 255, 0.9); /* Slightly transparent white */
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  overflow: hidden; /* Round corners for table */
  margin: 20px 0;
}
.v-table th {
  background: linear-gradient(135deg, #1e88e5, #43a047); /* Gradient header */
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
  color: #2c3e50; /* Dark text for contrast */
  transition: background 0.3s ease;
}
.v-table tr:hover {
  background-color: rgba(227, 242, 253, 0.9); /* Light hover effect */
}
.v-dialog .v-card {
  border-radius: 12px; /* Rounded dialog */
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3); /* Deep shadow */
  background: linear-gradient(145deg, #ffffff, #e8ecef); /* Subtle gradient */
}
.v-card-title {
  background: linear-gradient(135deg, #1e88e5, #43a047); /* Gradient title */
  color: #ffffff;
  padding: 15px;
  border-radius: 12px 12px 0 0; /* Round top corners */
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.v-card-text {
  padding: 20px;
}
.v-form .v-text-field, .v-form .v-select {
  background: rgba(255, 255, 255, 0.1); /* Semi-transparent inputs */
  border-radius: 8px;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}
.v-form .v-text-field:hover, .v-form .v-select:hover {
  box-shadow: 0 0 10px rgba(30, 136, 229, 0.5); /* Glow on hover */
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
