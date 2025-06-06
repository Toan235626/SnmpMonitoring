<template>
  <div class="device-tab">
    <div class="search-bar">
      <v-text-field
        v-model="oid"
        label="OID"
        variant="outlined"
        class="search-input"
      />
      <v-text-field
        v-model="community"
        label="Community String"
        variant="outlined"
        class="search-input"
      />
      <v-text-field
        v-model="port"
        label="Port"
        type="number"
        variant="outlined"
        class="search-input"
      />
      <v-select
        v-model="version"
        label="SNMP Version"
        :items="['1', '2c', '3']"
        variant="outlined"
        class="search-input"
        @update:modelValue="onVersionChange"
      />
      <v-text-field
        v-if="version === '3'"
        v-model="authUsername"
        label="Auth Username"
        variant="outlined"
        class="search-input"
      />
      <v-text-field
        v-if="version === '3'"
        v-model="authPass"
        label="Auth Password"
        type="password"
        variant="outlined"
        class="search-input"
      />
      <v-text-field
        v-if="version === '3'"
        v-model="privPass"
        label="Privacy Password"
        type="password"
        variant="outlined"
        class="search-input"
      />
      <v-select
        v-if="version === '3'"
        v-model="authProtocol"
        label="Auth Protocol"
        :items="['MD5', 'SHA']"
        variant="outlined"
        class="search-input"
      />
      <v-select
        v-if="version === '3'"
        v-model="privProtocol"
        label="Privacy Protocol"
        :items="['DES', 'AES']"
        variant="outlined"
        class="search-input"
      />
      <v-text-field
        v-if="version === '3'"
        v-model="securityLevel"
        label="Security Level"
        type="number"
        variant="outlined"
        class="search-input"
      />
    </div>

    <div class="actions">
      <v-btn @click="performAction('get')">Get</v-btn>
      <v-btn @click="performAction('getNext')">GetNext</v-btn>
      <v-btn @click="performAction('getBulk')">GetBulk</v-btn>
      <v-btn @click="performAction('walk')">Walk</v-btn>
      <v-btn @click="performAction('getMibTree')">Refresh MIB Tree</v-btn>
    </div>

    <v-progress-circular
      v-if="isLoading"
      indeterminate
      color="primary"
      class="loading"
    />

    <v-table v-if="results.length">
      <thead>
        <tr>
          <th>OID</th>
          <th>Value</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="result in results" :key="result.oid">
          <td>{{ result.oid }}</td>
          <td>{{ result.value }}</td>
        </tr>
      </tbody>
    </v-table>

    <v-table v-if="deviceHistory.length">
      <thead>
        <tr>
          <th>Time</th>
          <th>OID</th>
          <th>Community</th>
          <th>Result</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="history in deviceHistory" :key="history.id">
          <td>{{ history.time }}</td>
          <td>{{ history.oid }}</td>
          <td>{{ history.community }}</td>
          <td>{{ history.result }}</td>
        </tr>
      </tbody>
    </v-table>
  </div>
</template>

<script>
import { ref, computed } from "vue";
import { deviceStore } from "@/stores/device";

export default {
  props: {
    deviceId: {
      type: String,
      required: true,
    },
  },
  setup(props) {
    const store = deviceStore();
    const oid = ref("");
    const community = ref("public"); // Mặc định
    const port = ref(161); // Mặc định
    const version = ref("2c"); // Mặc định
    const authUsername = ref("");
    const authPass = ref("");
    const privPass = ref("");
    const authProtocol = ref("");
    const privProtocol = ref("");
    const securityLevel = ref(0);

    const onVersionChange = (newVersion) => {
      if (newVersion !== "3") {
        authUsername.value = "";
        authPass.value = "";
        privPass.value = "";
        authProtocol.value = "";
        privProtocol.value = "";
        securityLevel.value = 0;
      }
    };

    const deviceHistory = computed(() =>
      store.searchHistory.filter(
        (history) => history.deviceId === props.deviceId
      )
    );

    const activeTab = computed(() => store.activeTab);
    const results = computed(() => store.results[props.deviceId] || []);
    const mibTreeData = computed(() => store.mibTreeData[props.deviceId] || {});
    const error = computed(() => store.error);

    return {
      oid,
      community,
      port,
      version,
      authUsername,
      authPass,
      privPass,
      authProtocol,
      privProtocol,
      securityLevel,
      onVersionChange,
      results,
      deviceHistory,
      mibTreeData,
      activeTab,
      error,
      isLoading: computed(() => store.isLoading),
      performAction: async (action) => {
        await store.performAction(action, props.deviceId, {
          oid: oid.value,
          community: community.value,
          port: Number(port.value),
          version: version.value,
          authUsername: authUsername.value,
          authPass: authPass.value,
          privPass: privPass.value,
          authProtocol: authProtocol.value,
          privProtocol: privProtocol.value,
          securityLevel: String(securityLevel.value),
        });
      },
    };
  },
};
</script>

<style scoped>
.device-tab {
  padding: 20px;
}
.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}
.search-input {
  max-width: 300px;
}
.actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.loading {
  display: block;
  margin: 20px auto;
}
.error {
  color: red;
  margin-top: 10px;
}
</style>
